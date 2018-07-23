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
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Renal extends ControleurScin {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg", "Blood Pool" };
	private int nbOrganes;

	private boolean[] kidneys = new boolean[2];

	/**
	 * Controle l'execution du programme renal
	 * 
	 * @param vue la vue
	 */
	protected Controleur_Renal(DynamicScintigraphy vue) {
		super(vue);

		this.setOrganes(ORGANES);
		this.nbOrganes = ORGANES.length;

		Modele_Renal modele = new Modele_Renal(vue.getFrameDurations(), kidneys, vue.getImpPost());

		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		modele.setLocked(true);

		this.setModele(modele);
	}

	@Override
	public void setSlice(int indexSlice) {
		super.setSlice(indexSlice);

		// refactoriser pour eviter les copier colles
		this.hideLabel("R. bkg", Color.GRAY);
		this.hideLabel("L. bkg", Color.GRAY);
		this.hideLabel("R. Pelvis", Color.YELLOW);
		this.hideLabel("L. Pelvis", Color.YELLOW);
	}

	private void hideLabel(String name, Color c) {
		Overlay ov = this.getScin().getImp().getOverlay();
		Roi roi = ov.get(ov.getIndex(name));
		if (roi != null) {
			roi.setName("");
			roi.setStrokeColor(c);
		}
	}

	@Override
	public void fin() {
		// on supprime le listener de l'image plus
		this.removeImpListener();

		// on recupere la vue, le modele et l'imp
		RenalScintigraphy vue = (RenalScintigraphy) this.getScin();
		Modele_Renal modele = (Modele_Renal) vue.getFenApplication().getControleur().getModele();

		// on passe l'image post dans la vue
		ImagePlus imp = vue.getImpPost();

		// on debloque le modele
		modele.setLocked(false);

		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = ModeleScin.captureImage(this.getScin().getImp(), 300, 300).getBufferedImage();

		// on ajoute l'imp a la vue
		this.getScin().setImp(imp);

		// on enregistre la mesure pour chaque slice
		this.indexRoi = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				imp.setRoi(roiManager.getRoi(this.indexRoi % this.getOrganes().length));
				String nom = this.getNomOrgane(this.indexRoi);
				modele.enregistrerMesure(this.addTag(nom), imp);
				this.indexRoi++;
			}
		}

		// on calcule les resultats
		modele.calculerResultats();

		// on recupere les chartPanels avec l'association
		List<XYSeries> series = modele.getSeries();
		String[][] asso = new String[][] { { "Final KL", "Final KR" } };
		ChartPanel[] cp = ModeleScinDyn.associateSeries(asso, series);
		
		FenNeph fan = new FenNeph(cp[0], this.getScin().getFenApplication(), modele);
		fan.setModal(true);
		fan.setVisible(true);

		((RenalScintigraphy) this.getScin()).setNephrogramChart(fan.getValueSetter());
		((RenalScintigraphy) this.getScin()).setPatlakChart(fan.getPatlakChart());

		// on passe les valeurs ajustees au modele
		modele.setAdjustedValues(fan.getValueSetter().getValues());

		// on fait le fit vasculaire avec les donnees collectees
		modele.fitVasculaire();

		// on affiche la fenetre de resultats principale
		vue.setNephrogramChart(fan.getValueSetter());
		new FenResultats_Renal(vue, capture);
	}

	public void setKidneys(boolean[] kidneys) {
		this.kidneys = kidneys;
		((Modele_Renal) this.getModele()).setKidneys(kidneys);
		this.adjustOrgans();
	}

	public boolean[] getKidneys() {
		return this.kidneys;
	}

	private void adjustOrgans() {

		nbOrganes = ORGANES.length;
		// on rajoute les organes selon les preferences
		ArrayList<String> organes = new ArrayList<>(Arrays.asList(Controleur_Renal.ORGANES));

		if (!kidneys[0]) {
			organes.remove("L. Kidney");
			organes.remove("L. bkg");
		}

		if (!kidneys[1]) {
			organes.remove("R. Kidney");
			organes.remove("R. bkg");
		}

		if (Prefs.get("renal.bladder.preferred", true)) {
			organes.add("Bladder");
		}

		if (Prefs.get("renal.pelvis.preferred", true)) {
			if (kidneys[0]) {
				organes.add(organes.indexOf("L. Kidney") + 1, "L. Pelvis");
			}

			if (kidneys[1]) {
				organes.add(organes.indexOf("R. Kidney") + 1, "R. Pelvis");
			}
		}

		if (Prefs.get("renal.ureter.preferred", true)) {
			if (kidneys[0]) {
				organes.add("L. Ureter");
			}

			if (kidneys[1]) {
				organes.add("R. Ureter");
			}
		}

		this.nbOrganes = organes.size();
		this.setOrganes(organes.toArray(new String[0]));
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 0;
	}
	
	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (indexRoi > 0 && lastRoi < this.indexRoi) {
			String org = this.getNomOrgane(indexRoi - 1);

			// roi de bruit de fond
			boolean pelvis = Prefs.get("renal.pelvis.preferred", true);
			if (!pelvis && org.contains("Kidney")) {
				Roi roi = roiManager.getRoi(indexRoi - 1);
				return Scintigraphy.createBkgRoi(roi, getScin().getImp(), Scintigraphy.KIDNEY);
			}else if(pelvis && org.contains("Pelvis")) {
				Roi roi = roiManager.getRoi(indexRoi - 2);
				return Scintigraphy.createBkgRoi(roi, getScin().getImp(), Scintigraphy.KIDNEY);
			}
		}

		return null;
	}

	@Override
	public boolean isPost() {
		return true;
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= this.getOrganes().length - 1;
	}

}
