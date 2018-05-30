package org.petctviewer.scintigraphy.scin.basic;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;

public class VueScin_Basic extends VueScin {

	private String[] organes;
	private CustomControleur cusCtrl;

	public VueScin_Basic(String[] organes, CustomControleur cusCtrl) {
		super("Scintigraphy");
		this.organes = organes;
		this.cusCtrl = cusCtrl;
		this.run("");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if (titresFenetres.length > 1) {
			IJ.log("There must be exactly one dicom opened");
		}

		ImagePlus imp = WindowManager.getImage(titresFenetres[0]);
		String info = imp.getInfoProperty();
		
		System.out.println(Arrays.toString(VueScin.sortAntPost(new ImagePlus[] {imp})));
		ImagePlus impSorted = VueScin.sortImageAntPost(imp);
		impSorted.setProperty("Info", info);
		imp.close();
		
		impSorted.show();

		this.setImp(impSorted.duplicate());

		VueScin.setCustomLut(this.getImp());
		FenApplication app = new FenApplication(this.getImp(), this.getExamType());
		app.setVisible(true);
		this.setFenApplication(app);
		
		Controleur_Basic ctrl = new Controleur_Basic(this, this.organes);
		ctrl.setCustomControleur(this.cusCtrl);
		this.getFenApplication().setControleur(ctrl);
	}
	
	public HashMap<String, Double> getData() {
		return ((Modele_Basic) this.getFenApplication().getControleur().getModele()).getData();
	}
	
	public BufferedImage getCapture() {
		return null;
	}

}
