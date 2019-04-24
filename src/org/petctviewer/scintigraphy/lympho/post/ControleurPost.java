package org.petctviewer.scintigraphy.lympho.post;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.lympho.gui.TabPost;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControleurPost extends ControleurScin {

	public String[] organes = { "Pelvis_Right", "Pelvis_Left","Background" };

	private static final int FIRST_IMAGE = 0, SECOND_IMAGE = 1;

	private int organe;

	private int organeRoiMaganer;

	private int etape;

	private static final int CAPTURE_ANT = 0, CAPTURE_POST = 1, TOTAL_CAPTURES = 2;

	private ImagePlus[] captures;

	private FenResults fenResults;

	private boolean firstOrientationOver;
	
	private TabResult resultTab;

	public ControleurPost(Scintigraphy main, FenApplication vue, ModeleScin model) {
		super(main, vue, model);
		// TODO Auto-generated constructor stub
	}

	public ControleurPost(Scintigraphy main, FenApplication vue, String examType, ImageSelection[] selectedImages, TabResult resultTab) {
		super(main, vue, new ModelePost(selectedImages, examType, resultTab));

		this.organe = 0;
		this.organeRoiMaganer = 0;
		this.firstOrientationOver = false;
		etape = 0;

		this.captures = new ImagePlus[TOTAL_CAPTURES];

		this.fenResults = new FenResults(this.model);
		this.fenResults.setVisible(false);
		
		this.changerImage();
		this.resultTab = resultTab;

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
					this.end();
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

		this.displayRois(this.position - this.organe, this.position);

		this.fenResults.setVisible(false);
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
		if (firstOrientationOver)
			this.captures[CAPTURE_ANT] = capture;
		else
			this.captures[CAPTURE_POST] = capture;
		;

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

	private final void DEBUG(String location) {
		if (location != null)
			System.out.println("== " + location.toUpperCase() + " ==");
		System.out
				.println("Current step: " + this.etape + " [" + (this.etape == 0 ? "FirstImage" : "SecondImage") + "]");
		System.out.println("Current organ: " + this.organe + " [" + this.organes[this.organe] + "]");
		System.out.println("Index ROI: " + this.organeRoiMaganer);
		System.out.println("Position: " + this.position);
		if (location == null)
			System.out.println("==============");
		System.out.println();
	}

	private final void DEBUG() {
		this.DEBUG(null);
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
		int organ = 0;
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {

			Roi r = this.model.getRoiManager().getRoisAsArray()[i];

			if (i < this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(firstSlice);

			} else if (i < 2 * this.organes.length) {
				img = this.model.getImageSelection()[FIRST_IMAGE].getImagePlus();
				img.setSlice(secondSlice);
			}

			img.setRoi(r);
			((ModelePost) this.model).calculerCoups(organ, img);
			organ++;

		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		ImagePlus montage = this.montage(stackCapture);

//		// Display result
//		this.fenResults.setMainTab(new TabResult(fenResults, "Result", true) {
//			@Override
//			public Component getSidePanelContent() {
//				String[] result = ((ModelePost) model).getResult();
//				JPanel res = new JPanel(new GridLayout(result.length, 1));
//				for (String s : result)
//					res.add(new JLabel(s));
//				return res;
//			}
//
//			@Override
//			public JPanel getResultContent() {
//				return new DynamicImage(montage.getImage());
//			}
//		});
//		this.fenResults.pack();
//		this.fenResults.setVisible(true);
		
		((ModelePost)this.model).setPelvisMontage(montage);
		montage.show();
		((TabPost)this.resultTab).setExamDone(true);
		((TabPost)this.resultTab).reloadDisplay();

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
	
	
	public ModeleScin getModel() {
		return this.model;
	}

}
