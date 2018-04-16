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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	protected int indexRoi;

	// Sert au restart
	protected ControleurScin(VueScin vue, ModeleScin leModele) {
		this.laVue = vue;
		this.leModele = leModele;

		this.indexRoi = 0;

		this.attachListener();
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
			Button btn = laVue.getFen_application().getBtn_drawROI();
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}
			laVue.getFen_application().getBtn_contrast().setBackground(null);
			IJ.setTool(Toolbar.POLYGON);
		}

		else if (b == laVue.getFen_application().getBtn_contrast()) {
			Button btn = laVue.getFen_application().getBtn_contrast();
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}
			IJ.run("Window Level Tool");
		}

		else if (b == laVue.getFen_application().getBtn_quitter()) {
			laVue.end();
			return;
		}

		else if (b == laVue.getFen_application().getBtn_showlog()) {

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
		} else {
			this.traitementBouton(b);
		}
		this.notifyClick();
	}

	public void notifyClick() {		
	}

	public void preparerRoi() {
		// on affiche la slice
		this.showSliceWithOverlay(this.getSliceNumberByRoiIndex(this.getIndexRoi()));

		// on charge la roi de l'organe identique precedent
		if (this.getOrganRoi() != null) {
			this.setRoi(this.getOrganRoi());
		}

		// on affiche les prochaines instructions
		this.laVue.getFen_application().setInstructions(this.indexRoi % this.organes.length);
	}

	private void clicPrecedent() {
		// sauvegarde du ROI courant
		this.saveCurrentRoi(this.createNomRoi());

		if (this.indexRoi > 0) {
			indexRoi--;
		} else {
			// si c'est le dernier roi, on desactive le bouton
			this.getVue().getFen_application().getBtn_precedent().setEnabled(false);
		}

		this.preparerRoi();
	}

	private void clicSuivant() {
		// ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = Modele_Shunpo.genererDicomTagsPartie1(laVue.getFen_application().getImagePlus(),
					laVue.getExamType());
		}

		// sauvegarde du ROI actuel
		boolean saved = this.saveCurrentRoi(this.createNomRoi());
		// si la sauvegarde est reussie
		if (saved) {
			// on active le bouton precedent
			this.getVue().getFen_application().getBtn_precedent().setEnabled(true);

			// on avtive la fin si c'est necessaire
			if (this.isOver()) {
				fin();
			}

			// on prepare la roi suivante
			indexRoi++;
			this.preparerRoi();
		}
	}

	/**
	 * Renvoie le nombre de roi avec le meme nom dans le Roi Manager
	 * @param nomRoi 
	 * 
	 * @return nombre de roi avec le meme nom
	 */
	public int getSameNameRoiCount(String nomRoi) {
		String[] roiNames = new String[this.getRoiManager().getCount()];
		for (int i = 0; i < roiNames.length; i++) {
			roiNames[i] = this.getRoiManager().getRoisAsArray()[i].getName();
		}

		int count = 0;
		for (int i = 0; i < roiNames.length; i++) {
			if (roiNames[i].contains(nomRoi)) {
				count++;
			}
		}

		return count;
	}

	/**
	 * permet de savoir si toutes les rois necessaires ont ete enregistrees
	 * 
	 * @return true si le bon nombre de roi est enregistre
	 */
	public abstract boolean isOver();

	/**
	 * est execute quand la prise est finie <br>
	 * See also {@link #isOver()}
	 */
	public abstract void fin();

	public abstract int getSliceNumberByRoiIndex(int roiIndex);

	// renvoie true si la prise est post, false si elle est ant
	public abstract boolean isPost();

	public abstract Roi getOrganRoi();

	public abstract void traitementBouton(Button b);

	/**
	 * Sauvegarde la roi dans le roi manager
	 * 
	 * @param nomRoi
	 *            : nom de la rooi a sauvegarder
	 * @return true si la sauvegarde est reussie, false si elle ne l'est pas
	 */
	public boolean saveCurrentRoi(String nomRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus

			// TODO on enregistre la ROI dans le modele
			// leModele.enregisterMesure(nomRoi, laVue.getFen_application().getImagePlus());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (laVue.getRoiManager().getRoi(this.getIndexRoi()) == null) {

				nomRoi += this.getSameNameRoiCount(nomRoi);

				laVue.getRoiManager().addRoi(laVue.getFen_application().getImagePlus().getRoi());
				laVue.getRoiManager().rename(this.getIndexRoi(), nomRoi);

			} else { // Si il existe on fait un update
				this.laVue.getRoiManager().select(this.getIndexRoi());
				this.laVue.getRoiManager().runCommand("Update");

				// on supprime le roi nouvellement ajoute de la vue
				laVue.getFen_application().getImagePlus().killRoi();
			}
			return true;
		} else {
			System.out.println("Roi perdue");
			return false;
		}

	}	

	public void clearOverlay() {
		laVue.getOverlay().clear();
		VueScin.setOverlayDG(laVue.getOverlay(), laVue.getFen_application().getImagePlus());
	}

	public void showSliceWithOverlay(int nSlice) {
		this.clearOverlay();
		this.getVue().getFen_application().getImagePlus().killRoi();

		laVue.getFen_application().showSlice(nSlice);

		// on affiche les roi pour cette slide
		for (Roi roi : this.getRoisSlice(this.getCurrentSlice())) {
			// on ajoute les roi dans l'overlay si ce n'est pas la roi courante
			if (roi != this.getRoiManager().getRoi(getIndexRoi())) {
				this.ajouterRoiOverlay(roi);
			} else { // sinon on la selectionne
				this.setRoi(roi);
			}
		}

		laVue.getFen_application().updateSliceSelector();
	}

	public Roi[] getRoisSlice(int nSlice) {

		List<Roi> rois = new ArrayList<Roi>();

		for (int i = 0; i < this.getRoiManager().getCount(); i++) {
			if (this.getSliceNumberByRoiIndex(i) == nSlice) {
				Roi roiIt = (Roi) this.getRoiManager().getRoi(i);
				if (roiIt != null) {
					rois.add(roiIt);
				}
			}

		}

		return rois.toArray(new Roi[0]);
	}
	
	public String createNomRoi() {
		// création du nom du ROI selon la prise post ou ant
		String nomRoi = this.getOrganes()[this.indexRoi];
		if (this.isPost()) {
			nomRoi += " Post";
		} else {
			nomRoi += " Ant";
		}
		return nomRoi;
	}

	private void attachListener() {
		this.laVue.getFen_application().getImagePlus();
		ImagePlus.addImageListener(new ControleurImp(this));
	}

	public void ajouterRoiOverlay(Roi roi) {
		this.laVue.getImp().getOverlay().add(roi);
	}

	public RoiManager getRoiManager() {
		return this.laVue.getRoiManager();
	}

	public void setRoi(Roi roi) {
		laVue.getFen_application().getImagePlus().setRoi(roi);
	}

	public Roi getSelectedRoi() {
		Roi roi = laVue.getFen_application().getImagePlus().getRoi();
		return roi;
	}

	public VueScin getVue() {
		return this.laVue;
	}

	public int getIndexRoi() {
		return this.indexRoi;
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
		return this.laVue.getImp().getCurrentSlice();
	}

}
