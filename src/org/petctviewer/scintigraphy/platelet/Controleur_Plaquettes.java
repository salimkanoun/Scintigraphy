/*
Copyright (C) 2017 KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy.platelet;

import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.CanvasResizer;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class Controleur_Plaquettes extends ControleurScin {

	protected static boolean showLog;
	private Modele_Plaquettes leModele;
	private String[] organes = {"Spleen", "Liver", "Heart"};
	private String[] organesAntPost = {"Spleen Post", "Liver Post", "Heart Post", "Spleen Ant", "Liver Ant", "Heart Ant"};
	private boolean antPost;
	private int indexRoi;

	// Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue) {
		super(vue);
		leModele=new Modele_Plaquettes(vue.getDateDebut());
		this.setModele(leModele);
		this.indexRoi = 0;
		antPost=vue.antPost;
		if (vue.antPost) this.setOrganes(organes); else this.setOrganes(organesAntPost);
		
	}


	public void fin() {
		ImagePlus capture = ModeleScin.captureImage(getImp(), 512, 512);
		// On resize le canvas pour etre a la meme taille que les courbes
		ImageProcessor ip = capture.getProcessor();
		CanvasResizer canvas = new CanvasResizer();
		ImageProcessor iptemp = canvas.expandImage(ip, 640, 512, (640 - 512) / 2, 0);
		capture.setProcessor(iptemp);
		IJ.log("avant get results");

		JTable tableResultats = leModele.getResults();

		IJ.log("apres get results");
		ImagePlus[] courbes = leModele.createDataset(tableResultats);

		ImageStack stack = new ImageStack(640, 512);
		stack.addSlice(capture.getProcessor());
		for (int i = 0; i < courbes.length; i++) {
			stack.addSlice(courbes[i].getProcessor());
		}
		IJ.log("Apres add image stack");

		ImagePlus courbesStackImagePlus = new ImagePlus();
		courbesStackImagePlus.setStack(stack);

		ImagePlus courbesFinale = new ImagePlus();
		IJ.log("Avan Montage");
		MontageMaker mm = new MontageMaker();
		courbesFinale = mm.makeMontage2(courbesStackImagePlus, 2, 2, 1, 1, courbesStackImagePlus.getStackSize(), 1, 0,
				false);
		IJ.log("apres Montage");
		FenetreResultat results=new FenetreResultat(courbesFinale, tableResultats);
		results.setVisible(true);
		//laVue.UIResultats(courbesFinale, tableResultats);
	}

	public Roi getOrganRoi() {
		Roi roiOrgane =null;
		if (roiManager.getRoi(indexRoi) != null) {
			roiOrgane = (Roi) roiManager.getRoi(indexRoi);
			getImp().setRoi(roiOrgane);
			roiManager.select(indexRoi);
		} else {
			if (roiManager.getCount() >= this.organes.length) { // Si on n'est pas dans le premier cycle on
																			// reaffiche la Roi preexistante pour cet
																			// organe
				roiOrgane = (Roi) roiManager.getRoi(this.indexRoi - this.organes.length).clone();
				getImp().setRoi(roiOrgane);
				roiManager.select(this.indexRoi);
			}
		}
		return roiOrgane;
	}

	
	private void showSlice() {
		this.afficherRoisSlice();
		int nSlice = (this.indexRoi / this.organes.length);
		this.getVue().getFen_application().showSlice(nSlice + 1);
	}

	private void afficherRoisSlice() {
		this.clearOverlay();
		int nSlice = (this.indexRoi / this.organes.length);
		int indexSliceDebut = nSlice * this.organes.length;
		int indexSliceFin = indexSliceDebut + this.organes.length;

		for (int i = indexSliceDebut; i < indexSliceFin; i++) {
			if (roiManager.getRoi(i) != null) {
				if (i != this.indexRoi) {
					Roi roi = (Roi) roiManager.getRoi(i).clone();
					this.getVue().getImp().getOverlay().add(roi);
				}
			}
		}

	}


	@Override
	public boolean isOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isPost() {
		if (antPost) return (this.getIndexRoi() % 2 == 1);
		else return true;
	}
}
