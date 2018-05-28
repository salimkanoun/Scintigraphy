package org.petctviewer.scintigraphy.dmsa;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Controleur_Dmsa extends ControleurScin {

	public static String[] ORGANES = { "L. Kidney", "L. bkg", "R. Kidney", "R. bkg" };
	private int maxIndexRoi = 0;

	private boolean antPost;
	private boolean over;
	
	protected Controleur_Dmsa(VueScin vue) {
		super(vue);
		
		this.antPost = vue.getImp().getNSlices() == 2;
		if (antPost) {
			this.setSlice(2);
			String[] organes = new String[ORGANES.length];
			for (int i = 0; i < organes.length; i++) {
				organes[i] = ORGANES[i%ORGANES.length];
			}
			this.setOrganes(organes);
		}
		
		Modele_Dmsa modele = new Modele_Dmsa();
		this.setModele(modele);
	}
	
	// cree la roi de bruit de fond
	private Roi createBkgRoi(int indexRoi) {
		ImagePlus imp = this.getVue().getImp();

		int indexLiver;
		// on clone la roi du rein
		if (RenalSettings.getSettings()[1]) {
			// si on trace des corticales, il faut decaler de deux
			indexLiver = indexRoi - 2;
		} else {
			indexLiver = indexRoi - 1;
		}

		// largeur a prendre autour du rein
		int largeurBkg = 1;
		if (this.getVue().getImp().getDimensions()[0] >= 128) {
			largeurBkg = 2;
		}
		
		//TODO refactor pour eviter le copier colle

		this.roiManager.select(indexLiver);
		IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
		this.roiManager.addRoi(imp.getRoi());

		this.roiManager.select(this.roiManager.getCount() - 1);
		IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
		this.roiManager.addRoi(imp.getRoi());

		this.roiManager
				.setSelectedIndexes(new int[] { this.roiManager.getCount() - 2, this.roiManager.getCount() - 1 });
		this.roiManager.runCommand(imp, "XOR");

		Roi bkg = imp.getRoi();

		// on supprime les rois de construction
		while (this.roiManager.getCount() - 1 > this.maxIndexRoi ) {
			this.roiManager.select(this.roiManager.getCount() - 1);
			this.roiManager.runCommand(imp, "Delete");
		}
		
		return bkg;
	}

	@Override
	public boolean isOver() {
		return indexRoi == this.getOrganes().length - 1;
	}

	@Override
	public void fin() {
		this.over = true;
		
		BufferedImage capture = ModeleScin.captureImage(getVue().getImp(), 400, 400).getBufferedImage();
		
		if(this.antPost) {
			this.setSlice(1);
			for(Roi roi : this.roiManager.getRoisAsArray()) {
				this.indexRoi++;
				this.getVue().getImp().setRoi(roi);
				String nom = this.getNomOrgane(indexRoi);
				this.getModele().enregistrerMesure(this.addTag(nom), this.getVue().getImp());
			}
		}
		
		this.getModele().calculerResultats();
		
		new FenResultats_Dmsa(this.getVue(), capture);
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return 2;
	}
	
	//TODO REFACTORISER
	@Override
	public void setSlice(int indexSlice) {
		super.setSlice(indexSlice);
		this.hideLabel("R. bkg", Color.GRAY);
		this.hideLabel("L. bkg", Color.GRAY);
	}

	private void hideLabel(String name, Color c) {
		Overlay ov = this.getVue().getImp().getOverlay();
		Roi roi = ov.get(ov.getIndex(name));
		if (roi != null) {
			roi.setName("");
			roi.setStrokeColor(c);
		}
	}
	
	@Override
	public void notifyClic(ActionEvent arg0) {
		this.maxIndexRoi = Math.max(this.maxIndexRoi, this.indexRoi);
	}
	
	@Override
	public Roi getOrganRoi(int lastRoi) {
		if(this.indexRoi == 1 | this.indexRoi == 3) {
			return this.createBkgRoi(indexRoi);
		}
		return null;
	}

	@Override
	public boolean isPost() {
		if(this.over) {
			return this.getVue().getImp().getCurrentSlice() == 2;
		}
		return true;
	}

}
