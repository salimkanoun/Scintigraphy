package org.petctviewer.scintigraphy.statics;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.gui.Roi;

public class ControleurScinStatic extends ControleurScin{

	protected ControleurScinStatic(Scintigraphy vue) {
		super(vue);
		
		ModeleScinStatic modele = new ModeleScinStatic();
		this.setModele(modele);
		
		this.setOrganes(new String[] {"organe 1", "pied", "bouche", "organe2"});
	}

	@Override
	public boolean isOver() {
		return false;
	}

	@Override
	public void fin() {
		//TODO
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		return null;
	}

	@Override
	public boolean isPost() {
		return this.getScin().getImp().getCurrentSlice() == 2;
	}

}
