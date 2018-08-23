package org.petctviewer.scintigraphy.esophageus;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Roi;

public class ControleurDynamique_EsophagealTransit extends ControleurScin {

	protected ControleurDynamique_EsophagealTransit(Scintigraphy scin) {
		super(scin);
		// TODO Auto-generated constructor stub
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
	public boolean isPost() {
		// TODO Auto-generated method stub
		return false;
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

}
