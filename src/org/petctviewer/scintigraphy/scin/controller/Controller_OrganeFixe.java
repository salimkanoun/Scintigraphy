package org.petctviewer.scintigraphy.scin.controller;

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

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.frame.RoiManager;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public abstract class Controller_OrganeFixe extends ControllerScin {

	// TODO: supprimer cette référence pour découpler le controleur de scintigraphy
	protected Scintigraphy scin;

	private String[] organes;
	protected int indexRoi;

	protected final HashMap<Integer, String> nomRois = new HashMap<>();
	private ImageListener ctrlImg;

	protected final Color STROKECOLOR = Color.RED;// couleur de la roi

	private final Overlay overlay;

	protected int tools = Toolbar.POLYGON;

	/**
	 * Classe abstraite permettant de controler les programmes de scintigraphie
	 * declarer le modele ainsi que la liste d'organes
	 */
	protected Controller_OrganeFixe(Scintigraphy scin, ModelScin model) {
		super(scin, scin.getFenApplication(), model);
		this.scin = scin;

		if (this.model.getImagePlus().getOverlay() == null) {
			this.model.getImagePlus().setOverlay(Library_Gui.initOverlay(this.model.getImagePlus()));
		}
		this.overlay = Library_Gui.duplicateOverlay(this.model.getImagePlus().getOverlay());

		// this.addImageListener
		this.ctrlImg = new ControllerImp(this);
		ImagePlus.addImageListener(this.ctrlImg);

		this.indexRoi = 0;

		Roi.setColor(this.STROKECOLOR);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// recuperation du bouton clique
		Button b = (Button) arg0.getSource();
		FenApplication fen = this.scin.getFenApplication();

		// on execute des action selon quel bouton a ete clique
		if (b == fen.getBtn_suivant()) {
			this.clickNext();

		} else if (b == fen.getBtn_precedent()) {
			this.clickPrevious();

		} else if (b == fen.getBtn_drawROI()) {
			Button btn = fen.getBtn_drawROI();

			// on change la couleur du bouton
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}

			// on deselectionne le bouton contraste
			fen.getBtn_contrast().setBackground(null);

			IJ.setTool(tools);

		} else if (b == fen.getBtn_contrast()) {
			// on change la couleur du bouton
			if (b.getBackground() != Color.LIGHT_GRAY) {
				b.setBackground(Color.LIGHT_GRAY);
			} else {
				b.setBackground(null);
			}

			// on deselectionne le bouton draw roi
			fen.getBtn_drawROI().setBackground(null);

			IJ.run("Window Level Tool");

		} else if (b == fen.getBtn_quitter()) {
			// this.scin.getFenApplication().getBtn_quitter()
			fen.close();
		}

	}

	/**
	 * Prepare la roi qui se situera a indexRoi
	 */
	public void preparerRoi(int lastRoi) {
		// on affiche la slice
		int indexSlice = this.getSliceNumberByRoiIndex(this.indexRoi);
		this.setSlice(indexSlice);

		// on charge la roi de l'organe identique precedent
		int nOrgane = this.indexRoi % this.getOrganes().length;
		Roi organRoi = this.getOrganRoi(lastRoi);
		if (organRoi != null) {
			this.model.getImagePlus().setRoi((Roi) organRoi.clone());
			this.model.getImagePlus().getRoi().setStrokeColor(this.STROKECOLOR);
			this.setInstructionsAdjust(nOrgane);
		} else {
			// on affiche les prochaines instructions
			this.setInstructionsDelimit(nOrgane);
		}
	}

	@Override
	public void clickPrevious() {
		// Si boutton suivant desactive car on est arrive a la fin du programme, on le
		// reactive quand on a clique sur precedent
		if (!scin.getFenApplication().getBtn_suivant().isEnabled())
			scin.getFenApplication().getBtn_suivant().setEnabled(true);

		// on decrement indexRoi
		if (this.indexRoi > 0) {
			this.indexRoi--;
			if (indexRoi == 0) {
				// si on est arrive au dernier roi, on desactive le bouton
				this.scin.getFenApplication().getBtn_precedent().setEnabled(false);
			}
		}
		// On affiche la ROI et la slice n-1
		this.preparerRoi(this.indexRoi);

		// this.saveCurrentRoi(this.getNomOrgane(this.indexRoi), this.indexRoi);
	}

	@Override
	public void clickNext() {
		// sauvegarde du ROI actuel
		try {
			this.saveRoiAtIndex(this.getNomOrgane(this.indexRoi), this.indexRoi);

			// on active le bouton precedent
			this.scin.getFenApplication().getBtn_precedent().setEnabled(true);

			// on active la fin si c'est necessaire
			if (this.isOver()) {
				this.setSlice(this.model.getImagePlus().getCurrentSlice());
				scin.getFenApplication().getBtn_suivant().setEnabled(false);
				// thread de capture, permet de laisser le temps de charger l'image plus dans le
				// thread principal
				Thread captureThread = new Thread(() -> {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					end();
				});
				captureThread.start();

			} else {
				// on prepare la roi suivante
				this.indexRoi++;
				this.preparerRoi(this.indexRoi - 1);
			}
		} catch (NoDataException e1) {
//			JOptionPane.showMessageDialog(vue, e1.getMessage(), "", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Sauvegarde la roi dans le roi manager et dans le modele
	 * 
	 * @param nomRoi : studyName de la roi a sauvegarder
	 */
	public void saveRoiAtIndex(String nomRoi, int indexRoi) throws NoDataException {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus
			// on change la couleur pour l'overlay
			this.model.getImagePlus().getRoi().setStrokeColor(Color.YELLOW);

			/*
			 * 
			 * // on enregistre la ROI dans le modele this.modele.enregistrerMesure(
			 * this.addTag(nomRoi), this.model.getImagePlus());
			 */

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.model.getRoiManager().getRoi(indexRoi) == null) {
				// Add Roi to the Roi Manager
				this.model.getRoiManager().addRoi(this.model.getImagePlus().getRoi());
			} else { // Si il existe on l'ecrase
				this.model.getRoiManager().setRoi(this.model.getImagePlus().getRoi(), indexRoi);
				// on supprime le roi nouvellement ajoute de la vue
				this.scin.getFenApplication().getImagePlus().killRoi();
			}

			// precise la postion en z
			this.model.getRoiManager().getRoi(indexRoi).setPosition(this.getSliceNumberByRoiIndex(indexRoi));

			// changement de studyName
			this.model.getRoiManager().rename(indexRoi, nomRoi);

			// on ajoute le studyName de la roi a la liste
			this.nomRois.put(indexRoi, addTag(nomRoi));
		} else {

			if (this.getOrganRoi(indexRoi) == null) {
				JOptionPane.showMessageDialog(scin.getFenApplication(), "Roi Lost", "Warning",
						JOptionPane.WARNING_MESSAGE);
			} else {
				// restore la roi organe si c'est possible
				JOptionPane.showMessageDialog(scin.getFenApplication(), "Roi Lost, previous Roi restaured", "Warning",
						JOptionPane.WARNING_MESSAGE);
				this.model.getImagePlus().setRoi(this.getOrganRoi(indexRoi));
			}
			throw new NoDataException("No ROI selected");
		}
	}

	/**
	 * Rajoute au studyName de l'organe son type de prise (A pour Ant / P pour Post) ainsi
	 * qu'un numero pour eviter les doublons
	 * 
	 * @param nomOrgane studyName de l'organe
	 * @return nouveau studyName
	 */
	public String addTag(String nomOrgane) {
		String nom = nomOrgane;

		// on ajoute au studyName P ou A pour Post ou Ant
		if (this.isPost()) {
			nom += " P";
		} else {
			nom += " A";
		}

		// on ajoute un numero pour l'identifier
		String count = this.getSameNameRoiCount(nom);
		nom += count;

		return nom;
	}

	public void removeImpListener() {
		ImagePlus.removeImageListener(this.ctrlImg);
	}

	public void addImpListener() {
		this.ctrlImg = new ControllerImp(this);
		ImagePlus.addImageListener(this.ctrlImg);
	}

	/**
	 * permet de savoir si toutes les rois necessaires ont ete enregistrees
	 * 
	 * @return true si le bon nombre de roi est enregistre
	 */
	public abstract boolean isOver();

	/**
	 * Permet de determiner si la roi indexRoi est post ou ant
	 * 
	 * @return true si la roi d'index indexRoi est post, false si elle est ant
	 */
	public abstract boolean isPost();

	/**
	 * Renvoie la roi de l'image plus
	 * 
	 * @return roi en cours d'édition de l'image
	 */
	public Roi getSelectedRoi() {
		System.out.println(this.scin.getFenApplication().getImagePlus().getRoi());
		return this.scin.getFenApplication().getImagePlus().getRoi();
	}

	public Scintigraphy getScin() {
		return this.scin;
	}

	public int getIndexRoi() {
		return this.indexRoi;
	}

	public String[] getOrganes() {
		return this.organes;
	}

	/*
	 * public ModelScin getModele() { return this.modele; }
	 */

	public String getNomOrgane(int index) {
		return this.getOrganes()[index % this.getOrganes().length];
	}

	public HashMap<Integer, String> getNomRois() {
		return this.nomRois;
	}

	public RoiManager getRoiManager() {
		return this.model.getRoiManager();
	}

	/**
	 * Renvoie le numero de slice ou doit se trouver la roi d'index roiIndex
	 * 
	 * @param roiIndex : Index de la roi dont il faut determiner le numero de slice
	 * @return le numero de slice ou se trouve la roi
	 */
	public abstract int getSliceNumberByRoiIndex(int roiIndex);

	/**
	 * Renvoie la roi qui sera utilisée dans la methode preparerRoi, appellée lors
	 * du clic sur les boutons <précédent et suivant <br>
	 * See also {@link #preparerRoi(int)}
	 * 
	 * @return la roi utilisée dans la methode preparerRoi, null si il n'y en a pas
	 * 
	 */
	public abstract Roi getOrganRoi(int lastRoi);

	/**
	 * Renvoie le nombre de roi avec le meme studyName ayant deja ete enregistrees
	 * 
	 * @param nomRoi : studyName de la roi
	 * 
	 * @return nombre de roi avec le meme studyName
	 */
	public String getSameNameRoiCount(String nomRoi) {
		int count = 0;
		for (int i = 0; i < this.nomRois.size(); i++) {
			if (this.nomRois.get(i).contains(nomRoi)) {
				count++;
			}
		}

		return String.valueOf(count);
	}

	/********** Setter **********/

	public void setScin(Scintigraphy scin) {
		this.scin = scin;
	}

	public void setIndexRoi(int indexRoi) {
		this.indexRoi = indexRoi;
	}

	public void setOrganes(String[] organes) {
		this.organes = organes;
	}

	/**
	 * Affiche les instructions de delimitation d'un organe (" ...")
	 * 
	 * @param nOrgane : numero de l'organe a delimiter
	 */
	public void setInstructionsDelimit(int nOrgane) {
		this.scin.getFenApplication().setText_instructions("Delimit the " + this.getNomOrgane(nOrgane));
		this.scin.getFenApplication().pack();
	}

	/**
	 * Affiche les instructions d'ajustement d'un organe ("Adjust the ...")
	 * 
	 * @param nOrgane : numero de l'organe a ajuster
	 */
	public void setInstructionsAdjust(int nOrgane) {
		this.scin.getFenApplication().setText_instructions("Adjust the " + this.getNomOrgane(nOrgane));
	}

	/**
	 * Affiche une slice et son Overlay, si la roi indexRoi se trouve sur cette
	 * slice, elle n'est pas affichee dans l'overlay mais chargee dans l'imp
	 * 
	 * @param indexSlice : numero de la slice a afficher
	 */
	public void setSlice(int indexSlice) {
		// ecrase l'overlay et tue la roi
		ImagePlus imp = this.model.getImagePlus();

		imp.getOverlay().clear();
		imp.setOverlay(Library_Gui.duplicateOverlay(overlay));
		imp.killRoi();

		// change la slice courante
		this.model.getImagePlus().setSlice(indexSlice);

		// ajout des roi dans l'overlay
		for (int i = 0; i < this.model.getRoiManager().getCount(); i++) {
			Roi roi = this.model.getRoiManager().getRoi(i);
			// si la roi se trouve sur la slice courante ou sur toutes les slices
			if (roi.getZPosition() == indexSlice || roi.getZPosition() == 0) {
				// si c'est la roi courante on la set dans l'imp
				if (i != this.indexRoi || this.isOver()) {
					imp.getOverlay().add(roi);
				} else {
					imp.setRoi(roi);
					imp.getRoi().setStrokeColor(this.STROKECOLOR);
				}
			}
		}
	}

	@Override
	public void close() {

		this.model.getRoiManager().close();
	}

}