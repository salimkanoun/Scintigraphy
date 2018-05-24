package org.petctviewer.scintigraphy.hepatic.dyn;

import java.awt.image.BufferedImage;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import ij.ImagePlus;
import ij.gui.Roi;

public class Controleur_HepaticDyn extends ControleurScin {

	public static String[] organes = { "Blood pool", "Liver R", "Liver L" };

	protected Controleur_HepaticDyn(Vue_HepaticDyn vue) {
		super(vue);
		this.setOrganes(organes);
		this.setModele(new Modele_HepaticDyn(vue));
	}

	@Override
	public boolean isOver() {
		return this.indexRoi >= 2;
	}

	@Override
	public void fin() {
		Vue_HepaticDyn vue = (Vue_HepaticDyn) this.getVue();
		
		ImagePlus imp = vue.getImp();
		BufferedImage capture = ModeleScin.captureImage(imp, 300, 300).getBufferedImage();
		
		//on copie les roi sur toutes les slices
		for (int i = 1; i <= vue.getImpAnt().getStackSize(); i++) {
			vue.getImpAnt().setSlice(i);
			for (int j = 0; j < this.getOrganes().length; j++) {
				vue.getImpAnt().setRoi(getOrganRoi(this.indexRoi));
				this.getModele().enregistrerMesure(this.addTag(this.getNomOrgane(this.indexRoi)), vue.getImpAnt());
				this.indexRoi++;
			}
		}
		
		this.getModele().calculerResultats();
		new FenResultat_HepaticDyn(vue, capture);
		this.getVue().getFenApplication().dispose();
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 1;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		return this.roiManager.getRoi(this.indexRoi%3);
	}

	@Override
	public boolean isPost() {
		return false;
	}

}
