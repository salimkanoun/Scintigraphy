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
import org.petctviewer.scintigraphy.shunpo.Modele_Shunpo;
import org.petctviewer.scintigraphy.view.VueScin;
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

	private boolean modeAnt;
	private String[] organes;
	private int indexRoi;

	// Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue, Modele_Plaquettes leModele, String[] organes) {
		this.laVue = vue;
		this.leModele = leModele;

		this.indexRoi = 0;

		this.modeAnt = false;
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
			ImagePlus captureFinale = Modele_Shunpo.captureFenetre(WindowManager.getCurrentImage(), 0, 0);
			WindowManager.getCurrentWindow().getImagePlus().changes = false;
			WindowManager.getCurrentWindow().close();
			// On genere la 2eme partie des tag dicom et on l'ajoute a la 1ere partie dans
			// le property de l'image finale
			captureFinale.setProperty("Info", tagCapture += (Modele_Shunpo.genererDicomTagsPartie2(captureFinale)));
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
		if (this.indexRoi > 0) {
			this.indexRoi--;
			this.laVue.roiManager.select(this.indexRoi);

			int nOrganeCourant = indexRoi % this.organes.length;
			this.laVue.setInstructions("Delimit the " + this.organes[nOrganeCourant]);

			this.clearOverlay();

			this.laVue.roiManager.select(indexRoi);
			this.laVue.win.getImagePlus().setRoi((Roi) laVue.roiManager.getRoi(this.indexRoi).clone());
		}
	}

	private void clicSuivant() {
		// ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = Modele_Shunpo.genererDicomTagsPartie1(laVue.win.getImagePlus(), "Platelet");
		}

		int nOrganeCourant = indexRoi % this.organes.length;
		this.laVue.setInstructions("Delimit the " + this.organes[(nOrganeCourant + 1) % this.organes.length]);

		// création du nom du ROI selon la prise post ou ant
		String nomRoi = this.organes[nOrganeCourant];
		if (this.modeAnt) {
			nomRoi += " Ant";
		} else {
			nomRoi += " Post";
		}

		// sauvegarde du ROI
		this.saveRoi(nomRoi);

		// copie de la roi de ce meme organe lors du cycle precedent
		this.getOrganRoi();

		// Changement d'étape et/ou de cycle
		if (nOrganeCourant >= this.organes.length - 1) {
			this.nextSlice();

			// passe en mode ant
			this.modeAnt = this.laVue.antPost && !this.modeAnt;

			// si il y a le bon nombre nombre d'image
			if (laVue.win.getImagePlus().getImageStackSize()
					* this.organes.length == this.laVue.roiManager.getRoisAsArray().length) {
				this.fin();
			}
		}

		indexRoi++;
	}

	private void fin() {
		ImagePlus capture = Modele_Shunpo.captureImage(laVue.win.getImagePlus(), 512, 512);
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

	private void saveRoi(String nomRoi) {

		// on enregistre la ROI dans le modele
		leModele.enregisterMesure(nomRoi, laVue.win.getImagePlus());

		// on ajoute le numero de la slide
		if (this.laVue.antPost) {
			nomRoi += this.indexRoi / (this.organes.length * 2);
		} else {
			nomRoi += this.indexRoi / this.organes.length;
		}

		// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
		// pour eviter les doublons
		if (laVue.roiManager.getRoi(indexRoi) == null) {
			laVue.roiManager.add(laVue.win.getImagePlus(), laVue.win.getImagePlus().getRoi(), indexRoi);
			laVue.roiManager.rename(indexRoi, nomRoi);
		}
		// Si elle existe on fait un update. Si elle a perdue dans l'imagePlus on
		// revient a la ROI sauvegardee et on notifie l'utilisateur
		else {
			if (laVue.win.getImagePlus().getRoi() == null) {
				IJ.showMessage("Roi lost, restoring previous saved ROI");
				laVue.roiManager.select(indexRoi);
			}
			laVue.roiManager.runCommand("Update");
		}

		// affichage de la roi dans l'overlay
		laVue.overlay.add(laVue.roiManager.getRoi(this.indexRoi));
		laVue.win.getImagePlus().killRoi();
	}

	private void getOrganRoi() {
		if (this.laVue.roiManager.getRoi(indexRoi) != null) {
			Roi roiOrgane = (Roi) this.laVue.roiManager.getRoi(indexRoi).clone();
			laVue.win.getImagePlus().setRoi(roiOrgane);
		} else if (this.laVue.roiManager.getCount() >= this.organes.length) { // Si on n'est pas dans le premier cycle on reaffiche la Roi preexistante pour cet organe
			Roi roiOrgane = (Roi) laVue.roiManager.getRoi(this.indexRoi - this.organes.length + 1).clone();
			laVue.win.getImagePlus().setRoi(roiOrgane);
		}
	}

	private void clearOverlay() {
		laVue.overlay.clear();
		VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
	}

	private void nextSlice() {
		this.clearOverlay();
		laVue.win.showSlice(laVue.win.getImagePlus().getCurrentSlice() + 1);
	}

	private void previousSlice() {
		this.clearOverlay();
		laVue.win.showSlice(laVue.win.getImagePlus().getCurrentSlice() - 1);
	}
}
