package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;

public class ControleurShunpo extends ControleurScin {

	private enum State {
		DELIMIT_ORGAN_ANT, DELEMIT_ORGAN_POST, END;
	}

	private static final int STEP_PULMON_KIDNEY = 0, STEP_BRAIN = 1;
	private String[][] steps = { { "Right lung", "Left lung", "Right kidney", "Left kidney", "Background" },
			{ "Brain", "Brain_2", "Brain_3" } };

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

	private Modele_Shunpo model;
	private ImagePlus[] captures;

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

		this.model = new Modele_Shunpo();
		this.captures = new ImagePlus[4];

		// Start working with kidney-pulmon
		this.prepareStep(this.currentStep);
	}

	private final void DEBUG() {
		System.out.println("Index organ: " + this.indexOrgan);
		System.out.println(
				"Current organ: " + this.currentOrgan + " [" + this.steps[this.currentStep][this.currentOrgan] + "]");
		System.out.println(
				"Current step: " + this.currentStep + " [" + (this.currentStep == 0 ? "PULMON_KIDNEY" : "BRAIN") + "]");
		System.out.println("Current state: " + this.state);
		System.out.println();
	}

	private void displayInstruction(String instruction) {
		this.vue.getTextfield_instructions().setText(instruction);
		this.vue.pack();
	}

	private void displayInstructionCurrentOrgan(String type) {
		this.displayInstruction(type + " the " + this.steps[this.currentStep][this.currentOrgan]);
	}

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
		this.editIndexOrgan();
	}

	private void prepareState(State state) {
		this.currentStep = 0;
		this.currentOrgan = 0;
		this.indexOrgan = 0;

		this.state = state;

		this.prepareStep(this.currentOrgan);
	}

	private void editIndexOrgan() {
		if (this.editRoi(this.indexOrgan))
			this.displayInstructionCurrentOrgan("Adjust");
		else
			this.displayInstructionCurrentOrgan("Delimit");
	}

	private void nextOrgan() {
		this.currentOrgan++;
		this.editIndexOrgan();
	}

	private void previousOrgan() {
		this.currentOrgan--;
		this.editIndexOrgan();
	}

	private boolean allOrgansDelimited() {
		return this.currentOrgan >= this.steps[this.currentStep].length - 1;
	}

	private boolean allStepsCompleted() {
		return this.currentStep == this.steps.length;
	}

	private void end() {
		this.state = State.END;
		this.currentOrgan = 0;
		this.currentStep = 0;
		this.vue.getTextfield_instructions().setText("End!");
		this.vue.getBtn_suivant().setEnabled(false);

		// Save model
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		this.model.montage(stackCapture, this.main.getExamType());
		
	}

	@Override
	public void clicSuivant() {
		if (this.saveCurrentRoi(this.steps[this.currentStep][this.currentOrgan])) {
			super.clicSuivant();
			this.displayRoi(this.indexOrgan);
			System.out.println("Displaying: " + this.indexOrgan);
			this.indexOrgan++;

			switch (this.state) {
			case DELIMIT_ORGAN_ANT:
				// All organs delimited
				if (this.allOrgansDelimited()) {
					// Capture
					this.captures[this.currentStep] = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512,
							512);
					// Next step
					this.currentStep++;
					// All steps completed
					if (this.allStepsCompleted()) {
						// Next state
						this.prepareState(State.DELEMIT_ORGAN_POST);
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
				break;

			case DELEMIT_ORGAN_POST:
				// All organs delimited
				if (this.allOrgansDelimited()) {
					// Capture
					this.captures[this.currentStep + this.steps.length] = Library_Capture_CSV
							.captureImage(this.vue.getImagePlus(), 512, 512);
					// Next step
					this.currentStep++;
					// All steps completed
					if (this.allStepsCompleted()) {
						// End
						this.end();
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
				break;

			case END:
				break;
			}

			this.DEBUG();
		}
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();
		System.out.println("== PREVIOUS ==");

		this.indexOrgan--;
		this.DEBUG();
		if (this.currentOrgan > 0) {
			// Previous organ
			System.out.println("-> Previous organ");
			this.previousOrgan();
			this.DEBUG();
		} else if (this.currentStep > 0) {
			System.out.println("-> Previous step");
			// Previous step
			this.currentStep--;
			this.currentOrgan = this.steps[this.currentStep].length - 1;
			this.prepareStep(currentStep);
			this.DEBUG();
		} else {
			System.out.println("-> Previous state");
			// Previous state
			this.currentStep = this.steps.length - 1;
			this.currentOrgan = this.steps[this.currentStep].length - 1;
			switch (this.state) {
			case END:
				this.state = State.DELEMIT_ORGAN_POST;
				break;
			case DELEMIT_ORGAN_POST:
				this.state = State.DELIMIT_ORGAN_ANT;
				this.indexOrgan = this.getIndexLastRoi();
				break;
			default:
				break;
			}
			this.prepareStep(currentStep);
			this.DEBUG();
		}

		System.out.println("Displaying: " + (this.indexOrgan - this.currentOrgan) + " to " + this.indexOrgan);
		this.displayRois(this.indexOrgan - this.currentOrgan, this.indexOrgan);
		System.out.println("Editing: " + this.indexOrgan);
		this.editRoi(this.indexOrgan);
	}

	@Override
	public boolean isOver() {
		return this.state == State.END;
	}

}
