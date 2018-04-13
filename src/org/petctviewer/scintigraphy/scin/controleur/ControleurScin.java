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
import org.petctviewer.scintigraphy.scin.view.ModeleScin;
import org.petctviewer.scintigraphy.scin.view.VueScin;
import org.petctviewer.scintigraphy.shunpo.Modele_Shunpo;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.frame.RoiManager;

public abstract class ControleurScin implements ActionListener {

	private VueScin laVue;
	private ModeleScin leModele;

	protected static boolean showLog;
	private String tagCapture;

	private String[] organes;
	private int indexRoi;
	
	private int nbContamination;

	// Sert au restart
	protected ControleurScin(VueScin vue, ModeleScin leModele) {
		this.laVue = vue;
		this.leModele = leModele;

		this.indexRoi = 0;
		this.nbContamination = 0;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Button b = (Button) arg0.getSource();

		if (b == laVue.getFen_application().getBtn_suivant()) {
			this.clicSuivant();
		}
		
		else if(b == laVue.getFen_application().getBtn_newCont()) {
			this.clicNewCont();
		}
		
		else if(b == laVue.getFen_application().getBtn_continue()) {
			this.laVue.getFen_application().stopContaminationMode();
		}

		else if (b == laVue.getFen_application().getBtn_precedent()) {
			this.clicPrecedent();
		}

		else if (b == laVue.getFen_application().getBtn_capture()) {
			laVue.getFen_application().getBtn_capture().setVisible(false);
			// laVue.csv.setText("Provided By Petctviewer.org");
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

		else if (b == laVue.getFen_application().getBtn_drawROI()) {
			laVue.getFen_application().getBtn_drawROI().setBackground(Color.LIGHT_GRAY);
			laVue.getFen_application().getBtn_contrast().setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}

		else if (b == laVue.getFen_application().getBtn_contrast()) {
			laVue.getFen_application().getBtn_contrast().setBackground(null);
			laVue.getFen_application().getBtn_drawROI().setBackground(Color.LIGHT_GRAY);
			IJ.run("Window Level Tool");
		}

		else if (b == laVue.getFen_application().getBtn_quitter()) {
			laVue.end();
			return;
		}

		else if (b == laVue.getFen_application().getBtn_showlog())

			// Regarder methode de Ping pour changer le libelle des bouttons
			if (!showLog) {
				showLog = true;
				laVue.getFen_application().getBtn_showlog().setLabel("Hide Log");
				// laVue.lesBoutons.get("Show").setBackground(Color.LIGHT_GRAY);
			}

			else {
				showLog = false;
				laVue.getFen_application().getBtn_showlog().setLabel("Show Log");
				// laVue.lesBoutons.get("Show").setBackground(null);
			}
	}

	private void clicNewCont() {
		String name = "Contamination"+this.nbContamination;
		
		this.saveCurrentRoi(name);
		
		//Roi roi = this.getShownRoi();
		//roi.setName(name);
		//this.ajouterRoiOVerlay(roi);
		
		this.nbContamination++;
		this.preparerRoi();
	}

	private void clicPrecedent() {
		// sauvegarde du ROI courant
		this.saveCurrentRoi(this.createNomRoi());

		if (this.indexRoi > 0) {
			indexRoi--;
			this.preparerRoi();
		}else {
			//si c'est le dernier roi, on desactive le bouton
			this.getVue().getFen_application().getBtn_precedent().setEnabled(false);
		}
	}

	private void clicSuivant() {
		//on active le bouton precedent
		this.getVue().getFen_application().getBtn_precedent().setEnabled(true);
		
		// ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = Modele_Shunpo.genererDicomTagsPartie1(laVue.getFen_application().getImagePlus(),
					laVue.getExamType());
		}

		// sauvegarde du ROI actuel
		this.saveCurrentRoi(this.createNomRoi());

		if (this.isOver()) {
			fin();
		}

		indexRoi++;
		this.preparerRoi();
	}

	public abstract boolean isOver();

	public abstract void fin();
	
	public abstract String createNomRoi();
	
	public abstract int createNumeroRoi();
	
	public abstract void preparerRoi();
	
	public abstract Roi[] getRoisSlice(int nSlide);

	// renvoie true si la prise est post, false si elle est ant
	public abstract boolean isPost();

	public void saveCurrentRoi(String nomRoi) {
		if (this.laVue.getFen_application().getImagePlus().getRoi() != null) { // si il y a une roi sur l'image plus

			//TODO on enregistre la ROI dans le modele 
			// leModele.enregisterMesure(nomRoi, laVue.getFen_application().getImagePlus());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (laVue.getRoiManager().getRoi(indexRoi) == null) {

				nomRoi += this.createNumeroRoi();

				laVue.getRoiManager().add(laVue.getFen_application().getImagePlus(),
						laVue.getFen_application().getImagePlus().getRoi(), indexRoi);
				laVue.getRoiManager().rename(indexRoi, nomRoi);

			} else { // Si il existe on fait un update
				this.laVue.getRoiManager().select(indexRoi);
				this.laVue.getRoiManager().runCommand("Update");

				// on supprime le roi nouvellement ajoute de la vue
				laVue.getFen_application().getImagePlus().killRoi();
			}
		}

	}

	public void afficherInstruction() {
		// affichage des instructions
		int nOrgane = indexRoi % this.organes.length;
		this.getVue().getFen_application().setInstructions(nOrgane);
	}

	public void getOrganRoi() {
		if (this.laVue.getRoiManager().getRoi(indexRoi) != null) {
			Roi roiOrgane = (Roi) this.laVue.getRoiManager().getRoi(indexRoi);
			this.laVue.getFen_application().getImagePlus().setRoi(roiOrgane);
			this.laVue.getRoiManager().select(indexRoi);
		} else {
			if (this.laVue.getRoiManager().getCount() >= this.organes.length) { // Si on n'est pas dans le premier cyc
																				// reaffiche la Roi preexistante pour c
																				// organe
				Roi roiOrgane = (Roi) laVue.getRoiManager().getRoi(this.indexRoi - this.organes.length).clone();
				this.selectRoi(roiOrgane);
				this.laVue.getRoiManager().select(this.indexRoi);
			}
		}
	}

	public void clearOverlay() {
		laVue.getOverlay().clear();
		VueScin.setOverlayDG(laVue.getOverlay(), laVue.getFen_application().getImagePlus());
	}

	public void showSlice(int nSlice) {
		System.out.println("Slice n" + this.getCurrentSlice());
		this.clearOverlay();
		laVue.getFen_application().showSlice(nSlice);
		laVue.getFen_application().updateSliceSelector();
	}
	
	public void ajouterRoiOverlay(Roi roi) {
		this.laVue.getImp().getOverlay().add(roi);
	}

	public RoiManager getRoiManager() {
		return this.laVue.getRoiManager();
	}

	public void selectRoi(Roi roi) {
		laVue.getFen_application().getImagePlus().setRoi(roi);
	}

	public Roi getShownRoi() {
		return laVue.getFen_application().getImagePlus().getRoi();
	}

	public VueScin getVue() {
		return this.laVue;
	}
	
	public int getIndexRoi() {
		return this.indexRoi;
	}
	
	public int getNbContamination() {
		return nbContamination;
	}
	
	public void setIndexRoi(int indexRoi) {
		this.indexRoi = indexRoi;
	}
	
	public String[] getOrganes() {
		return organes;
	}

	public void setOrganes(String[] organes) {
		this.organes = organes;
	}
	
	public int getCurrentSlice() {
		return this.getVue().getImp().getCurrentSlice();	
	}

}
