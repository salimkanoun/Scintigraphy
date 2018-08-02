package org.petctviewer.scintigraphy.scin;

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
import java.util.List;

import javax.swing.JOptionPane;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.frame.RoiManager;

public abstract class ControleurScin implements ActionListener {

	private Scintigraphy scin;
	private ModeleScin modele;
	protected RoiManager roiManager;


	private String[] organes;
	protected int indexRoi;

	private List<String> nomRois = new ArrayList<>();
	private ImageListener ctrlImg;
	
	protected Color STROKECOLOR = Color.RED;//couleur de la roi

	private Overlay overlay;
	
	protected int tools = Toolbar.POLYGON;

	/**
	 * Classe abstraite permettant de controler les programmes de scintigraphie
	 * declarer le modele ainsi que la liste d'organes
	 */
	protected ControleurScin(Scintigraphy scin) {
		this.scin = scin;

		if (scin.getImp().getOverlay() == null) {
			scin.getImp().setOverlay(Scintigraphy.initOverlay(scin.getImp()));
		}
		this.overlay = Scintigraphy.duplicateOverlay(scin.getImp().getOverlay());

		//SK BOOLEAN FALSE POUR MASQUER, A METTRE TRUE POUR DEVELOPPEMENT
		this.roiManager = new RoiManager(false);

		//this.addImageListener
		this.ctrlImg = new ControleurImp(this);
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
			this.clicSuivant();
			
		}else if (b == fen.getBtn_precedent()) {
			this.clicPrecedent();
			
		}else if (b == fen.getBtn_drawROI()){
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
			
		}else if (b == fen.getBtn_contrast()) {
			// on change la couleur du bouton
			if (b.getBackground() != Color.LIGHT_GRAY) {
				b.setBackground(Color.LIGHT_GRAY);
			} else {
				b.setBackground(null);
			}

			// on deselectionne le bouton draw roi
			fen.getBtn_drawROI().setBackground(null);

			IJ.run("Window Level Tool");
			
		}else if (b == fen.getBtn_quitter()) {
			//this.scin.getFenApplication().getBtn_quitter()
			fen.close();
			return;
		}

		// on apelle la methode notify clic pour recuperer le clic dans les classes
		// heritees
		this.notifyClic(arg0);
	}

	/**
	 * Est appelee a la fin de action performed, son corps est vide </br>
	 * <b> Cette methode existe uniquement pour etre override </b>
	 * 
	 * @param arg0
	 *            ActionEvent
	 */
	public void notifyClic(ActionEvent arg0) {
		// A overrider si besoin
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
			this.scin.getImp().setRoi((Roi) organRoi.clone());
			this.scin.getImp().getRoi().setStrokeColor(this.STROKECOLOR);
			this.setInstructionsAdjust(nOrgane);
		} else {
			// on affiche les prochaines instructions
			this.setInstructionsDelimit(nOrgane);
		}
	}

	/**
	 * est appelle lors du clic sur le bouton "Previous"
	 */
	public void clicPrecedent() {
		// sauvegarde du ROI courant
		this.saveCurrentRoi(this.getNomOrgane(this.indexRoi), this.indexRoi);

		// on decrement indexRoi
		if (this.indexRoi > 0) {
			this.indexRoi--;
		} else {
			// si c'est le dernier roi, on desactive le bouton
			this.scin.getFenApplication().getBtn_precedent().setEnabled(false);
		}

		this.preparerRoi(this.indexRoi + 1);
	}

	/**
	 * est appelle lors du clic sur le bouton "Next"
	 */
	public void clicSuivant() {
		// sauvegarde du ROI actuel
		boolean saved = this.saveCurrentRoi(this.getNomOrgane(this.indexRoi), this.indexRoi);

		// si la sauvegarde est reussie
		if (saved) {
			// on active le bouton precedent
			this.scin.getFenApplication().getBtn_precedent().setEnabled(true);

			// on active la fin si c'est necessaire
			if (this.isOver()) {
				this.setSlice(this.scin.getImp().getCurrentSlice());

				// thread de capture, permet de laisser le temps de charger l'image plus dans le
				// thread principal
				Thread captureThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						fin();
					}
				});
				captureThread.start();

			} else {
				// on prepare la roi suivante
				this.indexRoi++;
				this.preparerRoi(this.indexRoi - 1);
			}
		}
	}
	
	/**
	 * Sauvegarde la roi dans le roi manager et dans le modele
	 * 
	 * @param nomRoi
	 *            : nom de la roi a sauvegarder
	 * @return true si la sauvegarde est reussie, false si elle ne l'est pas
	 */
	public boolean saveCurrentRoi(String nomRoi, int indexRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus
 
			// on change la couleur pour l'overlay
			this.scin.getImp().getRoi().setStrokeColor(Color.YELLOW);

			// on enregistre la ROI dans le modele
			this.modele.enregistrerMesure(
					this.addTag(nomRoi), 
					this.scin.getImp());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(indexRoi) == null) {
				this.roiManager.addRoi(this.scin.getImp().getRoi());
			} else { // Si il existe on l'ecrase
				this.roiManager.setRoi(this.scin.getImp().getRoi(), indexRoi);
				// on supprime le roi nouvellement ajoute de la vue
				this.scin.getFenApplication().getImagePlus().killRoi();
			}

			// precise la postion en z
			this.roiManager.getRoi(indexRoi).setPosition(this.getSliceNumberByRoiIndex(indexRoi));

			// changement de nom
			this.roiManager.rename(indexRoi, nomRoi);

			return true;
		}

		if (this.getOrganRoi(indexRoi) == null) {
			JOptionPane.showMessageDialog(scin.getFenApplication(), "Roi Lost", "Warning",
				        JOptionPane.WARNING_MESSAGE);
		} else {
			// restore la roi organe si c'est possible
			JOptionPane.showMessageDialog(scin.getFenApplication(), "Roi Lost, previous Roi restaured", "Warning",
			        JOptionPane.WARNING_MESSAGE);
			this.scin.getImp().setRoi(this.getOrganRoi(indexRoi));
		}

		return false;

	}

	/**
	 * Rajoute au nom de l'organe son type de prise (A pour Ant / P pour Post) ainsi
	 * qu'un numero pour eviter les doublons
	 * 
	 * @param nomOrgane
	 *            nom de l'organe
	 * @return nouveau nom
	 */
	public String addTag(String nomOrgane) {
		String nom = nomOrgane;

		// on ajoute au nom P ou A pour Post ou Ant
		if (this.isPost()) {
			nom += " P";
		} else {
			nom += " A";
		}

		// on ajoute un numero pour l'identifier
		String count = this.getSameNameRoiCount(nom);
		nom += count;

		// on ajoute le nom de la roi a la liste
		this.nomRois.add(nom);

		return nom;
	}

	public void removeImpListener() {
		ImagePlus.removeImageListener(this.ctrlImg);
	}

	public void addImpListener() {
		this.ctrlImg = new ControleurImp(this);
		ImagePlus.addImageListener(this.ctrlImg);
	}
	
	/********Abstract*****/
	
	/**
	 * permet de savoir si toutes les rois necessaires ont ete enregistrees
	 * 
	 * @return true si le bon nombre de roi est enregistre
	 */
	public abstract boolean isOver();

	/**
	 * est execute quand la prise est finie, doit ouvrir la fenetre de resultat <br>
	 * See also {@link #isOver()}
	 */
	public abstract void fin();

	/**
	 * Permet de determiner si la roi indexRoi est post ou ant
	 * 
	 * @return true si la roi d'index indexRoi est post, false si elle est ant
	 */
	public abstract boolean isPost();

	
	/*********** Getter *******/
	
	/**
	 * Renvoie la roi de l'image plus
	 * 
	 * @return roi en cours d'édition de l'image
	 */
	public Roi getSelectedRoi() {
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

	public ModeleScin getModele() {
		return this.modele;
	}
	
	public String getNomOrgane(int index) {
		return this.getOrganes()[index % this.getOrganes().length];
	}

	public List<String> getNomRois() {
		return this.nomRois;
	}

	public RoiManager getRoiManager() {
		return this.roiManager;
	}

	/**
	 * Renvoie le numero de slice ou doit se trouver la roi d'index roiIndex
	 * 
	 * @param roiIndex
	 *            : Index de la roi dont il faut determiner le numero de slice
	 * @return le numero de slice ou se trouve la roi
	 */
	public abstract int getSliceNumberByRoiIndex(int roiIndex);

	/**
	 * Renvoie la roi qui sera utilisée dans la methode preparerRoi, appellée lors
	 * du clic sur les boutons <précédent et suivant <br>
	 * See also {@link #preparerRoi()}
	 * 
	 * @param lastRoi
	 * 
	 * @return la roi utilisée dans la methode preparerRoi, null si il n'y en a pas
	 * 
	 */
	public abstract Roi getOrganRoi(int lastRoi);

	/**
	 * Renvoie le nombre de roi avec le meme nom ayant deja ete enregistrees
	 * 
	 * @param nomRoi
	 *            : nom de la roi
	 * 
	 * @return nombre de roi avec le meme nom
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


	public void setModele(ModeleScin modele) {
		this.modele = modele;
	}

	public void setRoiManager(RoiManager rm) {
		this.roiManager = rm;
	}

	/**
	 * Affiche les instructions de delimitation d'un organe (" ...")
	 * 
	 * @param nOrgane
	 *            : numero de l'organe a delimiter
	 */
	public void setInstructionsDelimit(int nOrgane) {
		this.scin.getFenApplication().getTextfield_instructions().setText("Delimit the " + this.getNomOrgane(nOrgane));
	}

	/**
	 * Affiche les instructions d'ajustement d'un organe ("Adjust the ...")
	 * 
	 * @param nOrgane
	 *            : numero de l'organe a ajuster
	 */
	public void setInstructionsAdjust(int nOrgane) {
		this.scin.getFenApplication().getTextfield_instructions().setText("Adjust the " + this.getNomOrgane(nOrgane));
	}

	/**
	 * Affiche une slice et son Overlay, si la roi indexRoi se trouve sur cette
	 * slice, elle n'est pas affichee dans l'overlay mais chargee dans l'imp
	 * 
	 * @param indexSlice
	 *            : numero de la slice a afficher
	 */
	public void setSlice(int indexSlice) {
		// ecrase l'overlay et tue la roi
		ImagePlus imp = this.scin.getImp();

		imp.getOverlay().clear();
		imp.setOverlay(Scintigraphy.duplicateOverlay(overlay));
		imp.killRoi();

		// change la slice courante
		this.scin.getImp().setSlice(indexSlice);

		// ajout des roi dans l'overlay
		for (int i = 0; i < this.roiManager.getCount(); i++) {
			Roi roi = this.roiManager.getRoi(i);
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

}