package org.petctviewer.scintigraphy.gastric_refactored;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit;
import org.petctviewer.scintigraphy.gastric_refactored.gui.Fit.FitType;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabChart;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabDynamic;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMainResult;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import ij.ImagePlus;

public class ControllerWorkflow_Gastric extends ControllerWorkflow implements ChartMouseListener {

	private static final int SLICE_ANT = 1, SLICE_POST = 2;

	private FenResults fenResults;
	private TabChart tabChart;
	private TabMainResult tabMain;

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
				Region regionStomach = new Region(Model_Gastric.REGION_STOMACH);
				regionStomach.inflate(state, this.getRoiManager().getRoisAsArray()[i + indexIncrementPost]);
				getModel().calculateCounts(regionStomach);

				// - Antre
				Region regionAntre = new Region(Model_Gastric.REGION_ANTRE);
				regionAntre.inflate(state, this.getRoiManager().getRoisAsArray()[i + 2 + indexIncrementPost]);
				getModel().calculateCounts(regionAntre);

				// - Fundus
				Region regionFundus = new Region(Model_Gastric.REGION_FUNDUS);
				regionFundus.inflate(state, null);
				getModel().calculateCounts(regionFundus);

				// - Intestine
				Region regionIntestine = new Region(Model_Gastric.REGION_INTESTINE);
				regionIntestine.inflate(state, this.getRoiManager().getRoisAsArray()[i + 1 + indexIncrementPost]);
				getModel().calculateCounts(regionIntestine);
			}

			// The numActualImage is reversed because the images are in reversed order
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
		this.tabMain = new TabMainResult(this.fenResults, this.captures.get(0));
		this.tabMain.displayTimeIngestion(getModel().getTimeIngestion());

		this.fenResults.clearTabs();
		this.fenResults.setMainTab(this.tabMain);
		this.fenResults.addTab(this.tabChart);
		this.fenResults.addTab(new TabDynamic(this.fenResults, this));
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
	public void actionPerformed(ActionEvent event) {
		super.actionPerformed(event);

		if (!(event.getSource() instanceof JComboBox))
			return;

		JComboBox<FitType> source = (JComboBox<FitType>) event.getSource();
		FitType selectedFit = (FitType) source.getSelectedItem();

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
			// Reset combo box
			source.setSelectedItem(this.getModel().getCurrentExtrapolation());
		}
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
				this.getModel().setExtrapolation(Fit.createFit(this.getModel().getCurrentExtrapolation(),
						Library_JFreeChart.invertArray(series.toArray())));
				this.tabChart.drawFit(this.getModel().getFittedSeries());
				this.tabChart.setErrorMessage(null);
			} catch (IllegalArgumentException e) {
				System.err.println("Not enough data");
				this.tabChart.setErrorMessage("Not enough data to fit the graph");
			}
		}
	}

}