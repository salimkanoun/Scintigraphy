package org.petctviewer.scintigraphy.lympho;

import java.awt.Color;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.lympho.gui.FenResultatsLympho;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.gui.Roi;

public class ControleurLympho extends ControleurScin {

	public String[] organes = { "L. foot", "R. foot" };

	private static final int FIRST_IMAGE = 0, SECOND_IMAGE = 1;

	private int organe;

	private int organeRoiMaganer;

	private int etape;

	private static final int CAPTURE_FIRST_ANT = 0, CAPTURE_FIRST_POST = 1, CAPTURE_SECOND_ANT = 2,
			CAPTURE_SECOND_POST = 3, TOTAL_CAPTURES = 4;

	private ImagePlus[] captures;

	private boolean firstOrientationOver;

	public ControleurLympho(Scintigraphy main, FenApplication vue, String examType, ImageSelection[] selectedImages) {
		super(main, vue, new ModeleLympho(selectedImages, examType));

		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		((ModeleLympho) model).setLocked(true);

		this.organe = 0;
		this.organeRoiMaganer = 0;
		this.firstOrientationOver = false;
		etape = 0;

		this.captures = new ImagePlus[TOTAL_CAPTURES];

		this.changerImage();
	}

	@Override
	public boolean isOver() {
		return this.organeRoiMaganer >= this.organes.length * 2 - 1;
	}

	@Override
	public void clicSuivant() {
		try {
			this.saveCurrentRoi(this.organes[this.organe] + (this.etape == FIRST_IMAGE ? "_F" : "_S")
					+ (firstOrientationOver ? "_P" : "_A"));
			super.clicSuivant();

			this.displayRoi(this.position - 1);

			if (this.allOrgansDelimited()) {
				this.capture();
				if (this.firstOrientationOver)
					if (this.etape == this.organes.length - 1)
						this.end();
					else
						this.nextStep();
				else
					this.nextOrientation();
				;
			} else
				this.nextOrgan();
		} catch (NoDataException e) {
			JOptionPane.showMessageDialog(vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		if (this.organe == 0)
			if (!this.firstOrientationOver)
				this.previousStep();
			else
				this.previousOrientation();
		else
			this.previousOrgan();

		this.resetOverlay();
		this.displayRois(this.position - this.organe, this.position);

	}

	private void nextOrgan() {
		this.organe++;
		this.organeRoiMaganer++;
		this.editOrgan();

	}

	private void previousOrgan() {
		this.organe--;
		this.organeRoiMaganer--;
		this.editOrgan();
	}

	private void nextStep() {
		this.etape++;
		this.organe = 0;
		this.organeRoiMaganer++;
		this.firstOrientationOver = false;
		changerImage();
	}

	private void previousStep() {
		this.etape--;
		this.organe = this.organes.length - 1;
		this.organeRoiMaganer--;
		this.firstOrientationOver = true;
		changerImage();
	}

	private void nextOrientation() {
		this.firstOrientationOver = true;
		this.organe = 0;
		this.organeRoiMaganer -= this.organes.length - 1;
		this.prepareOrientation();
	}

	private void previousOrientation() {
		this.firstOrientationOver = false;
		this.organe = this.organes.length - 1;
		this.organeRoiMaganer += this.organes.length - 1;
		this.prepareOrientation();
	}

	private void capture() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
		if (this.etape == FIRST_IMAGE)
			if (firstOrientationOver)
				this.captures[CAPTURE_FIRST_ANT] = capture;
			else
				this.captures[CAPTURE_FIRST_POST] = capture;
		else if (firstOrientationOver)
			this.captures[CAPTURE_SECOND_ANT] = capture;
		else
			this.captures[CAPTURE_SECOND_POST] = capture;

	}

	private boolean allOrgansDelimited() {
		return this.organe >= this.organes.length - 1;
	}

	private void editOrgan() {
		boolean existed = false;

		existed = this.editRoi(this.position);
		if (!existed) {
			existed = this.editCopyRoi(this.organeRoiMaganer * 2 - this.organe);
		}

		if (existed)
			this.displayInstructionorgane("Adjust");
		else
			this.displayInstructionorgane("Delimit");
	}

	private void displayInstructionorgane(String type) {
		this.displayInstruction(type + " the " + this.organes[this.organe]);
	}

	@Override
	protected void end() {
		this.organe++;
		this.vue.getTextfield_instructions().setText("End!");
		this.vue.getBtn_suivant().setEnabled(false);

		// Compute model
		int firstSlice = 1;
		int secondSlice = 2;
		ImagePlus img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
		this.model.getImageSelection()[FIRST_IMAGE].getImagePlus().setSlice(firstSlice);
		this.model.getImageSelection()[SECOND_IMAGE].getImagePlus().setSlice(firstSlice);
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];

			if (i < this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(secondSlice);
			} else if (i < 3 * this.organes.length) {
				img = this.model.getImageSelection()[SECOND_IMAGE].getImagePlus();
				img.setSlice(firstSlice);
			} else {
				img = this.model.getImageSelection()[SECOND_IMAGE].getImagePlus();
				img.setSlice(secondSlice);

			}

			img.setRoi(r);
			((ModeleLympho) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculerResultats();

		new FenResultatsLympho(this, captures);

	}

	private void changerImage() {

		this.vue.setImage(this.model.getImageSelection()[this.etape].getImagePlus());
		this.vue.getImagePlus().setOverlay(Library_Gui.initOverlay(this.vue.getImagePlus()));

		// Remove overlay

		this.resetOverlay(this.vue.getImagePlus());

		// Display ant image
		if (firstOrientationOver)
			this.vue.getImagePlus().setSlice(2);
		else
			this.vue.getImagePlus().setSlice(1);
		this.editOrgan();
	}

	public void resetOverlay(ImagePlus imp) {
		imp.setOverlay(Library_Gui.initOverlay(imp));
		Library_Gui.setOverlayDG(imp, Color.YELLOW);
		Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
		Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW, 1);
		Library_Gui.setOverlayTitle("Post", this.vue.getImagePlus(), Color.YELLOW, 2);
	}

	private void prepareOrientation() {
		// Remove overlay
		this.resetOverlay();

		this.vue.setImage(this.model.getImageSelection()[this.etape].getImagePlus());
		if (firstOrientationOver)
			this.vue.getImagePlus().setSlice(2);
		else
			this.vue.getImagePlus().setSlice(1);
		this.editOrgan();
	}

}
