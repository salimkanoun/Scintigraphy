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
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.CanvasResizer;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class Controleur_Plaquettes extends ControleurScin {

	protected static boolean showLog;
	private Modele_Plaquettes leModele;
	private String[] organes = { "Spleen", "Liver", "Heart" };
	private String[] organesAntPost = { "Spleen Post", "Liver Post", "Heart Post", "Spleen Ant", "Liver Ant", "Heart Ant" };
	private boolean antPost;

	// Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue) {
		super(vue);
		leModele = new Modele_Plaquettes(vue.getDateDebut());
		this.setModele(leModele);
		antPost = vue.antPost;
		
		if (vue.antPost) {
			this.setOrganes(organesAntPost);
		} else {
			this.setOrganes(organes);
		}
	}

	public void fin() {
		
		
		Thread captureThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
				    Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
		});
		captureThread.start();
		
		
		ImagePlus capture = ModeleScin.captureImage(this.getVue().getImp(), 512, 512);
		// On resize le canvas pour etre a la meme taille que les courbes
		ImageProcessor ip = capture.getProcessor();
		CanvasResizer canvas = new CanvasResizer();
		ImageProcessor iptemp = canvas.expandImage(ip, 640, 512, (640 - 512) / 2, 0);
		capture.setProcessor(iptemp);

		JTable tableResultats = leModele.getResults();

		ImagePlus[] courbes = leModele.createDataset(tableResultats);

		ImageStack stack = new ImageStack(640, 512);
		stack.addSlice(capture.getProcessor());
		for (int i = 0; i < courbes.length; i++) {
			stack.addSlice(courbes[i].getProcessor());
		}

		ImagePlus courbesStackImagePlus = new ImagePlus();
		courbesStackImagePlus.setStack(stack);

		ImagePlus courbesFinale = new ImagePlus();

		MontageMaker mm = new MontageMaker();
		courbesFinale = mm.makeMontage2(courbesStackImagePlus, 2, 2, 1, 1, courbesStackImagePlus.getStackSize(), 1, 0,
				false);
		// SK A Reprendre
		FenetreResultat results = new FenetreResultat(courbesFinale, tableResultats);
		results.setVisible(true);
		// laVue.UIResultats(courbesFinale, tableResultats);
	}

	@Override
	public boolean isOver() {
		return this.roiManager.getCount() >= this.getVue().getImp().getStackSize() * 3;
	}

	@Override
	public int getSliceNumberByRoiIndex(int roiIndex) {
		return (roiIndex / 3) + 1;
	}

	@Override
	public boolean isPost() {
		if (antPost)
			return (this.getSliceNumberByRoiIndex(this.getIndexRoi()) % 2 == 1);
		else
			return true;
	}

	@Override
	public Roi getOrganRoi(int lastRoi) {
		if (roiManager.getRoi(getIndexRoi()) == null)
			if (this.getVue().getImp().getCurrentSlice() > 1) {
				return roiManager.getRoi(this.getIndexRoi() - 3);
			}
		return null;
	}

}
