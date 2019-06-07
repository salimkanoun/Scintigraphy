package org.petctviewer.scintigraphy.renal.postMictional;


import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackground;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ControllerWorkflowPostMictional extends ControllerWorkflow {

	public String[] organeListe;

	private final boolean[] kidneys;

	private List<ImagePlus> captures;

	public ControllerWorkflowPostMictional(Scintigraphy main, FenApplicationWorkflow vue, ModelScin model,
			boolean[] kidneys) {
		super(main, vue, model);

		this.kidneys = kidneys;

		this.captures = new ArrayList<>();

		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		List<String> organes = new LinkedList<>();

		this.workflows = new Workflow[1];
		DrawRoiInstruction dri_1, dri_2, dri_3;

		DrawRoiBackground dri_Background_1, dri_Background_2;

		ScreenShotInstruction dri_capture_1 = null;

		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState statePost = new ImageState(Orientation.POST, 1, false, ImageState.ID_NONE);

		if (this.kidneys[0]) {
			dri_1 = new DrawRoiInstruction("L. Kidney", statePost);
			this.workflows[0].addInstruction(dri_1);
			organes.add("L. Kidney");

			dri_Background_1 = new DrawRoiBackground("L. Background", statePost, dri_1, this.model, " ");
			this.workflows[0].addInstruction(dri_Background_1);
			organes.add("L. bkg");
		}

		if (this.kidneys[1]) {
			dri_2 = new DrawRoiInstruction("R. Kidney", statePost);
			this.workflows[0].addInstruction(dri_2);
			organes.add("R. Kidney");

			dri_Background_2 = new DrawRoiBackground("R. Background", statePost, dri_2, this.model, " ");
			this.workflows[0].addInstruction(dri_Background_2);
			organes.add("R. bkg");
		}

		if (Prefs.get(PrefTabRenal.PREF_BLADDER, true)) {
			dri_3 = new DrawRoiInstruction("Bladder", statePost);
			this.workflows[0].addInstruction(dri_3);
			organes.add("Bladder");
		}

		this.organeListe = organes.toArray(new String[0]);

		this.workflows[0].addInstruction(dri_capture_1);

		this.workflows[0].addInstruction(new EndInstruction());
	}

	@Override
	public void end() {
		HashMap<String, Double> hm = new HashMap<>();
		ImagePlus imp = this.model.getImagePlus().duplicate();
		// Normalizing to compare to the previous values, from the original exam.
		Library_Dicom.normalizeToCountPerSecond(imp);
		for (int indexRoi = 0; indexRoi < this.organeListe.length; indexRoi++) {
			imp.setRoi(this.model.getRoiManager().getRoi(indexRoi));
			hm.put(this.organeListe[indexRoi], Library_Quantif.getCounts(imp));
		}
		((Model_PostMictional) this.model).setData(hm);

		((PostMictional) this.main).getResultFrame().setExamDone(true);
		((PostMictional) this.main).getResultFrame().setModelPostMictional((Model_PostMictional) this.model);
		// ((PostMictional) this.main).getResultFrame().reloadDisplay();
		((PostMictional) this.main).getResultFrame().setImp(this.vue.getImagePlus());
		// ((PostMictional)
		// this.main).getResultFrame().getImagePlus().duplicate().show();
		// captures.get(0).show();
		// this.main.getFenApplication().dispose();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		Overlay ov = this.model.getImagePlus().getOverlay();

		if (ov.getIndex("L. bkg") != -1)
			Library_Gui.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);

		if (ov.getIndex("R. bkg") != -1)
			Library_Gui.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
	}

}
