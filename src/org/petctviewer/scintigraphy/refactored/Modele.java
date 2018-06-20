package org.petctviewer.scintigraphy.refactored;

import java.awt.Color;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.ImageListener;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import ij.gui.Overlay;
import ij.gui.Roi;

public class Modele {

	private Color STROKECOLOR = Color.RED, OVERLAYCOLOR = Color.YELLOW;

	private RoiManager roiManager;
	private String[] organes;
	private int indexRoi;
	private ImageListener imageListener;
	private Overlay overlay;
	private Calculateur calculateur;
	private ImagePlus imp;
	private String instructions;
	private boolean over;
	private int currentRoiSlice;
	private boolean post;
	private HashMap<String, Integer> nbRois;

	public Modele(ImagePlus imp, Calculateur calculateur) {
		this.calculateur = calculateur;
		this.imp = imp;
		this.currentRoiSlice = 0;

		// si l'overlay n'est pas nul, on le sauvegarde
		if (imp.getOverlay() != null) {
			this.overlay = Scintigraphy.duplicateOverlay(imp.getOverlay());
		}

		// lance le RoiManager en instance seule (pas en singleton)
		this.roiManager = new RoiManager(false);

		this.indexRoi = 0;
		Roi.setColor(STROKECOLOR);
	}
	
	public void setOrganes(String[] organes) {
		this.organes = organes;
		this.instructions = "Delimit the " + organes[0];
	}

	/**
	 * prepare la prochaine roi, si la roi par defaut est null elle ne sera pas
	 * preaffiche
	 * 
	 * @param indexSlice
	 *            numero de la slice
	 * @param organRoi
	 *            roi de par defaut
	 */
	public void preparerRoi(int indexSlice, Roi organRoi) {
		if (indexSlice != 0) {
			// rafraichit l'image
			this.setSlice(indexSlice);
		} else {
			this.currentRoiSlice = indexSlice;
		}

		// on charge la roi de l'organe identique precedent
		String nomOrgane = this.organes[this.indexRoi % this.organes.length];

		if (organRoi != null) {
			this.imp.setRoi((Roi) organRoi.clone());
			this.imp.getRoi().setStrokeColor(this.STROKECOLOR);
			this.instructions = "Delimit the " + nomOrgane;
		} else {
			// on affiche les prochaines instructions
			this.instructions = "Adjust the " + nomOrgane;
		}
	}

	public void setSlice(int indexSlice) {
		imp.getOverlay().clear();
		imp.setOverlay(Scintigraphy.duplicateOverlay(this.overlay));
		imp.killRoi();

		// change la slice courante
		this.imp.setSlice(indexSlice);

		// ajout des roi dans l'overlay
		for (int i = 0; i < this.roiManager.getCount(); i++) {
			Roi roi = this.roiManager.getRoi(i);
			// si la roi se trouve sur la slice courante ou sur toutes les slices
			if (roi.getZPosition() == indexSlice || roi.getZPosition() == 0) {
				// si c'est la roi courante on la set dans l'imp
				if (i != this.indexRoi || over) {
					imp.getOverlay().add(roi);
				} else {
					imp.setRoi(roi);
					imp.getRoi().setStrokeColor(this.STROKECOLOR);
				}
			}
		}
	}

	/**
	 * sauvegarde la roi dans le roiManager et enregistre la mesure (avec le tag) dans le modele
	 * @return
	 */
	public boolean saveCurrentRoi() {
		if (this.imp.getRoi() != null) { // si il y a une roi sur l'image plus

			// on change la couleur pour l'overlay
			this.imp.getRoi().setStrokeColor(this.OVERLAYCOLOR);

			String nomRoi = this.organes[this.indexRoi % this.organes.length];
			
			// on enregistre la ROI dans le modele
			this.calculateur.enregistrerMesure(this.addTag(nomRoi), this.imp);

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(indexRoi) == null) {
				this.roiManager.addRoi(this.imp.getRoi());
			} else { // Si il existe on l'ecrase
				this.roiManager.setRoi(this.imp.getRoi(), indexRoi);
			}

			//on supprime la roi nouvellement ajoutee
			this.imp.killRoi();
			
			// precise la postion en z
			this.roiManager.getRoi(indexRoi).setPosition(this.currentRoiSlice);

			// changement de nom
			this.roiManager.rename(indexRoi, nomRoi);
			
			//on ajoute 1 au nombre d'organes du meme nom
			String nomOrgane = this.organes[indexRoi % this.organes.length];
			int nb = this.nbRois.get(nomOrgane);
			nb ++;
			this.nbRois.put(nomOrgane, nb);
			
			return true;
		}
		
		return false;
	}
	
	public void setPost(boolean post) {
		this.post = post;
	}

	/**
	 * Rajoute au nom de l'organe son type de prise (A pour Ant / P pour Post) ainsi
	 * qu'un numero pour eviter les doublons.
	 * 
	 * ex: heart A0, brain P4
	 * 
	 * @param nomOrgane
	 *            nom de l'organe
	 * @return nouveau nom
	 */
	private String addTag(String nomRoi) {
		String nom = nomRoi;

		// on ajoute au nom P ou A pour Post ou Ant
		if (this.post) {
			nom += " P";
		} else {
			nom += " A";
		}

		// on ajoute un numero pour l'identifier
		nom += this.nbRois.get(nomRoi);;

		return nom;
	}

	public String[] getOrganes() {
		return this.organes;
	}

	public String getInstructions() {
		return instructions;
	}

	public void end() {
		this.over = true;
		this.calculateur.calculerResultats();
		this.calculateur.afficherResultats();
	}
	
	public ImagePlus getImp() {
		return this.imp;
	}
	
	public void removeImpListener() {
		ImagePlus.removeImageListener(this.imageListener);
	}

	public void addImpListener() {
		this.imageListener = new ImageUpdater(this);
		ImagePlus.addImageListener(this.imageListener);
	}
}