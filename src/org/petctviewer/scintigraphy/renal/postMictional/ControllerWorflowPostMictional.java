package org.petctviewer.scintigraphy.renal.postMictional;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackground;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;

public class ControllerWorflowPostMictional extends ControllerWorkflow {
	
	public String[] organeListe;

	private List<ImagePlus> captures;
	
	private boolean[] kidneys;

	public ControllerWorflowPostMictional(Scintigraphy main, FenApplication vue, ModeleScin model, boolean[] kidneys) {
		super(main, vue, model);
		
		this.kidneys = kidneys;
		
		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {
		
		List<String> organes = new LinkedList<>();

		this.workflows = new Workflow[1];
		DrawRoiInstruction dri_1 = null, dri_2 = null, dri_3 = null;

		DrawRoiBackground dri_Background_1 = null, dri_Background_2 = null;
		this.captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState statePost = new ImageState(Orientation.POST, 1, false, ImageState.ID_NONE);

		if (this.kidneys[0]) {
			dri_1 = new DrawRoiInstruction("L. Kidney", statePost);
			this.workflows[0].addInstruction(dri_1);
			organes.add("L. Kidney");

			dri_Background_1 = new DrawRoiBackground("L. Background", statePost, dri_1, this.model);
			this.workflows[0].addInstruction(dri_Background_1);
			organes.add("L. bkg");
		}

		if (this.kidneys[1]) {
			dri_2 = new DrawRoiInstruction("R. Kidney", statePost);
			this.workflows[0].addInstruction(dri_2);
			organes.add("R. Kidney");

			dri_Background_2 = new DrawRoiBackground("R. Background", statePost, dri_2, this.model);
			this.workflows[0].addInstruction(dri_Background_2);
			organes.add("R. bkg");
		}

		if (Prefs.get("renal.bladder.preferred", true)) {
			dri_3 = new DrawRoiInstruction("Bladder", statePost);
			this.workflows[0].addInstruction(dri_3);
			organes.add("Bladder");
		}
		
		this.organeListe = organes.toArray(new String[organes.size()]);

		this.workflows[0].addInstruction(new EndInstruction());

	}
	
	@Override
	public void end() {
		int indexRoi = 0;
		HashMap<String, Double> hm = new HashMap<String, Double>();
		ImagePlus imp = this.model.getImagePlus().duplicate();
		Library_Dicom.normalizeToCountPerSecond(imp);
		for (int j = 0; j < this.model.getRoiManager().getCount(); j++) {
			System.out.println(this.model.getRoiManager().getRoi(indexRoi) == null);
			imp.setRoi(this.model.getRoiManager().getRoi(indexRoi));
			String name = this.getNomOrgane(indexRoi);
			System.out.println(name);
			System.out.println(Library_Quantif.getCounts(imp));
			hm.put(name, Library_Quantif.getCounts(imp));
			indexRoi++;
		}
		((Modele_PostMictional) this.model).setData(hm);
		this.main.getFenApplication().dispose();
		((PostMictional) this.main).getResultFrame().setExamDone(true);
		((PostMictional) this.main).getResultFrame().setModelPostMictional((Modele_PostMictional) this.model);
		((PostMictional) this.main).getResultFrame().reloadDisplay();

	}
	
	public String getNomOrgane(int index) {
		return this.organeListe[index % this.organeListe.length];
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		super.actionPerformed(arg0);
		Overlay ov = this.model.getImagePlus().getOverlay();

		if (ov.getIndex("L. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "L. bkg", "", Color.GRAY);
		}

		if (ov.getIndex("R. bkg") != -1) {
			Library_Gui.editLabelOverlay(ov, "R. bkg", "", Color.GRAY);
		}

	}

}
