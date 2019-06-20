package org.petctviewer.scintigraphy.renal;

import ij.ImagePlus;
import ij.Prefs;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.gui.FenNeph;
import org.petctviewer.scintigraphy.renal.gui.FenResultats_Renal;
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

public class ControllerWorkflowRenal extends ControllerWorkflow {

	public String[] organeListe;

	public ControllerWorkflowRenal(FenApplicationWorkflow vue, ModelScin model) {
		super(vue, model);

		// Those method (generateInstructions() and start()) are called in setKidneys,
		// to avoid problems, because you need to
		// select the kidneys number before starting the Controller.
	}

	@Override
	public void end() {
		super.end();

		// on recupere la vue, le modele et l'imp
		Model_Renal modele = (Model_Renal) this.model;

		// Remet les data du modele a zero (en cas de relance)
		modele.getData().clear();

		// On recupere l'image Post dynamique sur laquelle on fait les quantifications
		ImagePlus imp = modele.getImpPost().getImagePlus();

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

		FenNeph fan = new FenNeph(cp[0], this.getVue(), modele);
		fan.setModal(true);
		fan.setVisible(true);

		((Model_Renal) model).setNephrogramChart(fan.getValueSetter());
		((Model_Renal) model).setPatlakChart(fan.getPatlakChart());

		// on passe les valeurs ajustees au modele
		modele.setAdjustedValues(fan.getValueSetter().getValues());

		// on fait le fit vasculaire avec les donnees collectees
		modele.fitVasculaire();

		// on affiche la fenetre de resultats principale
		((Model_Renal) model).setNephrogramChart(fan.getValueSetter());
		FenResults fenResults = new FenResultats_Renal(capture, this);
		fenResults.toFront();
		fenResults.setVisible(true);


		// SK On rebloque le modele pour la prochaine generation
		modele.setLocked(true);

	}

	/**
	 * This method is called to construct and start the controller, because we need to specifie the number of kidney
	 * before.
	 *
	 * @param kidneys Boolean array to specify the kidneys
	 */
	public void setKidneys(boolean[] kidneys) {
		((Model_Renal) this.model).setKidneys(kidneys);
		this.generateInstructions();
		this.start();
	}

	@Override
	protected void generateInstructions() {

		List<String> organes = new LinkedList<>();

		this.workflows = new Workflow[1];
		DrawRoiInstruction dri_1, dri_2, dri_3, dri_4, dri_5, dri_6, dri_7, dri_8;
		DrawRoiBackground dri_Background_1, dri_Background_2;
		ScreenShotInstruction dri_capture_1;
		List<ImagePlus> captures = new ArrayList<>();

		this.workflows[0] = new Workflow(this, this.model.getImageSelection()[0]);

		ImageState statePost = new ImageState(Orientation.POST, 1, ImageState.LAT_LR, ImageState.ID_NONE);

		dri_capture_1 = new ScreenShotInstruction(captures, this.getVue(), 0);

		if (((Model_Renal) this.model).getKidneys()[0]) {

			dri_1 = new DrawRoiInstruction("L. Kidney", statePost);
			this.workflows[0].addInstruction(dri_1);
			organes.add("L. Kidney");

			if (Prefs.get(PrefTabRenal.PREF_PELVIS, true)) {
				dri_2 = new DrawRoiInstruction("L. Pelvis", statePost);
				this.workflows[0].addInstruction(dri_2);
				organes.add("L. Pelvis");
			}

			dri_Background_1 = new DrawRoiBackground("L. Background", statePost, dri_1, this.workflows[0], "");
			this.workflows[0].addInstruction(dri_Background_1);
			organes.add("L. bkg");

		}

		if (((Model_Renal) this.model).getKidneys()[1]) {
			dri_3 = new DrawRoiInstruction("R. Kidney", statePost);
			this.workflows[0].addInstruction(dri_3);
			organes.add("R. Kidney");

			if (Prefs.get(PrefTabRenal.PREF_PELVIS, true)) {
				dri_4 = new DrawRoiInstruction("R. Pelvis", statePost);
				this.workflows[0].addInstruction(dri_4);
				organes.add("R. Pelvis");
			}

			dri_Background_2 = new DrawRoiBackground("R. Background", statePost, dri_3, this.workflows[0], "");
			this.workflows[0].addInstruction(dri_Background_2);
			organes.add("R. bkg");

		}

		dri_5 = new DrawRoiInstruction("Blood Pool", statePost);
		this.workflows[0].addInstruction(dri_5);
		organes.add("Blood Pool");

		if (Prefs.get(PrefTabRenal.PREF_BLADDER, true)) {
			dri_6 = new DrawRoiInstruction("Bladder", statePost);
			this.workflows[0].addInstruction(dri_6);
			organes.add("Bladder");
		}

		if (Prefs.get(PrefTabRenal.PREF_URETER, true)) {
			if (((Model_Renal) this.model).getKidneys()[0]) {
				dri_7 = new DrawRoiInstruction("L. Ureter", statePost);
				this.workflows[0].addInstruction(dri_7);
				organes.add("L. Ureter");
			}

			if (((Model_Renal) this.model).getKidneys()[1]) {
				dri_8 = new DrawRoiInstruction("R. Ureter", statePost);
				this.workflows[0].addInstruction(dri_8);
				organes.add("R. Ureter");
			}

		}

		this.organeListe = organes.toArray(new String[0]);

		this.workflows[0].addInstruction(dri_capture_1);

		this.workflows[0].addInstruction(new EndInstruction());
	}

}