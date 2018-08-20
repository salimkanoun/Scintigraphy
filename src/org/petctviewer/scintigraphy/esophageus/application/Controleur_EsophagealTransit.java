package org.petctviewer.scintigraphy.esophageus.application;

import org.petctviewer.scintigraphy.esophageus.EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ControleurScin;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class Controleur_EsophagealTransit  extends ControleurScin {
 
	public static String[] ORGANES = {"Esophage"};
	EsophagealTransit esoPlugin;
	Modele_EsophagealTransit modele ;
	
	public Controleur_EsophagealTransit(EsophagealTransit esoPlugin, ImagePlus [][] sauvegardeImagesSelectDicom) {
		super(esoPlugin);
		this.esoPlugin = esoPlugin;
		this.setOrganes(ORGANES);
		modele = new Modele_EsophagealTransit(sauvegardeImagesSelectDicom,  esoPlugin);
		this.setModele(modele);
		this.tools = Toolbar.RECTANGLE;
	}

	@Override
	public boolean isOver() {
		return (this.indexRoi+1)>=this.esoPlugin.getImp().getStackSize();
	}

	@Override
	public void fin() {
		((Modele_EsophagealTransit) this.getModele()).setRoiManager(this.roiManager);
		this.getModele().calculerResultats();
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(((Modele_EsophagealTransit)this.getModele()).getExamenMean(), ((Modele_EsophagealTransit)this.getModele()).getDicomRoi(), modele);
		fen.pack();
		fen.setVisible(true);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		System.out.println("SliceCalled"+roiIndex);
		return (roiIndex+1);
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		System.out.println("lastRoi called"+lastRoi);
		System.out.println("Roi Manager Lenght"+roiManager.getRoisAsArray().length);
		
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
