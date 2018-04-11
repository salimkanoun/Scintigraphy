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

	private int etape;
	private int cycle;
	private boolean modeAnt;
	private boolean fini;	
	private String[] organes;
	private int indexRoi;

	// Sert au restart
	protected Controleur_Plaquettes(Vue_Plaquettes vue, Modele_Plaquettes leModele, String[] organes) {
		this.laVue = vue;
		this.leModele = leModele;

		this.etape = 0;
		this.indexRoi = 0;
		this.cycle = 0;
		this.modeAnt = false;
		this.organes = organes;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();

		if (b == laVue.lesBoutons.get("Suivant")) {
			this.clicSuivant();
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

		if (b == laVue.lesBoutons.get("Precedent")) {
			//TODO
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

	private void clicSuivant() {		
		//ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = Modele_Shunpo.genererDicomTagsPartie1(laVue.win.getImagePlus(), "Platelet");
		}

		//création du nom du ROI selon la prise post ou ant
		String nomRoi = this.organes[this.etape];
		if(this.modeAnt) {
			nomRoi += " Ant";
		}else {
			nomRoi += " Post";
		}
		
		//sauvegarde du ROI
		this.saveRoi(nomRoi);
		
		//affichage des ROI précédents
		this.displayRois();

		this.etape++;
		
		//Changement d'étape et/ou de cycle
		if(this.etape >= this.organes.length) {
			this.etape = 0;
			this.nextSlice();
			this.modeAnt = this.laVue.antPost && !this.modeAnt;
			
			if(!this.modeAnt) {
				this.cycle++;
			}
			
			//si il y a le bon nombre nombre d'image
			if(laVue.win.getImagePlus().getImageStackSize()  * this.organes.length == this.laVue.roiManager.getRoisAsArray().length) {
				this.fin();
			}
		}
		
		this.laVue.setInstructions("Delimit the " + this.organes[this.etape]);
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
		courbesFinale = mm.makeMontage2(courbesStackImagePlus, 2, 2, 1, 1, courbesStackImagePlus.getStackSize(), 1,
				0, false);
		IJ.log("apres Montage");
		laVue.UIResultats(courbesFinale, tableResultats);
	}

	private void addRoi(String nom) {
		// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
		// pour eviter les doublons
		if (laVue.roiManager.getRoi(indexRoi) == null) {
			laVue.roiManager.add(laVue.win.getImagePlus(), laVue.win.getImagePlus().getRoi(), indexRoi);
			laVue.roiManager.rename(indexRoi, nom);
		}
		// Si elle existe on fait un update. Si elle a é– ï¿½ perdue dans l'imagePlus on
		// revient a la ROI sauvegardee et on notifie l'utilisateur
		else {
			if (laVue.win.getImagePlus().getRoi() == null) {
				IJ.showMessage("Roi lost, restoring previous saved ROI");
				laVue.roiManager.select(indexRoi);
			}
			laVue.roiManager.runCommand("Update");
		}
		indexRoi++;
	}

	private void saveRoi(String nomRoi) {
		addRoi(nomRoi + this.cycle);
		leModele.enregisterMesure(nomRoi, laVue.win.getImagePlus());
		// index -1 car add roi incremente l'index de 1
		laVue.overlay.add(laVue.roiManager.getRoi(this.indexRoi - 1));
		laVue.win.getImagePlus().killRoi();
	}

	private void displayRois() {
		// Si la ROI suivante est deja presente on l'affiche
		if (laVue.roiManager.getRoi(this.indexRoi) != null) {
			laVue.roiManager.select(this.indexRoi);
			return;
		}
		
		// Si on n'est pas dans le premier cycle on reaffiche les Roi preexistantes
		if (this.laVue.roiManager.getRoisAsArray().length >= this.organes.length){
			laVue.win.getImagePlus().setRoi((Roi) laVue.roiManager.getRoi(this.indexRoi - this.organes.length).clone());
		}
	}

	private void nextSlice() {
		laVue.overlay.clear();
		VueScin.setOverlayDG(laVue.overlay, laVue.win.getImagePlus());
		laVue.win.showSlice(laVue.win.getImagePlus().getCurrentSlice() + 1);
	}

}
