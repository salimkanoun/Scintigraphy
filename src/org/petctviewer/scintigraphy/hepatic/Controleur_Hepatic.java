package org.petctviewer.scintigraphy.hepatic;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.gui.Roi;

public class Controleur_Hepatic extends ControleurScin {
	
	public static String[] organes = {"Liver", "Intestine"};

	protected Controleur_Hepatic(VueScin vue) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_Hepatic(vue.getImp()));
	}

	@Override
	public boolean isOver() {
		return this.getOrganes().length == this.roiManager.getCount();
	}

	@Override
	public void fin() {
		//Copie des rois sur la deuxieme slice
		for(int i = 0; i < 2; i++) {
			this.indexRoi++;
			this.preparerRoi();
			this.saveCurrentRoi(this.getNomOrgane(indexRoi), indexRoi);
		}
		
		this.getModele().calculerResultats();
		System.out.println(this.getModele());
		
		System.out.println("Fin du programme");
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		if(isPost()) {
			return 2;
		}else {
			return 1;
		}
	}

	@Override
	public boolean isPost() {
		return this.getIndexRoi() > 1;
	}

	@Override
	public Roi getOrganRoi() {
		if(this.isPost()) {
			return this.roiManager.getRoi(this.getIndexRoi() - 2);
		}
		return null;
	}

}
