package org.petctviewer.scintigraphy.esophageus;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Roi;

public class Controleur_EsophagealTransit  extends ControleurScin {
 
	

	protected Controleur_EsophagealTransit(EsophagealTransit esoPlugin) {
		super(esoPlugin);
		
		Modele_EsophagealTransit modele = new Modele_EsophagealTransit(esoPlugin.getFrameDurations());
		this.setModele(modele);
	}

	@Override
	public boolean isOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void fin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPost() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
