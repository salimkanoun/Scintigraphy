package org.petctviewer.scintigraphy.hepaticdyn;

import java.awt.image.BufferedImage;

import org.petctviewer.scintigraphy.hepatic.Modele_Hepatic;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_HepaticDyn extends ControleurScin {

	public static String[] organes = { "Blood pool", "Liver R", "Liver L" };

	protected Controleur_HepaticDyn(VueScin vue) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_HepaticDyn(vue.getImp()));
	}

	@Override
	public boolean isOver() {
		return indexRoi >= 2;
	}

	@Override
	public void fin() {
		Vue_HepaticDyn vue = (Vue_HepaticDyn) this.getVue();
		
		ImagePlus imp = vue.getImp();
		BufferedImage capture = ModeleScin.captureImage(imp, 256, 256).getBufferedImage();
		
		//on copie les roi sur toutes les slices
		vue.setImp(vue.getImpAnt());
		vue.getFen_application().setImage(vue.getImpAnt());
		for (int i = 1; i <= vue.getImpAnt().getStackSize(); i++) {
			for (int j = 0; j < this.getOrganes().length; j++) {
				this.indexRoi++;
				this.preparerRoi();
				this.saveCurrentRoi(this.getNomOrgane(indexRoi), indexRoi);
			}
		}
		
		new FenResultat_HeptaticDyn(vue, capture);
		
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return roiIndex / this.getOrganes().length;
	}

	@Override
	public Roi getOrganRoi() {
		return this.roiManager.getRoi(indexRoi - 3);
	}

	@Override
	public boolean isPost() {
		return false;
	}

}
