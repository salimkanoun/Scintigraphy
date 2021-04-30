package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.Prefs;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.renal.gui.FenResultats_Renal;
import org.petctviewer.scintigraphy.salivaryGlands.gui.FenCitrus;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Workflow;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiBackground;
import org.petctviewer.scintigraphy.scin.instructions.drawing.DrawRoiInstruction;
import org.petctviewer.scintigraphy.scin.instructions.execution.ScreenShotInstruction;
import org.petctviewer.scintigraphy.scin.instructions.messages.EndInstruction;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabRenal;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ControllerWorkflowSalivaryGlands extends ControllerWorkflow {

	public String[] organeListe;

	public ControllerWorkflowSalivaryGlands(FenApplicationWorkflow vue, ModelScin model) {
		super(vue, model);
		
		this.generateInstructions();
		this.start();
	}

	@Override
	public void end() {
		super.end();

		// on recupere la vue, le modele et l'imp
		ModelSalivaryGlands modele = (ModelSalivaryGlands) this.model;

		// Remet les data du modele a zero (en cas de relance)
		modele.getData().clear();

		// On recupere l'image Ant dynamique sur laquelle on fait les quantifications
		ImagePlus imp = modele.getImpAnt().getImagePlus();

		// on debloque le modele pour avoir l'enregistrement des mesures
		modele.setLocked(false);

		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 512, 0).getBufferedImage();

		// on enregistre la mesure pour chaque slice
		for (int indexSlice = 1; indexSlice <= imp.getStackSize(); indexSlice++) {
			imp.setSlice(indexSlice);
			for (int indexRoi = 0; indexRoi < this.organeListe.length; indexRoi++) {
				imp.setRoi(this.model.getRoiManager().getRoi(indexRoi));
				String nom = this.organeListe[indexRoi];
				modele.enregistrerMesure(nom, imp);

				if (indexSlice == 1) modele.enregistrerPixelRoi(nom, Library_Quantif.getPixelNumber(imp));
			}
		}


		// on calcule les resultats
		modele.calculateResults();

		// on recupere les chartPanels avec l'association
		List<XYSeries> series = modele.getSeries();
		String[][] asso = new String[][]{{"Final KL", "Final KR"}};
		ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);

		FenCitrus fan = new FenCitrus(cp[0], this.getVue(), modele);
		fan.setModal(true);
		fan.setVisible(true);
		fan.toFront();

		// on passe les valeurs ajustees au modele
		modele.setAdjustedValues(fan.getValueSetter().getValues());

		// on affiche la fenetre de resultats principale
		//FenResults fenResults = new FenResultats_Renal(capture, this);
		//fenResults.toFront();
		//fenResults.setVisible(true);


		// SK On rebloque le modele pour la prochaine generation
		modele.setLocked(true);

	}

	@Override
	protected void generateInstructions() {

		List<String> organes = new LinkedList<>();

		this.workflows = new Workflow[1];
		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4;
		DrawRoiInstruction dri_Background_1;
		ScreenShotInstruction dri_capture_1;
		List<ImagePlus> captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState stateAnt = new ImageState(Orientation.ANT, 1, ImageState.LAT_RL, ImageState.ID_NONE);

		dri_1 = new DrawRoiInstruction("R. Parotid", stateAnt);
		this.workflows[0].addInstruction(dri_1);
		organes.add("R. Parotid");

		dri_2 = new DrawRoiInstruction("L. Parotid", stateAnt);
		this.workflows[0].addInstruction(dri_2);
		organes.add("L. Parotid");

		dri_Background_1 = new DrawRoiInstruction("Background", stateAnt);
		this.workflows[0].addInstruction(dri_Background_1);
		organes.add("Background");


		dri_3 = new DrawRoiInstruction("R. SubMandib", stateAnt);
		this.workflows[0].addInstruction(dri_3);
		organes.add("R. SubMandib");

		
		dri_4 = new DrawRoiInstruction("L. SubMandib", stateAnt);
		this.workflows[0].addInstruction(dri_4);
		organes.add("L. SubMandib");

		
		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);

		
		this.organeListe = organes.toArray(new String[0]);

		this.workflows[0].addInstruction(dri_capture_1);

		this.workflows[0].addInstruction(new EndInstruction());
	}

}