package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabChart;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabDefaultMethod;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMainResult;
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
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;

import ij.ImagePlus;

public class ControllerWorkflow_Gastric extends ControllerWorkflow implements ChartMouseListener, ItemListener {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	private FenResults fenResults;
	private TabChart tabChart;
	private TabMainResult tabMain;
	private TabDefaultMethod tabDefaultMethod;

	private List<ImagePlus> captures;

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));

		this.generateInstructions();
		this.start();

		this.fenResults = new FenResults(this);
		this.fenResults.setVisible(false);
	}

	@Override
	protected void start() {
		// Find isotope
		String isotopeCode = Library_Dicom.findIsotopeCode(getModel().getImagePlus());
		if(isotopeCode == null) {
			// No code
			System.out.println("No code found in the image");
			// Ask user for code
			IsotopeDialog isotopeDialog = new IsotopeDialog(vue);
			isotopeDialog.setVisible(true);
		}
		Isotope isotope = Isotope.getIsotopeFromCode(isotopeCode);
		if(isotope == null) {
			// Code unknown
			System.out.println("Code found in the image unknown");
			// Ask user for isotope
			IsotopeDialog isotopeDialog = new IsotopeDialog(vue, isotopeCode);
			isotopeDialog.setVisible(true);
			isotope = isotopeDialog.getIsotope();
		}
		getModel().setIsotope(isotope);
		System.out.println("Isotope found: " + isotope);
		
		super.start();
	}

	// TODO: remove this method and compute the model during the process
	private void computeModel() {

		// Place point 0
		getModel().activateTime0();

		ImageState previousState = null;
		int count = 0;
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

			System.out.println("*** COMPUTING " + ++count + " ***");
			getModel().computeStaticData(state, previousState);
			previousState = state;
		}
		this.model.calculerResultats();
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
	protected void end() {
		super.end();

		// Compute model
		this.computeModel();

		// Display results
		this.tabChart = new TabChart(this.fenResults);
		
		this.tabMain = new TabMainResult(this.fenResults, this.captures.get(0), this);
		this.tabMain.displayTimeIngestion(getModel().getTimeIngestion());
		
		this.tabDefaultMethod = new TabDefaultMethod(this.fenResults);

		this.fenResults.clearTabs();
		this.fenResults.setMainTab(this.tabMain);
		this.fenResults.addTab(this.tabChart);
		this.fenResults.addTab(tabDefaultMethod);
		this.fenResults.pack();
		this.fenResults.setSize(this.fenResults.getSize().width, 1024);
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
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4, "Antre"));
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 0, 640, 512));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// Does nothing
		this.tabMain.reloadSidePanelContent();
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// Reload fit
		if (this.tabChart.getValueSetter().getGrabbedSelector() != null) {
			XYSeries series = ((XYSeriesCollection) this.tabChart.getValueSetter().retrieveValuesInSpan()).getSeries(0);
			try {
				this.getModel().setExtrapolation(Fit.createFit(this.tabChart.getSelectedFit(),
						Library_JFreeChart.invertArray(series.toArray())));
				this.tabChart.drawFit(this.getModel().getFittedSeries());
				this.tabChart.setErrorMessage(null);
			} catch (IllegalArgumentException e) {
				System.err.println("Not enough data");
				this.tabChart.setErrorMessage("Not enough data to fit the graph");
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		FitType selectedFit = (FitType) event.getItem();

		// By default, use linear fit
		XYSeries series = ((XYSeriesCollection) this.tabChart.getValueSetter().retrieveValuesInSpan()).getSeries(0);
		try {
			this.getModel()
					.setExtrapolation(Fit.createFit(selectedFit, Library_JFreeChart.invertArray(series.toArray())));
			this.tabChart.drawFit(this.getModel().getFittedSeries());
			this.tabChart.changeLabelInterpolation(selectedFit.toString());
			this.tabMain.reloadSidePanelContent();
			this.tabChart.setErrorMessage(null);
		} catch (IllegalArgumentException e) {
			// Error messages
			System.err.println("Not enough data");
			this.tabChart.setErrorMessage("Not enough data to fit the graph");
		}
	}

}