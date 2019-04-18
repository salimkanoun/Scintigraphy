package org.petctviewer.scintigraphy.gastric_refactored;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

public class Controller_Gastric extends ControleurScin {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	private final boolean FIRST_ORIENTATION_POST;
	private final int SLICE_FIRST, SLICE_SECOND;

	private final String[] organs = { "Stomach", "Intestine" };
	private int currentOrgan, currentOrientation, currentImage;

	/**
	 * This controller expects that the selectedImages are of the form:
	 * <ul>
	 * <li>any number of images</li>
	 * <li>Ant/Post Orientation</li>
	 * </ul>
	 */
	public Controller_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));
		// TODO: set this variable with the preferences of the user
		this.FIRST_ORIENTATION_POST = false;

		this.SLICE_FIRST = (this.FIRST_ORIENTATION_POST ? SLICE_POST : SLICE_ANT);
		this.SLICE_SECOND = this.SLICE_FIRST % 2 + 1;

		this.currentOrgan = 0;
		this.currentOrientation = this.SLICE_FIRST;
		this.currentImage = 0;

		this.prepareOrientation();
	}

//	private void DEBUG(String s) {
//		System.out.println("== " + s + " ==");
//		System.out.println("Current image: " + this.currentImage);
//		System.out.println("Current orientation: " + (this.currentOrientation == SLICE_ANT ? "ANT" : "POST") + "("
//				+ this.currentOrientation + ")");
//		System.out.println(
//				"Current organ: " + (this.currentOrgan < this.organs.length ? this.organs[this.currentOrgan] : "") + "("
//						+ this.currentOrgan + ")");
//		System.out.println();
//	}

	private void prepareOrientation() {
		this.resetOverlay();

		this.vue.setImage(this.model.getImagesPlus()[this.currentImage]);
		this.vue.getImagePlus().setSlice(this.currentOrientation);

		this.editOrgan();
	}

	private void nextOrientation() {
		this.currentOrgan = 0;
		this.currentOrientation = this.currentOrientation % 2 + 1;
		this.prepareOrientation();
	}

	private void previousOrientation() {
		this.currentOrgan = this.organs.length - 1;
		this.currentOrientation--;
		this.prepareOrientation();
	}

	private void nextImage() {
		this.currentOrgan = 0;
		this.currentOrientation = SLICE_FIRST;
		this.currentImage++;
		this.prepareOrientation();
	}

	private void previousImage() {
		this.currentOrgan = this.organs.length - 1;
		this.currentOrientation = SLICE_SECOND;
		this.currentImage--;
		this.prepareOrientation();
	}

	private void nextOrgan() {
		this.currentOrgan++;
		this.editOrgan();
	}

	private void previousOrgan() {
		this.currentOrgan--;
		this.editOrgan();
	}

	/**
	 * Checks that there is an intersection between the previous ROI and the current
	 * one.
	 */
	private boolean checkIntersectionBetweenRois() {
		this.model.getRoiManager().setSelectedIndexes(new int[] { this.position - 1, this.position });
		this.model.getRoiManager().runCommand("AND");
		this.model.getRoiManager().runCommand("Deselect");
		this.model.getRoiManager().deselect();
		if (this.vue.getImagePlus().getRoi() == null) {
			JOptionPane.showMessageDialog(this.vue,
					"Please adjust the intestine in order to create an intersection with the estomach.",
					"Intersection missing", JOptionPane.PLAIN_MESSAGE);
			this.model.getRoiManager().select(this.position);
			return false;
		}
		return true;
	}

	/**
	 * This method displays the organ to edit (if necessary) and the instruction for
	 * the user.
	 * 
	 * @param indexRoi
	 */
	private void editOrgan() {
		boolean existed = false;

		existed = this.editRoi(this.position);
		if (!existed) {
			existed = this.editCopyRoi(this.position - 2);
		}

		if (existed)
			this.displayInstructionCurrentOrgan("Adjust");
		else
			this.displayInstructionCurrentOrgan("Delimit");
	}

	/**
	 * Displays the current organ's instruction type.<br>
	 * Instruction is of the form: 'TYPE the CURRENT_ORGAN'.<br>
	 * example: 'Delimit the Right Kidney'.
	 * 
	 * @param type Type of the instruction (expecting 'Delimit' or 'Adjust')
	 */
	private void displayInstructionCurrentOrgan(String type) {
		this.displayInstruction(type + " the " + this.organs[this.currentOrgan]);
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		if (this.currentOrgan == 0)
			if (this.currentOrientation == SLICE_FIRST)
				this.previousImage();
			else
				this.previousOrientation();
		else
			this.previousOrgan();

		this.displayRois(this.position - this.currentOrgan, this.position);
	}

	@Override
	public void clicSuivant() {
		try {
			this.saveCurrentRoi("#" + this.currentImage + "_" + this.organs[this.currentOrgan]
					+ (this.currentOrientation == SLICE_POST ? "_P" : "_A"));
			if (this.currentOrgan == this.organs.length - 1 && !this.checkIntersectionBetweenRois())
				return;

			this.displayRoi(this.position);
			super.clicSuivant();

			// All organs delimited
			if (this.currentOrgan >= this.organs.length - 1)
				if (this.currentOrientation == SLICE_SECOND)
					if (this.currentImage >= this.model.getImageSelection().length - 1)
						this.end();
					else
						this.nextImage();
				else
					this.nextOrientation();
			else
				this.nextOrgan();

		} catch (NoDataException e) {
			JOptionPane.showMessageDialog(this.vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public boolean isOver() {
		return this.currentImage == this.model.getImageSelection().length - 1 && this.currentOrientation == SLICE_SECOND
				&& this.currentOrgan == this.organs.length - 1;
	}

	@Override
	protected void end() {
		this.currentOrgan++;
		super.end();
	}

}
