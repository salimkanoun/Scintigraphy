package org.petctviewer.scintigraphy.renal;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.renal.gui.FenNeph;
import org.petctviewer.scintigraphy.renal.gui.FenResultats_Renal;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Renal extends ControleurScin {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg", "Blood Pool" };

	// plus grande valeur que index roi ait prise
	private int maxIndexRoi = 0;
	private int nbOrganes;

	private boolean[] kidneys = new boolean[2];

	/**
	 * Controle l'execution du programme renal
	 * 
	 * @param vue
	 *            la vue
	 */
	protected Controleur_Renal(VueScinDyn vue) {
		super(vue);

		this.setOrganes(ORGANES);
		this.nbOrganes = ORGANES.length;

		Modele_Renal modele = new Modele_Renal(vue.getFrameDurations(), kidneys, vue.getImpPost());

		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		modele.lock();

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
		Overlay ov = this.getVue().getImp().getOverlay();
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
		VueScinDyn vue = (VueScinDyn) this.getVue();
		Modele_Renal modele = (Modele_Renal) vue.getFenApplication().getControleur().getModele();

		// on passe l'image post dans la vue
		ImagePlus imp = vue.getImpPost();

		// on debloque le modele
		modele.unlock();

		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = ModeleScin.captureImage(this.getVue().getImp(), 300, 300).getBufferedImage();

		// on ajoute l'imp a la vue
		this.getVue().setImp(imp);

		// on enregistre la mesure pour chaque slice
		this.indexRoi = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
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

		FenNeph fan = new FenNeph(cp[0], this.getVue().getFenApplication(), modele);
		fan.setModal(true);
		fan.setVisible(true);
		
		// on passe les valeurs ajustees au modele
		modele.setAdjustedValues(fan.getSelectorListener().getValues());

		// on fait le fit vasculaire avec les donnees collectees
		modele.fitVasculaire();

		// on affiche la fenetre de resultats principale
		new FenResultats_Renal(vue, capture, fan.getSelectorListener().getChartPanel());

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
		boolean[] settings = RenalSettings.getOrganSettings();
		ArrayList<String> organes = new ArrayList<>(Arrays.asList(Controleur_Renal.ORGANES));

		if (!kidneys[0]) {
			organes.remove("L. Kidney");
			organes.remove("L. bkg");
		}

		if (!kidneys[1]) {
			organes.remove("R. Kidney");
			organes.remove("R. bkg");
		}

		if (settings[0]) {
			organes.add("Bladder");
		}

		if (settings[1]) {
			if (kidneys[0]) {
				organes.add(organes.indexOf("L. Kidney") + 1, "L. Pelvis");
			}

			if (kidneys[1]) {
				organes.add(organes.indexOf("R. Kidney") + 1, "R. Pelvis");
			}
		}

		if (settings[2]) {
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
	public void notifyClic(ActionEvent arg0) {
		this.maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.isOver()) {
			// on renvoie la roi de l'organe uniquement si on a fini de tracer les roi
			return this.roiManager.getRoi(this.indexRoi % this.getOrganes().length);
		}

		// roi de bruit de fond
		boolean cort = RenalSettings.getOrganSettings()[1];

		String org = this.getNomOrgane(indexRoi - 1);

		if ((!cort && org.contains("Kidney")) || (cort && org.contains("Pelvis"))) {
			return this.createBkgRoi(this.indexRoi);
		}

		return null;
	}

	// cree la roi de bruit de fond
	private Roi createBkgRoi(int indexRoi) {
		ImagePlus imp = this.getVue().getImp();

		int indexLiver;
		// on clone la roi du rein
		if (RenalSettings.getOrganSettings()[1]) {
			// si on trace des pelvis, il faut decaler de deux
			indexLiver = indexRoi - 2;
		} else {
			indexLiver = indexRoi - 1;
		}

		// largeur a prendre autour du rein
		int largeurBkg = 1;
		if (this.getVue().getImp().getDimensions()[0] >= 128) {
			largeurBkg = 2;
		}

		this.roiManager.select(indexLiver);
		IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
		this.roiManager.addRoi(imp.getRoi());

		this.roiManager.select(this.roiManager.getCount() - 1);
		IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
		this.roiManager.addRoi(imp.getRoi());

		this.roiManager
				.setSelectedIndexes(new int[] { this.roiManager.getCount() - 2, this.roiManager.getCount() - 1 });
		this.roiManager.runCommand(imp, "XOR");

		Roi bkg = imp.getRoi();

		// on supprime les rois de construction
		while (this.roiManager.getCount() - 1 > this.maxIndexRoi) {
			this.roiManager.select(this.roiManager.getCount() - 1);
			this.roiManager.runCommand(imp, "Delete");
		}

		return bkg;
	}

	@Override
	public boolean isPost() {
		return true;
	}

	@Override
	public boolean isOver() {
		return this.maxIndexRoi == this.nbOrganes - 1;
	}

}
