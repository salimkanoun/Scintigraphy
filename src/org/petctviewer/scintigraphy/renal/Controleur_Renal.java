package org.petctviewer.scintigraphy.renal;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_Renal extends ControleurScin {

	public static String[] ORGANES = { "R. Kidney", "R. bkg", "L. Kidney", "L. bkg", "Blood Pool", "Bladder" };

	//plus grande valeur que index roi ait prise
	private int maxIndexRoi = 0;

	/**
	 * Controle l'execution du programme renal
	 * @param vue la vue
	 */
	protected Controleur_Renal(VueScinDyn vue) {
		super(vue);
		this.setOrganes(ORGANES);
		Modele_Renal modele = new Modele_Renal(vue.getFrameDurations());

		// on bloque le modele pour ne pas enregistrer les valeurs de la projection
		modele.lock();

		this.setModele(modele);
	}

	@Override
	public boolean isOver() {
		return this.maxIndexRoi == ORGANES.length - 1;
	}

	@Override
	public void fin() {
		// on supprime le listener de l'image plus
		this.removeImpListener();

		// on recupere la vue, le modele et l'imp
		VueScinDyn vue = (VueScinDyn) this.getVue();
		Modele_Renal modele = (Modele_Renal) vue.getFen_application().getControleur().getModele();
		ImagePlus imp = vue.getImpAnt();

		// on debloque le modele
		modele.unlock();

		// capture de l'imageplus ainsi que de l'overlay
		BufferedImage capture = ModeleScin.captureImage(this.getVue().getImp(), 300, 300).getBufferedImage();

		//on ajoute l'imp a la vue
		this.getVue().setImp(imp);

		// on enregistre la mesure pour chaque slice
		this.indexRoi = 0;
		for (int i = 1; i <= imp.getStackSize(); i++) {
			imp.setSlice(i);
			for (int j = 0; j < ORGANES.length; j++) {
				imp.setRoi(getOrganRoi(this.indexRoi));
				String nom = this.getNomOrgane(this.indexRoi);
				modele.enregistrerMesure(this.addTag(nom), imp);
				this.indexRoi++;
			}
		}

		//on calcule les resultats
		modele.calculerResultats();
		
		//on recupere les chartPanels avec l'association
		List<XYSeries> series = modele.getSeries();
		String[][] asso = new String[][] { { "Final KR", "Final KL" } };		
		ChartPanel[] cp = ModeleScin.associateSeries(asso, series);
		
		//on ouvre la fenetre pour ajuster les valeurs
		FenSetValues adjuster = new FenSetValues(cp[0]);
		adjuster.setModal(true);
		adjuster.setVisible(true);
		
		//on passe les valeurs ajustees au modele
		modele.setAdjustedValues(adjuster.getXValues());
		
		//on fait le fit vasculaire avec les donnees collectees
		modele.fitVasculaire();

		//on affiche la fenetre de resultats principale
		new FenResultat_Renal(vue, capture, adjuster.getChartPanelWithOverlay());
		
		if(true) { //TODO condition d'ajout
			new FenOptionalCharts(vue);
		}
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public void notifyClic(ActionEvent arg0) {
		this.maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (this.isOver()) {
			// on renvoie la roi de l'organe uniquement si on a fini de tracer les roi
			return this.roiManager.getRoi(this.indexRoi % Controleur_Renal.ORGANES.length);
		}
		
		// roi de bruit de fond rein droit
		if (this.indexRoi == 1) {
			return this.createBkgRoi(this.indexRoi, new int[] { -1, 1 });
		}

		//roi de bruit de fond rein gauche
		if (this.indexRoi == 3) {
			return this.createBkgRoi(this.indexRoi, new int[] { 1, 1 });
		}

		return null;
	}

	//cree la roi de bruit de fond
	private Roi createBkgRoi(int indexRoi, int[] direction) {
		ImagePlus imp = this.getVue().getImp();

		// on clone la roi du rein
		Roi liver = (Roi) this.roiManager.getRoi(indexRoi - 1).clone();
		// on recupere ses bounds
		Rectangle bounds = liver.getBounds();

		// la taille de la roi bdf correpond a 1/4 de roi organe
		int[] size = { (bounds.width / 4) * direction[0], (bounds.height / 4) * direction[1] };

		Roi liverShifted = (Roi) liver.clone();
		liverShifted.setLocation(liver.getXBase() + size[0], liver.getYBase() + size[1]);
		this.roiManager.addRoi(liverShifted);

		// renvoie une section de la roi
		this.roiManager.setSelectedIndexes(new int[] { indexRoi - 1, this.roiManager.getCount() - 1 });
		this.roiManager.runCommand(imp, "XOR");
		this.roiManager.runCommand(imp, "Split");

		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;
		int w = size[0] * imp.getWidth();
		int h = size[1] * imp.getHeight();

		// permet de diviser la roi
		Rectangle splitter;
		if (w > 0) {
			splitter = new Rectangle(x, y, w, h);
		} else {
			splitter = new Rectangle(x + w, y, -w, h);
		}

		Roi rect = new Roi(splitter);
		this.roiManager.addRoi(rect);

		this.roiManager
				.setSelectedIndexes(new int[] { this.roiManager.getCount() - 1, this.roiManager.getCount() - 2 });
		this.roiManager.runCommand(imp, "AND");

		Roi bkg = (Roi) this.getVue().getImp().getRoi().clone();
		int[] offset = new int[] { size[0] / 4, size[1] / 4 };

		// si le deplacement de la bdf est de moins d'un pixel, on la deplace d'un pixel
		for (int i = 0; i < 2; i++) {
			if (offset[i] == 0)
				offset[i] = direction[i];
		}
		bkg.setLocation(bkg.getXBase() + offset[0], bkg.getYBase() + offset[1]);

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

}
