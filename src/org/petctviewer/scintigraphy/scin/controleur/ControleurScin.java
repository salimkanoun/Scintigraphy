package org.petctviewer.scintigraphy.scin.controleur;

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

import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTable;

import org.petctviewer.scintigraphy.scin.view.ModeleScin;
import org.petctviewer.scintigraphy.scin.view.VueScin;
import org.petctviewer.scintigraphy.shunpo.Modele_Shunpo;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.CanvasResizer;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;

public class ControleurScin implements ActionListener {

	private VueScin laVue;
	private ModeleScin leModele;

	protected static boolean showLog;
	private String tagCapture;

	private String[] organes;
	private int indexRoi;

	// Sert au restart
	protected ControleurScin(VueScin vue, ModeleScin leModele, String[] organes) {
		this.laVue = vue;
		this.leModele = leModele;

		this.indexRoi = 0;
		
		this.organes = organes;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();

		if (b == laVue.getFen_application().getBtn_suivant()) {
			this.clicSuivant();
		}

		else if (b == laVue.getFen_application().getBtn_precedent()) {
			this.clicPrecedent();
		}

		if (b == laVue.getFen_application().getBtn_capture()) {
			laVue.getFen_application().getBtn_capture().setVisible(false);
			//laVue.csv.setText("Provided By Petctviewer.org");
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

		if (b == laVue.getFen_application().getBtn_drawROI()) {
			laVue.getFen_application().getBtn_drawROI().setBackground(Color.LIGHT_GRAY);
			laVue.getFen_application().getBtn_contrast().setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}

		if (b == laVue.getFen_application().getBtn_contrast()) {
			laVue.getFen_application().getBtn_contrast().setBackground(null);
			laVue.getFen_application().getBtn_drawROI().setBackground(Color.LIGHT_GRAY);
			IJ.run("Window Level Tool");
		}

		if (b == laVue.getFen_application().getBtn_quitter()) {
			laVue.end();
			return;
		}

		if (b == laVue.getFen_application().getBtn_showlog())

			// Regarder methode de Ping pour changer le libelle des bouttons
			if (!showLog) {
				showLog = true;
				laVue.getFen_application().getBtn_showlog().setLabel("Hide Log");
				//laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}

			else {
				showLog = false;
				laVue.getFen_application().getBtn_showlog().setLabel("Show Log");
				//laVue.lesBoutons.get("Show").setBackground(null);
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
			tagCapture = Modele_Shunpo.genererDicomTagsPartie1(laVue.getFen_application().getImagePlus(), laVue.getExamType());
		}

		// sauvegarde du ROI actuel
		this.saveRoi();

		indexRoi++;
		this.preparerRoi();
	}

	private void fin() {
		//TODO gere le modele
		/**
		ImagePlus capture = Modele_Shunpo.captureImage(laVue.getFen_application().getImagePlus(), 512, 512);
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
	*/
	}

	private void saveRoi() {
		if (this.laVue.getFen_application().getImagePlus().getRoi() != null) { // si il y a une roi sur l'image plus
			int nOrganeCourant = indexRoi % this.organes.length;

			// création du nom du ROI selon la prise post ou ant
			String nomRoi = this.organes[nOrganeCourant];
			if (this.laVue.isAntPost() && (this.indexRoi / this.organes.length) % 2 == 1) {
				nomRoi += " Ant";
			} else {
				nomRoi += " Post";
			}

			// on enregistre la ROI dans le modele
			//TODO
			//leModele.enregisterMesure(nomRoi, laVue.getFen_application().getImagePlus());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (laVue.getRoiManager().getRoi(indexRoi) == null) {
				
				// on ajoute le numero de la slide au nom
				if (this.laVue.isAntPost()) {
					nomRoi += this.indexRoi / (this.organes.length * 2);
				} else {
					nomRoi += this.indexRoi / this.organes.length;
				}
				
				laVue.getRoiManager().add(laVue.getFen_application().getImagePlus(), laVue.getFen_application().getImagePlus().getRoi(), indexRoi);
				laVue.getRoiManager().rename(indexRoi, nomRoi);

			} else { // Si il existe on fait un update.
				this.laVue.getRoiManager().select(indexRoi);
				this.laVue.getRoiManager().runCommand("Update");
			}

			// on supprime le roi nouvellement ajoute de la vue
			laVue.getFen_application().getImagePlus().killRoi();
		}
	}

	private void getOrganRoi() {
		if (this.laVue.getRoiManager().getRoi(indexRoi) != null) {
			Roi roiOrgane = (Roi) this.laVue.getRoiManager().getRoi(indexRoi);
			this.laVue.getFen_application().getImagePlus().setRoi(roiOrgane);
			this.laVue.getRoiManager().select(indexRoi);
		} else {
			if (this.laVue.getRoiManager().getCount() >= this.organes.length) { // Si on n'est pas dans le premier cycle on
																			// reaffiche la Roi preexistante pour cet
																			// organe
				Roi roiOrgane = (Roi) laVue.getRoiManager().getRoi(this.indexRoi - this.organes.length).clone();
				laVue.getFen_application().getImagePlus().setRoi(roiOrgane);
				this.laVue.getRoiManager().select(this.indexRoi);
			}
		}
	}

	private void clearOverlay() {
		laVue.getOverlay().clear();
		VueScin.setOverlayDG(laVue.getOverlay(), laVue.getFen_application().getImagePlus());
	}

	private void showSlice() {
		this.afficherRoisSlice();
		int nSlice = (this.indexRoi / this.organes.length);
		laVue.getFen_application().showSlice(nSlice + 1);
	}

	private void afficherRoisSlice() {
		this.clearOverlay();
		int nSlice = (this.indexRoi / this.organes.length);
		int indexSliceDebut = nSlice * this.organes.length;
		int indexSliceFin = indexSliceDebut + this.organes.length;

		for (int i = indexSliceDebut; i < indexSliceFin; i++) {
			if (this.laVue.getRoiManager().getRoi(i) != null) {
				if (i != this.indexRoi) {
					Roi roi = (Roi) this.laVue.getRoiManager().getRoi(i).clone();
					this.laVue.getOverlay().add(roi);
				}
			}
		}

	}

	private void preparerRoi() {
		int nOrgane = indexRoi % this.organes.length;

		// si il y a le bon nombre nombre d'image on a fini
		if (this.laVue.getRoiManager().getCount() >= laVue.getFen_application().getImagePlus().getImageStackSize() * this.organes.length) {
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
		this.laVue.getFen_application().setInstructions("Delimit the " + this.organes[nOrgane]);
	}
}
