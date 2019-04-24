package org.petctviewer.scintigraphy.shunpo;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControllerShunpo extends ControleurScin {

	private final boolean FIRST_ORIENTATION_POST;

	private static final int SLICE_ANT = 1, SLICE_POST = 2;
	private static final int INDEX_ORGAN_AUTO_GENERATE_ROI = 4;
	private static final int STEP_KIDNEY_LUNG = 0, STEP_BRAIN = 1;
	private String[][] steps = { { "Right lung", "Left lung", "Right kidney", "Left kidney", "Background" },
			{ "Brain" } };

	/**
	 * Index of the step for the current state
	 */
	private int currentStep;
	/**
	 * Index of the organ for the current step
	 */
	private int currentOrgan;
	/**
	 * Index of the organ in the RoiManager
	 */
	private int indexRoi;
	private boolean firstOrientationOver;

	private static final int CAPTURE_ANT_KIDNEY_LUNG = 0, CAPTURE_POST_KIDNEY_LUNG = 1, CAPTURE_ANT_BRAIN = 2,
			CAPTURE_POST_BRAIN = 3, TOTAL_CAPTURES = 4;
	private ImagePlus[] captures;

	private FenResults fenResults;

	/**
	 * @param vue
	 * @param images 2 images, 1st: KIDNEY-PULMON; 2nd: BRAIN
	 */
	public ControllerShunpo(Scintigraphy main, FenApplication vue, ImageSelection[] images, String studyName) {
		super(main, vue, new ModeleShunpo(images, studyName));
		this.FIRST_ORIENTATION_POST = true;
		this.currentStep = STEP_KIDNEY_LUNG;
		this.firstOrientationOver = false;
		this.indexRoi = 0;
		this.currentOrgan = 0;

		this.captures = new ImagePlus[TOTAL_CAPTURES];

		this.fenResults = new FenResults(this.model);
		this.fenResults.setVisible(false);

		// Start working with kidney-pulmon
		this.prepareOrientation();
	}

//	private final void DEBUG() {
//		this.DEBUG(null);
//	}
//
//	private final void DEBUG(String location) {
//		if (location != null)
//			System.out.println("== " + location.toUpperCase() + " ==");
//		System.out.println("Current orientation: " + (this.isNowPost() ? "POST" : "ANT"));
//		System.out.println(
//				"Current step: " + this.currentStep + " [" + (this.currentStep == 0 ? "PULMON_KIDNEY" : "BRAIN") + "]");
//		System.out.println(
//				"Current organ: " + this.currentOrgan + " [" + this.steps[this.currentStep][this.currentOrgan] + "]");
//		System.out.println("Index ROI: " + this.indexRoi);
//		System.out.println("Position: " + this.position);
//		if (location == null)
//			System.out.println("==============");
//		System.out.println();
//	}

	/**
	 * Displays the current organ's instruction type.<br>
	 * Instruction is of the form: 'TYPE the CURRENT_ORGAN'.<br>
	 * example: 'Delimit the Right Kidney'.
	 * 
	 * @param type Type of the instruction (expecting 'Delimit' or 'Adjust')
	 */
	private void displayInstructionCurrentOrgan(String type) {
		this.displayInstruction(type + " the " + this.steps[this.currentStep][this.currentOrgan]);
	}

	/**
	 * This method will set the right image (Ant/Post orientation) for the specified
	 * step and place the ROI to edit if necessary.
	 */
	private void prepareOrientation() {
		// Remove overlay
		this.resetOverlay();

		this.vue.setImage(this.model.getImageSelection()[this.currentStep].getImagePlus());
		if (this.isNowPost()) {
			// Display post image
			this.vue.getImagePlus().setSlice(2);
		} else {
			// Display ant image
			this.vue.getImagePlus().setSlice(1);
		}

		this.editOrgan();
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
			existed = this.editCopyRoi(this.indexRoi * 2 - this.currentOrgan);
		}

		if (existed)
			this.displayInstructionCurrentOrgan("Adjust");
		else
			this.displayInstructionCurrentOrgan("Delimit");
	}

	/**
	 * @return TRUE if all of the organs of the currentStep are delimited and FALSE
	 *         if organs remain
	 */
	private boolean allOrgansDelimited() {
		return this.currentOrgan >= this.steps[this.currentStep].length - 1;
	}

	@Override
	protected void end() {
		this.currentOrgan++;
		super.end();

		// Compute model
		int firstSlice = (this.FIRST_ORIENTATION_POST ? SLICE_POST : SLICE_ANT);
		int secondSlice = firstSlice % 2 + 1;
		ImagePlus img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
		this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus().setSlice(firstSlice);
		this.model.getImageSelection()[STEP_BRAIN].getImagePlus().setSlice(firstSlice);
		for (int i = 0; i < this.model.getRoiManager().getRoisAsArray().length; i++) {
			String title_completion = "";
			Roi r = this.model.getRoiManager().getRoisAsArray()[i];
			int organ = 0;

			if (i < this.steps[STEP_KIDNEY_LUNG].length) {
				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
				img.setSlice(firstSlice);
				title_completion += " {KIDNEY_LUNG}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i * 2 + 1;
				else
					organ = i * 2;
			} else if (i < 2 * this.steps[STEP_KIDNEY_LUNG].length) {
				img = this.model.getImageSelection()[STEP_KIDNEY_LUNG].getImagePlus();
				img.setSlice(secondSlice);
				title_completion += " {KIDNEY_LUNG}";
				if (this.FIRST_ORIENTATION_POST)
					organ = (i - this.steps[STEP_KIDNEY_LUNG].length) * 2;
				else
					organ = (i - this.steps[STEP_KIDNEY_LUNG].length) * 2 + 1;
			} else if (i - 2 * this.steps[STEP_KIDNEY_LUNG].length < this.steps[STEP_BRAIN].length) {
				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
				img.setSlice(firstSlice);
				title_completion += " {BRAIN}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i + 1;
				else
					organ = i;
			} else {
				img = this.model.getImageSelection()[STEP_BRAIN].getImagePlus();
				img.setSlice(secondSlice);
				title_completion += " {BRAIN}";
				if (this.FIRST_ORIENTATION_POST)
					organ = i - 1;
				else
					organ = i;
			}

			if (img.getCurrentSlice() == SLICE_ANT)
				title_completion += " ANT";
			else
				title_completion += " POST";

			System.out.println("Oppening:: " + r.getName() + title_completion);
			img.setRoi(r);
			((ModeleShunpo) this.model).calculerCoups(organ, img);
		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults.setMainTab(new TabResult(fenResults, "Result", true) {
			@Override
			public Component getSidePanelContent() {
				String[] result = ((ModeleShunpo) model).getResult();
				JPanel res = new JPanel(new GridLayout(result.length, 1));
				for (String s : result)
					res.add(new JLabel(s));
				return res;
			}

			@Override
			public JPanel getResultContent() {
				return new DynamicImage(montage.getImage());
			}
		});
		this.fenResults.pack();
		this.fenResults.setVisible(true);

	}

	/**
	 * @return TRUE if the current position is in post orientation
	 */
	private boolean isNowPost() {
		return !(FIRST_ORIENTATION_POST && this.firstOrientationOver)
				|| (!FIRST_ORIENTATION_POST && !this.firstOrientationOver);
	}

	private void capture() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
		if (this.currentStep == STEP_KIDNEY_LUNG)
			if (this.isNowPost())
				this.captures[CAPTURE_POST_KIDNEY_LUNG] = capture;
			else
				this.captures[CAPTURE_ANT_KIDNEY_LUNG] = capture;

		else if (this.isNowPost())
			this.captures[CAPTURE_POST_BRAIN] = capture;
		else
			this.captures[CAPTURE_ANT_BRAIN] = capture;

	}

	private void nextStep() {
		this.currentStep++;
		this.currentOrgan = 0;
		this.indexRoi++;
		this.firstOrientationOver = false;
		this.prepareOrientation();
	}

	private void previousStep() {
		this.currentStep--;
		this.currentOrgan = this.steps[this.currentStep].length - 1;
		this.indexRoi--;
		this.firstOrientationOver = true;
		this.prepareOrientation();
	}

	private void nextOrientation() {
		this.firstOrientationOver = true;
		this.currentOrgan = 0;
		this.indexRoi -= this.steps[this.currentStep].length - 1;
		this.prepareOrientation();
	}

	private void previousOrientation() {
		this.firstOrientationOver = false;
		this.currentOrgan = this.steps[this.currentStep].length - 1;
		this.indexRoi += this.steps[this.currentStep].length - 1;
		this.prepareOrientation();
	}

	private void nextOrgan() {
		this.currentOrgan++;
		this.indexRoi++;
		if (this.currentOrgan == INDEX_ORGAN_AUTO_GENERATE_ROI)
			// Auto generate ROI
			this.vue.getImagePlus().setRoi(this.roiBetween(this.model.getRoiManager().getRoi(this.position - 2),
					this.model.getRoiManager().getRoi(this.position - 1)));
		else
			this.editOrgan();

	}

	private void previousOrgan() {
		this.currentOrgan--;
		this.indexRoi--;
		this.editOrgan();
	}

	@Override
	public void clicSuivant() {
		try {
			this.saveRoiAtIndex(this.steps[this.currentStep][this.currentOrgan] + (this.isNowPost() ? "_P" : "_A"), this.position);

			this.displayRoi(this.position);
			super.clicSuivant();

			if (this.allOrgansDelimited()) {
				this.capture();
				if (this.firstOrientationOver)
					if (this.currentStep == this.steps.length - 1)
						this.end();
					else
						this.nextStep();
				else
					this.nextOrientation();
			} else
				this.nextOrgan();
		} catch (NoDataException e) {
			JOptionPane.showMessageDialog(this.vue, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		if (this.currentOrgan == 0)
			if (!this.firstOrientationOver)
				this.previousStep();
			else
				this.previousOrientation();

		else
			this.previousOrgan();

		this.displayRois(this.position - this.currentOrgan, this.position);

		this.fenResults.setVisible(false);
	}

	@Override
	public boolean isOver() {
		return this.currentStep == this.steps.length - 1 && this.firstOrientationOver
				&& this.currentOrgan == this.steps[this.currentStep].length - 1;
	}

}