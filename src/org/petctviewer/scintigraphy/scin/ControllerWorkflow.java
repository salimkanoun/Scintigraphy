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
 * Then, the constructor must call the {@link #generateInstructions()} and the
 * {@link #start()} methods (in that order).
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

	protected Workflow[] workflows;

	protected int indexCurrentImage;
	protected ImageState currentState;

	/**
	 * Index of the ROI to store in the RoiManager.
	 */
	private int indexRoi;

	public ControllerWorkflow(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
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

	/**
	 * This method initializes the controller. It must be called <b>after</b> the
	 * {@link #generateInstructions()} method.
	 */
	protected void start() {
		this.indexCurrentImage = 0;
		this.indexRoi = 0;

		Instruction i = this.workflows[0].next();
		if (i != null) {
			// TODO: maybe do not assume the lateralisation is RL?
			System.out.println(this.workflows[0].getImageAssociated().getImageOrientation());
			this.currentState = new ImageState(
					this.workflows[0].getImageAssociated().getImageOrientation().getFacingOrientation(), 1,
					ImageState.LAT_RL, ImageState.ID_NONE);
			this.setOverlay(currentState);

			this.displayInstruction(i.getMessage());
			this.prepareImage(i.getImageState());
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
	 * @return array of ROI indexes to display for the current instruction
	 */
	private int[] roisToDisplay() {
		List<Instruction> dris = new ArrayList<>();
		for (Instruction i : this.workflows[this.indexCurrentImage]
				.getInstructionsWithOrientation(this.currentState.getFacingOrientation()))
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

	private String generateRoiName(int indexImage, String instructionRoiName) {
		return instructionRoiName + "-" + this.currentState.getFacingOrientation().abrev();
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();
		System.out.println(this.workflows[this.indexCurrentImage]);

		Instruction currentInstruction = this.workflows[this.indexCurrentImage].previous();
		if (currentInstruction == null) {
			this.indexCurrentImage--;
			currentInstruction = this.workflows[this.indexCurrentImage].getCurrentInstruction();
			currentInstruction.prepareAsPrevious();
		}
		System.out.println("Previous instruction: " + currentInstruction.getClass().getSimpleName());

		if (currentInstruction.isExpectingUserInput()) {
			this.displayInstruction(currentInstruction.getMessage());
			this.vue.getOverlay().clear();
			this.prepareImage(currentInstruction.getImageState());

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
					this.saveRoiAtIndex(this.generateRoiName(indexPreviousImage, previousInstruction.getRoiName()),
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
				// TODO: might be a problem if the workflow is over: this code should not
				// execute
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
	 * Prepares the ImagePlus with the specified state and updates the currentState.
	 * 
	 * @param imageState
	 *            State the ImagePlus must complies
	 */
	private void prepareImage(ImageState imageState) {
		if (imageState == null)
			return;

		boolean resetOverlay = false;

		// == FACING ORIENTATION ==
		if (imageState.getFacingOrientation() != null
				&& imageState.getFacingOrientation() != this.currentState.getFacingOrientation()) {
			this.currentState.setFacingOrientation(imageState.getFacingOrientation());
			resetOverlay = true;
		}

		// == ID IMAGE ==
		if (imageState.getIdImage() == ImageState.ID_NONE)
			// Don't use the id
			this.currentState.setIdImage(this.indexCurrentImage);
		else if (imageState.getIdImage() >= 0)
			// Use the specified id
			this.currentState.setIdImage(imageState.getIdImage());
		// else, don't touch the previous id

		// Change image only if different than the previous
		if (this.vue.getImagePlus() != this.model.getImageSelection()[this.currentState.getIdImage()].getImagePlus()) {
			this.vue.setImage(this.model.getImageSelection()[this.currentState.getIdImage()].getImagePlus());
			resetOverlay = true;
		}

		// == SLICE ==
		if (imageState.getSlice() > ImageState.SLICE_PREVIOUS)
			// Use the specified slice
			this.currentState.setSlice(imageState.getSlice());
		// else, don't touch the previous slice

		// Change slice only if different than the previous
		if (this.currentState.getSlice() != this.vue.getImagePlus().getCurrentSlice()) {
			this.vue.getImagePlus().setSlice(this.currentState.getSlice());
			resetOverlay = true;
		}

		if (resetOverlay) {
			this.vue.getOverlay().clear();
			this.setOverlay(this.currentState);
		}
	}

	@Override
	public boolean isOver() {
		return this.workflows[this.indexCurrentImage].getCurrentInstruction() instanceof LastInstruction;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (!(e.getSource() instanceof Button))
			return;

		Button source = (Button) e.getSource();
		if (source.getActionCommand().contentEquals(COMMAND_END)) {
			if (this.workflows[this.indexCurrentImage].getCurrentInstruction() instanceof GeneratorInstruction) {
				((GeneratorInstruction) this.workflows[this.indexCurrentImage].getCurrentInstruction()).stop();
				this.clicSuivant();
			}
		}
	}

}