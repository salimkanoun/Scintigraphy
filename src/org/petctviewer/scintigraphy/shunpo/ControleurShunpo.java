package org.petctviewer.scintigraphy.shunpo;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;

public class ControleurShunpo extends ControleurScin {

	private enum State {
		DELIMIT_ORGAN_ANT, DELEMIT_ORGAN_POST, END;
	}

	private static final int INDEX_ORGAN_AUTO_GENERATE_ROI = 4;
	private static final int STEP_KIDNEY_PULMON = 0;
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

	private ImagePlus[] captures;

	private FenResults fenResults;

	/**
	 * @param vue
	 * @param images 2 images, 1st: KIDNEY-PULMON; 2nd: BRAIN
	 */
	public ControleurShunpo(Scintigraphy main, FenApplication vue, ImageSelection[] images, String studyName) {
		super(main, vue, new ModeleShunpo(images, studyName));
		this.state = State.DELIMIT_ORGAN_ANT;
		this.currentStep = STEP_KIDNEY_PULMON;
		this.indexOrgan = 0;
		this.currentOrgan = 0;

		this.captures = new ImagePlus[4];

		// Start working with kidney-pulmon
		this.prepareStep(this.currentStep);
	}

	/*
	 * private final void DEBUG() { System.out.println("Current state: " +
	 * this.state); System.out.println( "Current step: " + this.currentStep + " [" +
	 * (this.currentStep == 0 ? "PULMON_KIDNEY" : "BRAIN") + "]");
	 * System.out.println( "Current organ: " + this.currentOrgan + " [" +
	 * this.steps[this.currentStep][this.currentOrgan] + "]");
	 * System.out.println("Index organ: " + this.indexOrgan);
	 * System.out.println("Position: " + this.position); System.out.println(); }
	 */

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

		this.vue.setImp(this.model.getImageSelection()[step].getImagePlus());
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
			this.vue.getImagePlus().setRoi(this.roiBetween(this.model.getRoiManager().getRoi(indexOrgan - 2),
					this.model.getRoiManager().getRoi(indexOrgan - 1)));
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

		// Compute model
		int index = 0;
		ImagePlus img = this.model.getImageSelection()[0].getImagePlus();
		this.model.getImageSelection()[0].getImagePlus().setSlice(1);
		this.model.getImageSelection()[1].getImagePlus().setSlice(1);
		for (Roi r : this.model.getRoiManager().getRoisAsArray()) {
			String title_completion = "";
			if (index >= ModeleShunpo.TOTAL_ORGANS) {
				// POST
				this.model.getImageSelection()[0].getImagePlus().setSlice(2);
				this.model.getImageSelection()[1].getImagePlus().setSlice(2);
				index = 1;
			}
			if (index % 2 == 0)
				title_completion += " ANT(" + img.getCurrentSlice() + ")";
			else
				title_completion += " POST(" + img.getCurrentSlice() + ")";

			if (index >= ModeleShunpo.BRAIN_ANT) {
				// BRAIN IMG
				img = this.model.getImageSelection()[1].getImagePlus();
				title_completion += " - BRAIN_IMG";
			} else {
				img = this.model.getImageSelection()[0].getImagePlus();
				title_completion += " - KIDNEY-PULMON_IMG";
			}
			img.setRoi(r);
			System.out.println("Oppening:: " + r.getName() + title_completion);
			((ModeleShunpo) this.model).calculerCoups(index, img);
			index += 2;
		}
		this.model.calculerResultats();

		// Save captures
		ImageStack stackCapture = Library_Capture_CSV.captureToStack(this.captures);
		ImagePlus montage = this.montage(stackCapture);

		// Display result
		this.fenResults = new FenResults(this.model);
		this.fenResults.addTab(new TabResult(fenResults, "Result", true) {
			@Override
			public Component getSidePanelContent() {
				String[] result = ((ModeleShunpo) model).getResult();
				JPanel res = new JPanel(new GridLayout(result.length, 1));
				for(String s : result)
					res.add(new JLabel(s));
				return res;
			}
			@Override
			public JPanel getResultContent() {
				return new DynamicImage(montage.getImage());
			}
		});
		this.fenResults.setVisible(true);
		
		
	}

	@Override
	public void clicSuivant() {
		if (this.saveCurrentRoi(this.steps[this.currentStep][this.currentOrgan]
				+ (this.state == State.DELIMIT_ORGAN_ANT ? "_A" : "_P"))) {
			super.clicSuivant();

			this.displayRoi(this.position - 1);
			this.indexOrgan++;

			// All organs delimited
			if (this.allOrgansDelimited()) {
				// Capture
				int indexCapture = this.currentStep + (this.state == State.DELEMIT_ORGAN_POST ? this.steps.length : 0);
				this.captures[indexCapture] = Library_Capture_CSV.captureImage(this.vue.getImagePlus(), 512, 0);
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
		}
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();

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
	}

	@Override
	public boolean isOver() {
		return this.state == State.END;
	}

}
