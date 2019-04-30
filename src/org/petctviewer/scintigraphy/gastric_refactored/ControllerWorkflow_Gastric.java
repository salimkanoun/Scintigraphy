package org.petctviewer.scintigraphy.gastric_refactored;

import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabChart;
import org.petctviewer.scintigraphy.gastric_refactored.tabs.TabMainResult;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.execution.CheckIntersectionInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptInstruction;

import ij.ImagePlus;

public class ControllerWorkflow_Gastric extends ControllerWorkflow {

	private FenResults fenResults;
	private List<ImagePlus> captures;

	public ControllerWorkflow_Gastric(Scintigraphy main, FenApplication vue, ImageSelection[] selectedImages,
			String studyName) {
		super(main, vue, new Model_Gastric(selectedImages, studyName));

		this.fenResults = new FenResults(model);
		this.fenResults.setVisible(false);
	}

	private void computeModel() {
		ImagePlus imp = this.model.getImagePlus();
		this.getModel().initResultat();
		for (int i = 0; i < this.getRoiManager().getRoisAsArray().length; i += 6) {
			imp = this.model.getImageSelection()[i / 6].getImagePlus();

			System.out.println("Saving results for image#" + i / 6);

			// Ant
			imp.setSlice(SLICE_ANT);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i]);
			this.getModel().calculerCoups("Estomac_Ant", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i + 1]);
			this.getModel().calculerCoups("Intes_Ant", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i + 2]);
			this.getModel().calculerCoups("Antre_Ant", i / 6, imp);
			this.getModel().setCoups("Fundus_Ant", i / 6,
					this.getModel().getCoups("Estomac_Ant", i / 6) - this.getModel().getCoups("Antre_Ant", i / 6));
			this.getModel().setCoups("Intestin_Ant", i / 6,
					this.getModel().getCoups("Intes_Ant", i / 6) - this.getModel().getCoups("Antre_Ant", i / 6));
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i + 3]);

			// Post
			imp.setSlice(SLICE_POST);
			this.getModel().calculerCoups("Estomac_Post", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i + 4]);
			this.getModel().calculerCoups("Intes_Post", i / 6, imp);
			imp.setRoi(this.getRoiManager().getRoisAsArray()[i + 5]);
			this.getModel().calculerCoups("Antre_Post", i / 6, imp);
			this.getModel().setCoups("Fundus_Post", i / 6,
					this.getModel().getCoups("Estomac_Post", i / 6) - this.getModel().getCoups("Antre_Post", i / 6));
			this.getModel().setCoups("Intestin_Post", i / 6,
					this.getModel().getCoups("Intes_Post", i / 6) - this.getModel().getCoups("Antre_Post", i / 6));
//			if (i == 0)
//				this.capture = Library_Capture_CSV.captureImage(imp, 640, 512);

			try {
				this.getModel().tempsImage(i / 6, imp);
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.getModel().pourcVGImage(i / 6);
		}
		this.model.calculerResultats();
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
		this.fenResults.setMainTab(new TabMainResult(this.fenResults, this.captures.get(0)));
		this.fenResults.addTab(new TabChart(this.fenResults));
		this.fenResults.pack();
		this.fenResults.setVisible(true);
	}

	@Override
	protected void generateInstructions() {
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null, dri_4 = null;

		this.captures = new ArrayList<>();

		// First instruction to get the acquisition time for the starting point
		PromptIngestionTime promptIngestionTime = new PromptIngestionTime(this);
		PromptInstruction promptTimeAcquisition = new PromptInstruction(promptIngestionTime);
		if (promptIngestionTime.isInputValid())
			this.getModel().setTimeIngestion(promptIngestionTime.getResult());

		for (int i = 0; i < this.model.getImageSelection().length; i++) {
			this.workflows[i] = new Workflow(this, this.getModel().getImageSelection()[i].getImagePlus());
			dri_1 = new DrawRoiInstruction("Stomach", Orientation.ANT, dri_3);
			dri_2 = new DrawRoiInstruction("Intestine", Orientation.ANT, dri_4);
			dri_3 = new DrawRoiInstruction("Stomach", Orientation.POST, dri_1);
			dri_4 = new DrawRoiInstruction("Intestine", Orientation.POST, dri_2);

			if (i == 0)
				this.workflows[i].addInstruction(promptTimeAcquisition);
			this.workflows[i].addInstruction(dri_1);
			this.workflows[i].addInstruction(dri_2);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_1, dri_2, "Antre"));
			this.workflows[i].addInstruction(dri_3);
			this.workflows[i].addInstruction(dri_4);
			this.workflows[i].addInstruction(new CheckIntersectionInstruction(this, dri_3, dri_4, "Antre"));
			if (i == 0)
				this.workflows[i].addInstruction(new ScreenShotInstruction(this.captures, this.vue, 640, 512));
		}
		this.workflows[this.model.getImageSelection().length - 1].addInstruction(new EndInstruction());
	}

}
