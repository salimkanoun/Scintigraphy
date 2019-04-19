package org.petctviewer.scintigraphy.scin;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;
import org.petctviewer.scintigraphy.scin.instructions.Instruction.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.LastInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;

public abstract class ControllerWorkflow extends ControleurScin {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

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
	 * for this model.
	 */
	protected abstract void generateInstructions();

	private void start() {
		Instruction i = this.workflows[0].next();
		if (i != null) {
			this.displayInstruction(i.getMessage());
			this.prepareImage(i.getImageState());
			this.resetOverlay();
		}
	}

	/**
	 * This method displays the ROI to edit (if necessary).
	 */
	private void editOrgan(int roiToCopy) {
		if (!this.editRoi(this.indexRoi))
			this.editCopyRoi(roiToCopy);
	}

	private void DEBUG(String s) {
		Instruction i = this.workflows[this.indexCurrentImage].getCurrentInstruction();
		System.out.println("== " + s + " ==");
		System.out.println("Current image: " + this.indexCurrentImage);
		System.out.println("Current instruction: " + (i != null ? i.getMessage() : "-No Instruction-"));
		System.out.println("Index roi: " + this.indexRoi);
		System.out.println("Current position: " + this.position);
		System.out.println("=======");
	}

	private int countRoisCreatedUntilNow() {
		int count = 0;
		for (int i = 0; i < this.indexCurrentImage; i++)
			count += this.workflows[i].countRoisCreated();
		return count;
	}

	@Override
	public void clicPrecedent() {
		this.DEBUG("PREVIOUS");
		super.clicPrecedent();

		Instruction previousInstruction = this.workflows[this.indexCurrentImage].getCurrentInstruction();
		Instruction currentInstruction = this.workflows[this.indexCurrentImage].previous();
		if (currentInstruction == null) {
			this.indexCurrentImage--;
			currentInstruction = this.workflows[this.indexCurrentImage].previous();
		}

		System.out.println("Previous:: " + previousInstruction);
		System.out.println("Current:: " + currentInstruction);

		if (currentInstruction.isDisplayable()) {
			this.displayInstruction(currentInstruction.getMessage());
			this.prepareImage(currentInstruction.getImageState());

			this.displayRois(this.countRoisCreatedUntilNow(), --this.indexRoi);

			if (currentInstruction instanceof DrawRoiInstruction) {
				DrawRoiInstruction dri_current = (DrawRoiInstruction) currentInstruction;
				this.editOrgan(dri_current.roiToDisplay());
			}
		} else {
			this.clicPrecedent();
		}

		this.DEBUG("       ");
	}

	@Override
	public void clicSuivant() {

		this.DEBUG("NEXT");

		Instruction previousInstruction = this.workflows[this.indexCurrentImage].getCurrentInstruction();
		int indexPreviousImage = this.indexCurrentImage;
		if (this.workflows[this.indexCurrentImage].isOver()) {
			this.indexCurrentImage++;
		}
		Instruction currentInstruction = this.workflows[this.indexCurrentImage].next();

		System.out.println("Previous:: " + previousInstruction);
		System.out.println("Current:: " + currentInstruction);

		if (previousInstruction != null) {
			if (previousInstruction instanceof DrawRoiInstruction) {
				DrawRoiInstruction dri_previous = (DrawRoiInstruction) previousInstruction;
				if (dri_previous.isDisplayable()) {
					try {
						this.saveRoiAtIndex(
								"#" + indexPreviousImage + "_" + dri_previous.getOrganName()
										+ (this.vue.getImagePlus().getSlice() == SLICE_ANT ? "_A" : "_P"),
								this.indexRoi);
						dri_previous.setRoi(this.indexRoi);

						this.displayRoi(this.indexRoi);

						this.indexRoi++;
					} catch (NoDataException e) {
						JOptionPane.showMessageDialog(vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}
		}
		super.clicSuivant();

		if (this.isOver()) {
			this.end();
		}

		if (currentInstruction.isDisplayable()) {
			this.displayInstruction(currentInstruction.getMessage());
			this.prepareImage(currentInstruction.getImageState());

			if (currentInstruction instanceof DrawRoiInstruction) {
				DrawRoiInstruction dri_current = (DrawRoiInstruction) currentInstruction;
				this.editOrgan(dri_current.roiToDisplay());
			}
		} else {
			this.clicSuivant();
		}

		this.DEBUG("    ");
	}

	private void prepareImage(ImageState imageState) {
		if (imageState == null)
			return;

		if (this.vue.getImagePlus() != this.model.getImageSelection()[this.indexCurrentImage].getImagePlus()) {
			this.vue.setImage(this.model.getImageSelection()[this.indexCurrentImage].getImagePlus());
			this.resetOverlay();
		}

		int newSlice = -1;
		if (imageState.orientation == Orientation.ANT)
			newSlice = SLICE_ANT;
		else if (imageState.orientation == Orientation.POST)
			newSlice = SLICE_POST;
		else
			System.err.println("The orientation specified in the state (" + imageState.orientation
					+ ") is not valid, it shoud be one of:\n[" + Orientation.ANT + ", " + Orientation.POST + "]");
		if(newSlice != -1 && newSlice != this.vue.getImagePlus().getSlice()) {
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

}
