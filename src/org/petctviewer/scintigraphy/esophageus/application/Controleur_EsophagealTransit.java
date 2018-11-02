package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ControleurScin;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class Controleur_EsophagealTransit  extends ControleurScin {
 
	public static String[] ORGANES = {"Esophage"};
	EsophagealTransit esoPlugin;
	Modele_EsophagealTransit modele ;
	
	public Controleur_EsophagealTransit(EsophagealTransit esoPlugin) {
		super(esoPlugin);
		this.esoPlugin = esoPlugin;
		this.setOrganes(ORGANES);
		modele=(Modele_EsophagealTransit) esoPlugin.getModele();
		this.tools = Toolbar.RECTANGLE;
	}

	@Override
	public boolean isOver() {
		return (this.indexRoi+1)>=this.esoPlugin.getImp().getStackSize();
	}

	@Override
	public void fin() {
		modele.setRoiManager(this.roiManager);
		modele.calculerResultats();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(modele.getExamenMean(), modele.getDicomRoi(), modele);
		fen.pack();
		fen.setLocationRelativeTo(null);
		fen.setVisible(true);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return (roiIndex+1);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		
		if (indexRoi > 0 && lastRoi < this.indexRoi) {
			return roiManager.getRoi(lastRoi);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		return false;
	}

	
	
}
