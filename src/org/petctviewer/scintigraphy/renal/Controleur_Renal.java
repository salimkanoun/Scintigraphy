package org.petctviewer.scintigraphy.renal;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.gui.FenNeph;
import org.petctviewer.scintigraphy.renal.gui.FenResultats_Renal;
import org.petctviewer.scintigraphy.scin.Controleur_OrganeFixe;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Roi;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Renal extends Controleur_OrganeFixe {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg", "Blood Pool" };

	/**
	 * Controle l'execution du programme renal
	 * 
	 * @param renalScinti la vue
	 */
	protected Controleur_Renal(RenalScintigraphy renalScinti, ImageSelection[] selectedImages, String studyName) {
		super(renalScinti, new Modele_Renal(renalScinti.getFrameDurations(), selectedImages, studyName));
		
		((Modele_Renal)this.model).setKidneys(new boolean[2]);

		this.setOrganes(ORGANES);

		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		((Modele_Renal) model).setLocked(true);
	}

	/************ Setter ***********/
	@Override
	public void setSlice(int indexSlice) {
		super.setSlice(indexSlice);

		// refactoriser pour eviter les copier colles
		this.hideLabel("R. bkg", Color.GRAY);
		this.hideLabel("L. bkg", Color.GRAY);
		this.hideLabel("R. Pelvis", Color.YELLOW);
		this.hideLabel("L. Pelvis", Color.YELLOW);
	}

	public void setKidneys(boolean[] kidneys) {
		((Modele_Renal) this.model).setKidneys(kidneys);
		this.adjustOrgans();
	}

	/************ Getter ***********/
	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 0;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		System.out.println("lastROi" + lastRoi);
		System.out.println("current index" + indexRoi);
		// Sens aller
		if (lastRoi < indexRoi) {
			String org = this.getNomOrgane(lastRoi);
			System.out.println("organRoiName" + org);
			// roi de bruit de fond
			boolean pelvis = Prefs.get("renal.pelvis.preferred", true);
			if (!pelvis && org.contains("Kidney")) {
				Roi roi = this.model.getRoiManager().getRoi(indexRoi - 1);
				return Library_Roi.createBkgRoi(roi, this.model.getImagePlus(), Library_Roi.KIDNEY);
			} else if (pelvis && org.contains("Pelvis")) {
				Roi roi = this.model.getRoiManager().getRoi(indexRoi - 2);
				return Library_Roi.createBkgRoi(roi, this.model.getImagePlus(), Library_Roi.KIDNEY);
			}
			// Sens Retour
		} else if (lastRoi == indexRoi) {
			return this.model.getRoiManager().getRoi(indexRoi);
		}

		return null;
	}

	/************ iS ***********/
	@Override
	public boolean isPost() {
		return true;
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= this.getOrganes().length - 1;
	}

	/************ Methods ***********/
	private void adjustOrgans() {

		// on rajoute les organes selon les preferences
		ArrayList<String> organes = new ArrayList<>(Arrays.asList(Controleur_Renal.ORGANES));

		if (!((Modele_Renal)this.model).getKidneys()[0]) {
			organes.remove("L. Kidney");
			organes.remove("L. bkg");
		}

		if (!((Modele_Renal)this.model).getKidneys()[1]) {
			organes.remove("R. Kidney");
			organes.remove("R. bkg");
		}

		if (Prefs.get("renal.bladder.preferred", true)) {
			organes.add("Bladder");
		}

		if (Prefs.get("renal.pelvis.preferred", true)) {
			if (((Modele_Renal)this.model).getKidneys()[0]) {
				organes.add(organes.indexOf("L. Kidney") + 1, "L. Pelvis");
			}

			if (((Modele_Renal)this.model).getKidneys()[1]) {
				organes.add(organes.indexOf("R. Kidney") + 1, "R. Pelvis");
			}
		}

		if (Prefs.get("renal.ureter.preferred", true)) {
			if (((Modele_Renal)this.model).getKidneys()[0]) {
				organes.add("L. Ureter");
			}

			if (((Modele_Renal)this.model).getKidneys()[1]) {
				organes.add("R. Ureter");
			}
		}

		this.setOrganes(organes.toArray(new String[0]));
	}

	@Override
	public void end() {
		// Increment l'index de 1 pour eviter erreur d'index
		// A VEFIRIER SK
		this.indexRoi++;

		// on supprime le listener de l'image plus
		// this.removeImpListener();

		// on recupere la vue, le modele et l'imp
		RenalScintigraphy scinRenal = (RenalScintigraphy) this.getScin();
		Modele_Renal modele = (Modele_Renal) this.model;

		// Remet les data du modele a zero (en cas de relance)
		modele.getData().clear();

		// On recupere l'image Post dynamique sur laquelle on fait les quantifications
		ImagePlus imp = scinRenal.getImpPost().getImagePlus();

		// on debloque le modele pour avoir l'enregistrement des mesures
		modele.setLocked(false);

		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = Library_Capture_CSV.captureImage(this.model.getImagePlus(), 300, 300).getBufferedImage();

		// on enregistre la mesure pour chaque slice
		int indexRoi = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				imp.setRoi(this.model.getRoiManager().getRoi(indexRoi % this.getOrganes().length));
				String nom = this.getNomOrgane(indexRoi);
				modele.enregistrerMesure(this.addTag(nom), imp);
				indexRoi++;
			}
		}

		// on calcule les resultats
		modele.calculerResultats();

		// on recupere les chartPanels avec l'association
		List<XYSeries> series = modele.getSeries();
		String[][] asso = new String[][] { { "Final KL", "Final KR" } };
		ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);

		FenNeph fan = new FenNeph(cp[0], this.getScin().getFenApplication(), modele);
		fan.setModal(true);
		fan.setVisible(true);

		((Modele_Renal)model).setNephrogramChart(fan.getValueSetter());
		((Modele_Renal)model).setPatlakChart(fan.getPatlakChart());

		// on passe les valeurs ajustees au modele
		modele.setAdjustedValues(fan.getValueSetter().getValues());

		// on fait le fit vasculaire avec les donnees collectees
		modele.fitVasculaire();

		// on affiche la fenetre de resultats principale
		((Modele_Renal)model).setNephrogramChart(fan.getValueSetter());
		new FenResultats_Renal(scinRenal, capture, this);

		// SK On rebloque le modele pour la prochaine generation
		modele.setLocked(true);

	}

	private void hideLabel(String name, Color c) {
		Overlay ov = this.model.getImagePlus().getOverlay();
		Roi roi = ov.get(ov.getIndex(name));
		if (roi != null) {
			roi.setName("");
			roi.setStrokeColor(c);
		}
	}

}
