package org.petctviewer.scintigraphy.shunpo;

import java.awt.Rectangle;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class ControleurShunpo extends ControleurScin {

	private enum State {
		DELIMIT_ORGAN_ANT, DELEMIT_ORGAN_POST, END;
	}

	private static final int INDEX_ORGAN_AUTO_GENERATE_ROI = 4;
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
	private int indexOrgan;

	private State state;
	private ImageSelection[] images;

	private ModeleScin model;
	private ImagePlus[] captures;

	private FenResults fenResults;

	/**
	 * @param vue
	 * @param images 2 images, 1st: KIDNEY-PULMON; 2nd: BRAIN
	 */
	public ControleurShunpo(Scintigraphy main, FenApplication vue, ImageSelection[] images) {
		super(main, vue);
		this.images = images;
		this.state = State.DELIMIT_ORGAN_ANT;
		this.currentStep = 0;
		this.indexOrgan = 0;
		this.currentOrgan = 0;

		this.model = new ModeleShunpo();
		this.captures = new ImagePlus[4];
		new RoiManager();

		// Start working with kidney-pulmon
		this.prepareStep(this.currentStep);
	}

	private final void DEBUG() {
		System.out.println("Current state: " + this.state);
		System.out.println(
				"Current step: " + this.currentStep + " [" + (this.currentStep == 0 ? "PULMON_KIDNEY" : "BRAIN") + "]");
		System.out.println(
				"Current organ: " + this.currentOrgan + " [" + this.steps[this.currentStep][this.currentOrgan] + "]");
		System.out.println("Index organ: " + this.indexOrgan);
		System.out.println("Position: " + this.position);
		System.out.println();
	}

	/**
	 * Creates a rectangle between the two ROIs specified.
	 * 
	 * @param r1
	 * @param r2
	 * @return Rectangle at the center of the ROIs specified
	 */
	private Rectangle roiBetween(Roi r1, Roi r2) {
		int x = (int) ((r1.getBounds().getLocation().x + r2.getBounds().getLocation().x + r2.getBounds().getWidth())
				/ 2);
		int y = (r1.getBounds().getLocation().y + r2.getBounds().getLocation().y) / 2;
		return new Rectangle(x, y, 15, 30);
	}

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
	 * 
	 * @param step Step to prepare (0 <= step < this.steps.length)
	 */
	private void prepareStep(int step) {
		// Remove overlay
		this.resetOverlay();

		this.vue.setImp(this.images[step].getImagePlus());
		if (this.state == State.DELEMIT_ORGAN_POST) {
			// Display post image
			this.vue.getImagePlus().setSlice(2);
		} else {
			this.vue.getImagePlus().setSlice(1);
		}

		this.editOrgan();
	}

	/**
	 * This method will reset the counters: currentStep, currentOrgan, indexOrgan
	 * and change the state to the specified state. Also, it will prepare the step.
	 * 
	 * @param state State to prepare
	 */
	private void prepareState(State state) {
		this.currentStep = 0;
		this.currentOrgan = 0;
		this.indexOrgan = 0;

		this.state = state;

		this.prepareStep(this.currentOrgan);
	}

	/**
	 * This method displays the organ to edit (if necessary) and the instruction for
	 * the user.
	 * 
	 * @param indexOrgan
	 */
	private void editOrgan() {
		boolean existed = false;
		if (this.state == State.DELIMIT_ORGAN_ANT) {
			existed = this.editRoi(this.indexOrgan);
		} else {
			existed = this.editRoi(this.position);
			if (!existed) {
				existed = this.editCopyRoi(this.indexOrgan);
			}
		}

		if (existed)
			this.displayInstructionCurrentOrgan("Adjust");
		else
			this.displayInstructionCurrentOrgan("Delimit");
	}

	private void nextOrgan() {
		this.currentOrgan++;
		if (this.currentOrgan == INDEX_ORGAN_AUTO_GENERATE_ROI)
			// Auto generate ROI
			this.vue.getImagePlus().setRoi(
					this.roiBetween(this.roiManager.getRoi(indexOrgan - 2), this.roiManager.getRoi(indexOrgan - 1)));
		else
			this.editOrgan();

	}

	private void previousOrgan() {
		this.currentOrgan--;
		this.editOrgan();
	}

	/**
	 * @return TRUE if all of the organs of the currentStep are delimited and FALSE
	 *         if organs remain
	 */
	private boolean allOrgansDelimited() {
		return this.currentOrgan >= this.steps[this.currentStep].length - 1;
	}

	/**
	 * @return TRUE if all of the steps are completed and FALSE if steps remain
	 */
	private boolean allStepsCompleted() {
		return this.currentStep == this.steps.length;
	}

	private int nbTotalOrgans() {
		int tot = 0;
		for (String[] organs : this.steps)
			tot += organs.length;
		return tot;
	}

	@Override
	protected void end() {
		this.state = State.END;
		this.currentOrgan = 0;
		this.currentStep = 0;
		this.vue.getTextfield_instructions().setText("End!");
		this.vue.getBtn_suivant().setEnabled(false);

		// Save model
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults = new FenResults("Results", this.main.getExamType());
		this.fenResults.setResult(new DynamicImage(montage.getImage()));
		this.fenResults.setInfos(new SidePanel(null, this.main.getExamType(), this.images[0].getImagePlus()));
		this.fenResults.setVisible(true);
	}

	@Override
	public void clicSuivant() {
		if (this.saveCurrentRoi(this.steps[this.currentStep][this.currentOrgan]
				+ (this.state == State.DELIMIT_ORGAN_ANT ? "_A" : "_P"))) {
			super.clicSuivant();
			DEBUG();
			this.displayRoi(this.position - 1);
			this.indexOrgan++;

			// All organs delimited
			if (this.allOrgansDelimited()) {
				// Capture
				int indexCapture = this.currentStep + (this.state == State.DELEMIT_ORGAN_POST ? this.steps.length : 0);
				this.captures[indexCapture] = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 0, 0);
				// Next step
				this.currentStep++;
				// All steps completed
				if (this.allStepsCompleted()) {
					switch (this.state) {
					case DELIMIT_ORGAN_ANT:
						// Next state
						this.prepareState(State.DELEMIT_ORGAN_POST);
						break;
					case DELEMIT_ORGAN_POST:
						// End
						this.end();
						break;
					default:
						break;
					}
				} else {
					this.currentOrgan = 0;
					this.prepareStep(this.currentStep);
				}
			}
			// There is organs to be delimited
			else {
				// Next organ
				this.nextOrgan();
			}
			DEBUG();
		}
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

		DEBUG();
		this.indexOrgan--;
		if (this.currentOrgan > 0) {
			this.previousOrgan();
		} else if (this.currentStep > 0) {
			// Previous step
			this.currentStep--;
			this.currentOrgan = this.steps[this.currentStep].length - 1;
			this.prepareStep(currentStep);
		} else {
			// Previous state
			this.currentStep = this.steps.length - 1;
			this.currentOrgan = this.steps[this.currentStep].length - 1;
			switch (this.state) {
			case END:
				this.fenResults.dispose();
				this.state = State.DELEMIT_ORGAN_POST;
				break;
			case DELEMIT_ORGAN_POST:
				this.state = State.DELIMIT_ORGAN_ANT;
				this.indexOrgan = this.nbTotalOrgans() - 1;
				break;
			default:
				break;
			}
			this.prepareStep(currentStep);
		}

		this.displayRois(this.position - this.currentOrgan, this.position);
		DEBUG();
	}

	@Override
	public boolean isOver() {
		return this.state == State.END;
	}

}
