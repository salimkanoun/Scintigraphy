package org.petctviewer.scintigraphy.gastric;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.petctviewer.scintigraphy.gastric.tabs.TabMainResult;
import org.petctviewer.scintigraphy.gastric.tabs.TabMethod1;
import org.petctviewer.scintigraphy.gastric.tabs.TabMethod2;
import org.petctviewer.scintigraphy.gastric.tabs.TabMethod2_bis;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
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
import org.petctviewer.scintigraphy.scin.preferences.PrefsTabGastric;

import ij.ImagePlus;
import ij.Prefs;

public class ControllerWorkflow_Gastric extends ControllerWorkflow {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	public static final String COMMAND_FIT_BEST_1 = "cfb_method1", COMMAND_FIT_BEST_2 = "cfb_method2",
			COMMAND_FIT_BEST_ALL = "cfb_all";

	private FenResults fenResults;
	private TabMethod1 tabMain;
	private TabMethod2_bis tabOnlyGastric;

	private List<ImagePlus> captures;

	private final boolean DO_ONLY_GASTRIC;

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplicationWorkflow vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));

		getModel().setFirstImage(selectedImages[0]);

		// Check do only gastric (from Prefs)
		DO_ONLY_GASTRIC = Prefs.get(PrefsTabGastric.PREF_SIMPLE_METHOD, false);

		this.generateInstructions();
		this.start();

		this.fenResults = new FenResults(this);
		this.fenResults.setVisible(false);
	}

	private void computeOnlyGastric() {
		// Point 0
		getModel().setTimeIngestion(getModel().getFirstImage().getDateAcquisition());

		for (int i = 0; i < this.getRoiManager().getRoisAsArray().length; i += 2) {
			ImageState antState = new ImageState(Orientation.ANT, SLICE_ANT, ImageState.LAT_RL, i / 2);
			ImageState postState = new ImageState(Orientation.POST, SLICE_POST, ImageState.LAT_RL, i / 2);

			// - Stomach
			getModel().calculateCounts(Model_Gastric.REGION_STOMACH, antState,
					this.getRoiManager().getRoisAsArray()[i]);
			getModel().calculateCounts(Model_Gastric.REGION_STOMACH, postState,
					this.getRoiManager().getRoisAsArray()[i + 1]);

			getModel().computeStaticData(antState, postState);
		}
		this.model.calculerResultats();
	}

	private void computeBothMethods() {
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

	// TODO: remove this method and compute the model during the process
	private void computeModel() {
		if (DO_ONLY_GASTRIC) {
			this.computeOnlyGastric();
		} else {
			this.computeBothMethods();
		}
	}

	private void fitBest(String command) {
		if (command.equals(COMMAND_FIT_BEST_1) || command.equals(COMMAND_FIT_BEST_ALL))
			this.tabMain.selectFit(this.tabMain.findBestFit());
		if (command.equals(COMMAND_FIT_BEST_2) || command.equals(COMMAND_FIT_BEST_ALL))
			this.tabOnlyGastric.selectFit(this.tabOnlyGastric.findBestFit());
	}

	private void generateInstructionsOnlyGastric() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1 = null, dri_2 = null;

		this.captures = new ArrayList<>(1);

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.getModel().getImageSelection()[i]);

			ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_NONE);
			ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_NONE);

			dri_1 = new DrawRoiInstruction("Stomach", stateAnt, dri_2);
			dri_2 = new DrawRoiInstruction("Stomach", statePost, dri_1);

			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(captures, vue, 0, 640, 512));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	private void generateInstructionsBothMethods() {
		this.workflows = new Workflow[this.model.getImageSelection().length];

		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;

		this.captures = new ArrayList<>(2);

		// First instruction to get the acquisition time for the starting point
		PromptIngestionTime promptIngestionTime = new PromptIngestionTime(this);
		PromptInstruction promptTimeAcquisition = new PromptInstruction(promptIngestionTime);
		if (promptIngestionTime.isInputValid())
			this.getModel().setTimeIngestion(promptIngestionTime.getResult());

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.getModel().getImageSelection()[i]);

			ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_WORKFLOW);
			ImageState statePost = new ImageState(Orientation.POST, 2, ImageState.LAT_RL, ImageState.ID_WORKFLOW);

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
				this.workflows[i].addInstruction(new ScreenShotInstruction(captures, vue, 0, 0, 0));
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4, "Antre"));
			// Capture 2: stomach and intestine, for method 1
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 1, 0, 0));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
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
		this.fenResults.clearTabs();

		if (!DO_ONLY_GASTRIC) {
//			this.tabMain = new TabMainResult(this.fenResults, this.captures.get(1), this);
			this.tabMain = new TabMethod1(this.fenResults, this.captures.get(1), this);
			this.tabMain.displayTimeIngestion(getModel().getTimeIngestion());
			this.fenResults.addTab(tabMain);
			// Select best fit
			this.fitBest(COMMAND_FIT_BEST_1);
		}

//		this.tabOnlyGastric = new TabMethod2(this.fenResults, this.captures.get(0), this);
		this.tabOnlyGastric = new TabMethod2_bis(this.fenResults, this.captures.get(0), this);
		this.fenResults.addTab(tabOnlyGastric);
		// Set the best fit
		this.fitBest(COMMAND_FIT_BEST_2);

		this.fenResults.pack();
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		if (DO_ONLY_GASTRIC) {
			this.generateInstructionsOnlyGastric();
		} else {
			this.generateInstructionsBothMethods();
		}
		
		getVue().setNbInstructions(this.allInputInstructions().size());
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