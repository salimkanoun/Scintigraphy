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

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.VueScin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.CanvasResizer;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class Controleur_Plaquettes implements ActionListener {

	private Vue_Plaquettes laVue;
	private Modele_Plaquettes leModele;

	protected static boolean showLog;
	private String tagCapture;

	private String[] organes;
	private int indexRoi;

	// Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue, Modele_Plaquettes leModele, String[] organes) {
		this.laVue = vue;
		this.leModele = leModele;

		this.indexRoi = 0;
		
		this.organes = organes;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();

		if (b == laVue.lesBoutons.get("Suivant")) {
			this.clicSuivant();
		}

		if (b == laVue.lesBoutons.get("Precedent")) {
			this.clicPrecedent();
		}

		if (b == laVue.lesBoutons.get("Capture")) {
			laVue.lesBoutons.get("Capture").setVisible(false);
			laVue.Csv.setText("Provided By Petctviewer.org");
			ImagePlus captureFinale = ModeleScin.captureFenetre(WindowManager.getCurrentImage(), 0, 0);
			WindowManager.getCurrentWindow().getImagePlus().changes = false;
			WindowManager.getCurrentWindow().close();
			// On genere la 2eme partie des tag dicom et on l'ajoute a la 1ere partie dans
			// le property de l'image finale
			captureFinale.setProperty("Info", tagCapture += (ModeleScin.genererDicomTagsPartie2(captureFinale)));
			// On affiche et on agrandie la fenetre de la capture finale
			captureFinale.show();
			// On met un zoom a 80%
			captureFinale.getCanvas().setMagnification(0.8);
			// SK FAIRE GENERATION CSV?

			// On fait la capture finale
			captureFinale.getWindow().toFront();
			// On propose de sauver la capture en DICOM
			IJ.run("myDicom...");
			// fin du programme ici
		}

		if (b == laVue.lesBoutons.get("Draw ROI")) {
			laVue.lesBoutons.get("Draw ROI").setBackground(Color.LIGHT_GRAY);
			laVue.lesBoutons.get("Contrast").setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}

		if (b == laVue.lesBoutons.get("Contrast")) {
			laVue.lesBoutons.get("Draw ROI").setBackground(null);
			laVue.lesBoutons.get("Contrast").setBackground(Color.LIGHT_GRAY);
			IJ.run("Window Level Tool");
		}

		if (b == laVue.lesBoutons.get("Quitter")) {
			laVue.end("");
			return;
		}

		if (b == laVue.lesBoutons.get("Show Log"))

			// Regarder methode de Ping pour changer le libelle des bouttons
			if (!showLog) {
				showLog = true;
				laVue.lesBoutons.get("Show Log").setLabel("Hide Log");
				laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}

			else {
				showLog = false;
				laVue.lesBoutons.get("Show Log").setLabel("Show Log");
				laVue.lesBoutons.get("Show").setBackground(null);
			}
	}

	private void clicPrecedent() {
		// sauvegarde du ROI courant
		this.saveRoi();

		if (this.indexRoi > 0) {
			indexRoi--;
			this.preparerRoi();
		}
	}

	private void clicSuivant() {
		// ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = ModeleScin.genererDicomTagsPartie1(laVue.win.getImagePlus(), "Platelet");
		}

		// sauvegarde du ROI actuel
		this.saveRoi();

		indexRoi++;
		this.preparerRoi();
	}

	private void fin() {
		ImagePlus capture = ModeleScin.captureImage(laVue.win.getImagePlus(), 512, 512);
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
		laVue.UIResultats(courbesFinale, tableResultats);
	}

	private void saveRoi() {
		if (this.laVue.win.getImagePlus().getRoi() != null) { // si il y a une roi sur l'image plus
			int nOrganeCourant = indexRoi % this.organes.length;

			// création du nom du ROI selon la prise post ou ant
			String nomRoi = this.organes[nOrganeCourant];
			if (this.laVue.antPost && (this.indexRoi / this.organes.length) % 2 == 1) {
				nomRoi += " Ant";
			} else {
				nomRoi += " Post";
			}

			// on enregistre la ROI dans le modele
			leModele.enregisterMesure(nomRoi, laVue.win.getImagePlus());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (laVue.roiManager.getRoi(indexRoi) == null) {
				// on ajoute le numero de la slide au nom
				if (this.laVue.antPost) {
					nomRoi += this.indexRoi / (this.organes.length * 2);
				} else {
					nomRoi += this.indexRoi / this.organes.length;
				}
				
				laVue.roiManager.add(laVue.win.getImagePlus(), laVue.win.getImagePlus().getRoi(), indexRoi);
				laVue.roiManager.rename(indexRoi, nomRoi);

			} else { // Si il existe on fait un update
				this.laVue.roiManager.runCommand("Update");
				Roi roiMaj = this.laVue.roiManager.getRoi(indexRoi);
				roiMaj.setPosition(0);
			}

			// on supprime le roi nouvellement ajoute de la vue
			laVue.win.getImagePlus().killRoi();
		}
	}

	private void getOrganRoi() {
		if (this.laVue.roiManager.getRoi(indexRoi) != null) {
			Roi roiOrgane = (Roi) this.laVue.roiManager.getRoi(indexRoi);
			this.laVue.win.getImagePlus().setRoi(roiOrgane);
			this.laVue.roiManager.select(indexRoi);
		} else {
			if (this.laVue.roiManager.getCount() >= this.organes.length) { // Si on n'est pas dans le premier cycle on
																			// reaffiche la Roi preexistante pour cet
																			// organe
				Roi roiOrgane = (Roi) laVue.roiManager.getRoi(this.indexRoi - this.organes.length).clone();
				laVue.win.getImagePlus().setRoi(roiOrgane);
				this.laVue.roiManager.select(this.indexRoi);
			}
		}
	}

	private void clearOverlay() {
		laVue.overlay.clear();
		VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
	}

	private void showSlice() {
		this.afficherRoisSlice();
		int nSlice = (this.indexRoi / this.organes.length);
		laVue.win.showSlice(nSlice + 1);
	}

	private void afficherRoisSlice() {
		this.clearOverlay();
		int nSlice = (this.indexRoi / this.organes.length);
		int indexSliceDebut = nSlice * this.organes.length;
		int indexSliceFin = indexSliceDebut + this.organes.length;

		for (int i = indexSliceDebut; i < indexSliceFin; i++) {
			if (this.laVue.roiManager.getRoi(i) != null) {
				if (i != this.indexRoi) {
					Roi roi = (Roi) this.laVue.roiManager.getRoi(i).clone();
					this.laVue.overlay.add(roi);
				}
			}
		}

	}

	private void preparerRoi() {
		int nOrgane = indexRoi % this.organes.length;

		// si il y a le bon nombre nombre d'image on a fini
		if (this.laVue.roiManager.getCount() >= laVue.win.getImagePlus().getImageStackSize() * this.organes.length) {
			this.fin();
		}

		// affichage de la slice courante
		this.showSlice();
		this.afficherRoisSlice();

		// copie de la roi de l'organe suivant (selon la valeur existante ou celle du
		// meme oragne precdent)
		this.getOrganRoi();

		// affichage des instructions
		nOrgane = indexRoi % this.organes.length;
		this.laVue.setInstructions("Delimit the " + this.organes[nOrgane]);
		
		//selection du roi si il existe
		if(this.laVue.roiManager.getRoi(indexRoi) != null) {
			this.laVue.roiManager.select(indexRoi);
		}
	}
}
