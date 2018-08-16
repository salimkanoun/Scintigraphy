package org.petctviewer.scintigraphy.esophageus.application;

import java.awt.Color;

import org.petctviewer.scintigraphy.esophageus.EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.FenResultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class Controleur_EsophagealTransit  extends ControleurScin {
 
	public static String[] ORGANES = {"Esophage"};
	private int nbOrganes;
	EsophagealTransit esoPlugin;
	Modele_EsophagealTransit modele ;
	public Controleur_EsophagealTransit(EsophagealTransit esoPlugin, ImagePlus [][] sauvegardeImagesSelectDicom, EsophagealTransit esoPlugIn) {
		super(esoPlugin);
		this.esoPlugin = esoPlugin;
		this.setOrganes(ORGANES);
		this.nbOrganes = ORGANES.length;
		
		 modele = new Modele_EsophagealTransit(esoPlugin.getFrameDurations(), sauvegardeImagesSelectDicom,  esoPlugIn);
		
		//modele.setLocked(true);
		this.setModele(modele);
		this.tools = Toolbar.RECTANGLE;
	}

	@Override
	public boolean isOver() {
		//System.out.println("ISOVER ?  indexroi ="+indexRoi+" et stacksize ="+this.esoPlugin.getImp().getStackSize());
		if((this.indexRoi+1)>=this.esoPlugin.getImp().getStackSize()) {
		//	System.out.println("ISOVER !!");
		}
		return (this.indexRoi+1)>=this.esoPlugin.getImp().getStackSize();
	}

	@Override
	public void fin() {
	//	System.out.println("fin");
		((Modele_EsophagealTransit) this.getModele()).setRoiManager(this.roiManager);
		
		
		this.getModele().calculerResultats();
		
		FenResultats_EsophagealTransit fen = new FenResultats_EsophagealTransit(((Modele_EsophagealTransit)this.getModele()).getExamenMean(), ((Modele_EsophagealTransit)this.getModele()).getDicomRoi(), modele);
		fen.setVisible(true);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return roiIndex+1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPost() {
		return false;
	}
	
	/**
	 * Prepare la roi qui se situera a indexRoi
	 */
	@Override
	public void preparerRoi(int lastRoi) {
		// on affiche la slice
		int indexSlice = this.getSliceNumberByRoiIndex(this.indexRoi);
		this.setSlice(indexSlice);
		
	//	System.out.println("ici");
		

	}

	
	
}
