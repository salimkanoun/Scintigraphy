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

import ij.ImagePlus;

/**
 * This controller is used when working with a flow of instructions.<br>
 * In order to use this type of controller, you need to redefine the
 * {@link #generateInstructions()} method to create the workflow.
 * 
 * @author Titouan QUÉMA
 *
 */
public abstract class ControllerWorkflow extends ControleurScin {

	/**
	 * This command signals that the instruction should not generate a next
	 * instruction.<br>
	 * This is only used for {@link GeneratorInstruction}.
	 */
	public static final String COMMAND_END = "command.end";

	/**
	 * This constants can only be used if the {@link #imageOrientation} is static
	 */
	protected static final int SLICE_ANT = 1, SLICE_POST = 2;

	protected Workflow[] workflows;

	protected int indexCurrentImage;
	/**
	 * Orientation of the current image
	 */
	protected Orientation imageOrientation;

	private int indexRoi;

	public ControllerWorkflow(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
		// TODO: remove the creation of the workflow here to move it in the
		// generateInstruction method
		this.workflows = new Workflow[model.getImageSelection().length];
		this.generateInstructions();

		this.indexCurrentImage = 0;
		this.imageOrientation = this.model.getImageSelection()[0].getImageOrientation();
		this.indexRoi = 0;

		this.start();
	}

	/**
	 * This method must instantiate the workflow and fill it with the instructions
	 * for this model.<br>
	 * Typically, this will look like a repetition of this:<br>
	 * 
	 * <pre>
	 * this.workflow[0].addInstruction(new DrawRoiInstruction(...));
	 * ...
	 * this.workflow[0].addInstruction(new EndInstruction());
	 * </pre>
	 * 
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
	 * This method returns Ant if the image is displaying the anterior orientation
	 * and returns Post if the image is displaying the posterior orientation.<br>
	 * Note that this is NOT the real orientation of the image, only its
	 * representation on the window.
	 * 
	 * @return current orientation of the ImagePlus (Ant or Post)
	 */
	private Orientation getCurrentOrientation() {
		ImagePlus currentImage = this.model.selectedImages[this.indexCurrentImage].getImagePlus();
		ImageState currentState = this.workflows[this.indexCurrentImage].getCurrentInstruction().getImageState();

		switch (this.imageOrientation) {
		case ANT:
		case POST:
		case UNKNOWN:
			return this.imageOrientation;
		case ANT_POST:
			if (currentImage.getCurrentSlice() == 1)
				return Orientation.ANT;
			else
				return Orientation.POST;
		case POST_ANT:
			if (currentImage.getCurrentSlice() == 1)
				return Orientation.POST;
			else
				return Orientation.ANT;
		case DYNAMIC_ANT:
			return Orientation.ANT;
		case DYNAMIC_POST:
			return Orientation.POST;
		case DYNAMIC_ANT_POST:
			if (currentState.slice <= currentImage.getSlice() / 2)
				return Orientation.ANT;
			else
				return Orientation.POST;
		case DYNAMIC_POST_ANT:
			if (currentState.slice <= currentImage.getSlice() / 2)
				return Orientation.POST;
			else
				return Orientation.ANT;
		}
		return Orientation.UNKNOWN;
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
				try {
					this.saveRoiAtIndex("#" + indexPreviousImage + "_" + previousInstruction.getRoiName()
							+ this.getCurrentOrientation().abrev(), this.indexRoi);
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
		int newSlice = imageState.slice;
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
