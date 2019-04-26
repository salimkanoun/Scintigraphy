package org.petctviewer.scintigraphy.scin;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.generator.GeneratorInstruction;

/**
 * This controller is used when working with a flow of instructions.<br>
 * In order to use this type of controller, you need to redefine the
 * {@link #generateInstructions()} method to create the workflow.<br>
 * Typically, this will look like a repetition of this:<br>
 * 
 * <pre>
 * this.workflow[0].addInstruction(new DrawRoiInstruction(...));
 * ...
 * this.workflow[0].addInstruction(new EndInstruction());
 * </pre>
 * 
 * Be careful, this controller expect that the images are in Ant/Post
 * orientation.
 * 
 * @author Titouan QUÉMA
 *
 */
public abstract class ControllerWorkflow extends ControleurScin {

	protected static final int SLICE_ANT = 1, SLICE_POST = 2;

	protected Workflow[] workflows;
	protected int indexCurrentImage;

	private int indexRoi;

	public ControllerWorkflow(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
		this.workflows = new Workflow[model.getImageSelection().length];
		this.generateInstructions();

		this.indexCurrentImage = 0;
		this.indexRoi = 0;

		this.start();
	}

	/**
	 * This method must instantiate the workflow and fill it with the instructions
	 * for this model.<br>
	 * Only the last workflow generated MUST end with a {@link LastInstruction}.
	 */
	protected abstract void generateInstructions();

	private void start() {
		Instruction i = this.workflows[0].next();
		if (i != null) {
			this.displayInstruction(i.getMessage());
			this.prepareImage(i.getImageState());
			this.resetOverlay();
			i.afterNext(this);
		}
	}

	/**
	 * This method displays the ROI to edit (if necessary).
	 */
	private void editOrgan(int roiToCopy) {
		if (!this.editRoi(this.indexRoi))
			this.editCopyRoi(roiToCopy);
	}

	/**
	 * @return current orientation of the ImagePlus
	 */
	private Orientation getCurrentOrientation() {
		switch (this.vue.getImagePlus().getCurrentSlice()) {
		case SLICE_ANT:
			return Orientation.ANT;
		case SLICE_POST:
			return Orientation.POST;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * @return array of ROI indexes to display for the current instruction
	 */
	private int[] roisToDisplay() {
		List<Instruction> dris = new ArrayList<>();
		for (Instruction i : this.workflows[this.indexCurrentImage]
				.getInstructionsWithOrientation(this.getCurrentOrientation()))
			if (i.roiToDisplay() >= 0 && i.roiToDisplay() < this.indexRoi) {
				dris.add(i);
			}
		int[] array = new int[dris.size()];
		for (int i = 0; i < dris.size(); i++)
			array[i] = dris.get(i).roiToDisplay();
		return array;
	}

	private void DEBUG(String s) {
		System.out.println("=== " + s + " ===");
		System.out.println("Current position: " + this.position);
		System.out.println("Current image: " + this.indexCurrentImage);
		String currentInstruction = "-No instruction-";
		if (this.indexCurrentImage >= 0 && this.indexCurrentImage < this.workflows.length) {
			Instruction i = this.workflows[this.indexCurrentImage].getCurrentInstruction();
			if (i != null)
				currentInstruction = i.getMessage();
			else
				currentInstruction = "-No Message-";
		}
		System.out.println("Current instruction: " + currentInstruction);
		System.out.println();
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();
		System.out.println(this.workflows[this.indexCurrentImage]);

		Instruction currentInstruction = this.workflows[this.indexCurrentImage].previous();
		if (currentInstruction == null) {
			this.indexCurrentImage--;
			currentInstruction = this.workflows[this.indexCurrentImage].getCurrentInstruction();
		}

		if (currentInstruction.isExpectingUserInput()) {
			this.displayInstruction(currentInstruction.getMessage());
			this.prepareImage(currentInstruction.getImageState());

			this.resetOverlay();
			if (currentInstruction.saveRoi())
				this.indexRoi--;
			this.displayRois(this.roisToDisplay());

			if (currentInstruction.saveRoi()) {
				this.editOrgan(currentInstruction.roiToDisplay());
			}

			currentInstruction.afterPrevious(this);
		} else {
			if (currentInstruction.saveRoi() && !currentInstruction.isRoiVisible()) {
				this.indexRoi--;
			}
			currentInstruction.afterPrevious(this);
			this.clicPrecedent();
		}

		DEBUG("PREVIOUS");
	}

	@Override
	public void clicSuivant() {
		System.out.println(this.workflows[this.indexCurrentImage]);

		Instruction previousInstruction = this.workflows[this.indexCurrentImage].getCurrentInstruction();

		// Only execute 'Next' if the instruction is not cancelled
		if (!previousInstruction.isCancelled()) {

			// Prepare next instruction
			int indexPreviousImage = this.indexCurrentImage;

			// === Draw ROI of the previous instruction ===
			if (previousInstruction != null && previousInstruction.saveRoi()) {
				Orientation orientation = previousInstruction.getImageState() == null ? this.getCurrentOrientation()
						: previousInstruction.getImageState().orientation;

				try {
					this.saveRoiAtIndex(
							"#" + indexPreviousImage + "_" + previousInstruction.getRoiName() + orientation.abrev(),
							this.indexRoi);
					previousInstruction.setRoi(this.indexRoi);

					if (previousInstruction.isRoiVisible())
						this.displayRoi(this.indexRoi);

					this.indexRoi++;
				} catch (NoDataException e) {
					JOptionPane.showMessageDialog(vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			// == Generate next instruction if necessary ==
			if (previousInstruction instanceof GeneratorInstruction) {
				GeneratorInstruction generatorInstruction = (GeneratorInstruction) previousInstruction;
				this.workflows[indexPreviousImage].addInstructionOnTheFly(generatorInstruction.generate());
			}

			// == Go to the next instruction ==
			super.clicSuivant();

			if (this.workflows[this.indexCurrentImage].isOver()) {
				this.indexCurrentImage++;
			}
			Instruction nextInstruction = this.workflows[this.indexCurrentImage].next();

			if (this.isOver()) {
				nextInstruction.afterNext(this);
				this.end();
			}

			// == Display instruction for the user ==
			if (nextInstruction.isExpectingUserInput()) {
				this.displayInstruction(nextInstruction.getMessage());
				this.prepareImage(nextInstruction.getImageState());

				if (nextInstruction.saveRoi())
					this.editOrgan(nextInstruction.roiToDisplay());

				nextInstruction.afterNext(this);
			} else {
				// If not displayable, go directly to the next instruction
				nextInstruction.afterNext(this);
				this.clicSuivant();
			}
		} else {
			// Execution cancelled
			if (!previousInstruction.isExpectingUserInput()) {
				// Since the previous instruction is not displayable, it should not be stopped
				// on, so you go back to the previous instruction
				this.clicPrecedent();
			}
		}

		DEBUG("NEXT");
	}

	/**
	 * Prepares the ImagePlus with the specified state.
	 * 
	 * @param imageState State the ImagePlus must complies
	 */
	private void prepareImage(ImageState imageState) {
		if (imageState == null)
			return;

		// Change image only if different than the previous
		if (this.vue.getImagePlus() != this.model.getImageSelection()[this.indexCurrentImage].getImagePlus()) {
			this.vue.setImage(this.model.getImageSelection()[this.indexCurrentImage].getImagePlus());
			this.resetOverlay();
		}

		// Change slice only if different than the previous
		int newSlice = -1;
		if (imageState.orientation == Orientation.ANT)
			newSlice = SLICE_ANT;
		else if (imageState.orientation == Orientation.POST)
			newSlice = SLICE_POST;
		else
			System.err.println("The orientation specified in the state (" + imageState.orientation
					+ ") is not valid, it shoud be one of:\n[" + Orientation.ANT + ", " + Orientation.POST + "]");
		if (newSlice != -1 && newSlice != this.vue.getImagePlus().getCurrentSlice()) {
			this.vue.getImagePlus().setSlice(newSlice);
			this.resetOverlay();
		}
	}

	@Override
	public boolean isOver() {
		return this.indexCurrentImage == this.model.getImageSelection().length - 1
				&& (this.workflows[this.indexCurrentImage].isOver()
						|| this.workflows[this.indexCurrentImage].getCurrentInstruction() instanceof LastInstruction);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		Button source = (Button) e.getSource();
		if (source.getActionCommand().contentEquals(COMMAND_END)) {
			if (this.workflows[this.indexCurrentImage].getCurrentInstruction() instanceof GeneratorInstruction)
				((GeneratorInstruction) this.workflows[this.indexCurrentImage].getCurrentInstruction()).stop();
		}
	}

}
