package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMainResult;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMethod2;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.IsotopeDialog;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.ImagePlus;

public class ControllerWorkflow_Gastric extends ControllerWorkflow {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	public static final String COMMAND_FIT_BEST_1 = "cfb_method1", COMMAND_FIT_BEST_2 = "cfb_method2",
			COMMAND_FIT_BEST_ALL = "cfb_all";

	private FenResults fenResults;
	private TabMainResult tabMain;
	private TabMethod2 tabDefaultMethod;

	private List<ImagePlus> captures;

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));

		this.generateInstructions();
		this.start();

		this.fenResults = new FenResults(this);
		this.fenResults.setVisible(false);
	}

	// TODO: remove this method and compute the model during the process
	private void computeModel() {

		// Place point 0
		getModel().activateTime0();

		ImageState previousState = null;
		for (int i = 0; i < this.getRoiManager().getRoisAsArray().length; i += 6) {
			ImageState state = null;

			for (Orientation orientation : Orientation.antPostOrder()) {
				int indexIncrementPost = 0;
				int slice = SLICE_ANT;
				if (orientation == Orientation.POST) {
					indexIncrementPost = 3;
					slice = SLICE_POST;
				}
				state = new ImageState(orientation, slice, ImageState.LAT_RL, i / 6);

				// - Stomach
				getModel().calculateCounts(Model_Gastric.REGION_STOMACH, state,
						this.getRoiManager().getRoisAsArray()[i + indexIncrementPost]);

				// - Antre
				getModel().calculateCounts(Model_Gastric.REGION_ANTRE, state,
						this.getRoiManager().getRoisAsArray()[i + 2 + indexIncrementPost]);

				// - Fundus
				getModel().calculateCounts(Model_Gastric.REGION_FUNDUS, state, null);

				// - Intestine
				getModel().calculateCounts(Model_Gastric.REGION_INTESTINE, state,
						this.getRoiManager().getRoisAsArray()[i + 1 + indexIncrementPost]);
			}

			getModel().computeStaticData(state, previousState);
			previousState = state;
		}
		this.model.calculerResultats();
	}

	private void fitBest(String command) {
		if (command.equals(COMMAND_FIT_BEST_1) || command.equals(COMMAND_FIT_BEST_ALL))
			this.tabMain.selectFit(this.tabMain.findBestFit());
		if (command.equals(COMMAND_FIT_BEST_2) || command.equals(COMMAND_FIT_BEST_ALL))
			this.tabDefaultMethod.selectFit(this.tabDefaultMethod.findBestFit());
	}

	@Override
	protected void start() {
		// Find isotope
		String isotopeCode = Library_Dicom.findIsotopeCode(getModel().getImagePlus());
		if (isotopeCode == null) {
			// No code
			// Ask user for isotope
			IsotopeDialog isotopeDialog = new IsotopeDialog(vue);
			isotopeDialog.setVisible(true);
		}
		Isotope isotope = Isotope.getIsotopeFromCode(isotopeCode);
		if (isotope == null) {
			// Code unknown
			// Ask user for isotope
			IsotopeDialog isotopeDialog = new IsotopeDialog(vue, isotopeCode);
			isotopeDialog.setVisible(true);
			isotope = isotopeDialog.getIsotope();
		}
		getModel().setIsotope(isotope);

		super.start();
	}

	@Override
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Display results
		this.tabMain = new TabMainResult(this.fenResults, this.captures.get(1), this);
		this.tabMain.displayTimeIngestion(getModel().getTimeIngestion());

		this.tabDefaultMethod = new TabMethod2(this.fenResults, this.captures.get(0), this);

		// Set the best fit by default
		this.fitBest(COMMAND_FIT_BEST_ALL);

		this.fenResults.clearTabs();
		this.fenResults.setMainTab(this.tabMain);
		this.fenResults.addTab(tabDefaultMethod);
		this.fenResults.pack();
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;

		this.captures = new ArrayList<>(1);

		// First instruction to get the acquisition time for the starting point
		PromptIngestionTime promptIngestionTime = new PromptIngestionTime(this);
		PromptInstruction promptTimeAcquisition = new PromptInstruction(promptIngestionTime);
		if (promptIngestionTime.isInputValid())
			this.getModel().setTimeIngestion(promptIngestionTime.getResult());

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.getModel().getImageSelection()[i]);

			ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_NONE);
			ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_NONE);

			dri_1 = new DrawRoiInstruction("Stomach", stateAnt, dri_3);
			dri_2 = new DrawRoiInstruction("Intestine", stateAnt, dri_4);
			dri_3 = new DrawRoiInstruction("Stomach", statePost, dri_1);
			dri_4 = new DrawRoiInstruction("Intestine", statePost, dri_2);

			if (i == 0)
				this.workflows[i].addInstruction(promptTimeAcquisition);
			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
			this.workflows[i].addInstruction(dri_3);
			// Capture 1: only stomach, for method 2
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(captures, vue, 0, 640, 512));
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4, "Antre"));
			// Capture 2: stomach and intestine, for toulouse method
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 1, 640, 512));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	@Override
	public void clicPrecedent() {
		super.clicPrecedent();
		this.fenResults.setVisible(false);
	}

	@Override
	public Model_Gastric getModel() {
		return (Model_Gastric) this.model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		// Auto-fit
		if (e.getSource() instanceof JButton) {
			JButton source = (JButton) e.getSource();
			this.fitBest(source.getActionCommand());
		}
	}

}