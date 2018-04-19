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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;

public abstract class ControleurScin implements ActionListener {

	private VueScin laVue;
	private ModeleScin leModele;
	protected RoiManager roiManager;

	protected static boolean showLog;
	private String tagCapture;

	private String[] organes;
	protected int indexRoi;

	protected ControleurScin(VueScin vue) {
		this.laVue = vue;
		this.roiManager = new RoiManager();

		this.indexRoi = 0;

		this.attachListener();
	}

	public void setModele(ModeleScin modele) {
		this.leModele = modele;
	}

	/**
	 * keys : id nom date
	 * 
	 * @param imp
	 * @return
	 */
	public HashMap<String, String> getDicomInfo(ImagePlus imp) {
		HashMap<String, String> hm = new HashMap<String, String>();
		String nom = DicomTools.getTag(imp, "0010,0010").trim();
		hm.put("nom", nom.replace("^", " "));

		hm.put("id", DicomTools.getTag(imp, "0010,0020").trim());

		String dateStr = DicomTools.getTag(imp, "0008,0022").trim();
		Date result = null;
		try {
			result = new SimpleDateFormat("yyyymmdd").parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String r = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH).format(result);

		hm.put("date", r);
		return hm;
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

		/**
		 * else if (b == laVue.getFen_application().getBtn_capture()) {
		 * laVue.getFen_application().getBtn_capture().setVisible(false); //TODO
		 * laVue.csv.setText("Provided By Petctviewer.org"); ImagePlus captureFinale =
		 * ModeleScin.captureFenetre(WindowManager.getCurrentImage(), 0, 0);
		 * WindowManager.getCurrentWindow().getImagePlus().changes = false;
		 * WindowManager.getCurrentWindow().close(); // On genere la 2eme partie des tag
		 * dicom et on l'ajoute a la 1ere partie dans // le property de l'image finale
		 * captureFinale.setProperty("Info", tagCapture +=
		 * (ModeleScin.genererDicomTagsPartie2(captureFinale))); // On affiche et on
		 * agrandie la fenetre de la capture finale captureFinale.show(); // On met un
		 * zoom a 80% captureFinale.getCanvas().setMagnification(0.8); // generation du
		 * c
		 * 
		 * // On fait la capture finale captureFinale.getWindow().toFront(); // On
		 * propose de sauver la capture en DICOM IJ.run("myDicom..."); // fin du
		 * programme ici
		 *
		 * }
		 */

		else if (b == laVue.getFen_application().getBtn_drawROI())

		{
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
			laVue.fen_application.close();
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
		}

		this.notifyClick(arg0);
	}

	/**
	 * Est appelée a la fin de action performed, son corps est vide <b> Cette
	 * methode existe uniquement pour etre override </b>
	 */
	public void notifyClick(ActionEvent arg0) {
	}

	/**
	 * Prepare la roi qui se situera a indexRoi
	 */
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
		boolean saved = this.saveCurrentRoi(this.createNomRoi(this.getOrganes()[this.indexRoi]));
		// si la sauvegarde est reussie
		if(saved) {
			if (this.indexRoi > 0) {
				indexRoi--;
			} else {
				// si c'est le dernier roi, on desactive le bouton
				this.getVue().getFen_application().getBtn_precedent().setEnabled(false);
			}
			
			this.preparerRoi();
		}		
	}

	private void clicSuivant() {
		// ajout du tag si il n'est pas encore présent
		if (tagCapture == null) {
			tagCapture = ModeleScin.genererDicomTagsPartie1(laVue.getFen_application().getImagePlus(),
					laVue.getExamType());
		}

		// sauvegarde du ROI actuel
		boolean saved = this.saveCurrentRoi(this.createNomRoi(this.getOrganes()[this.indexRoi]));
		// si la sauvegarde est reussie
		if (saved) {
			// on active le bouton precedent
			this.getVue().getFen_application().getBtn_precedent().setEnabled(true);

			// on avtive la fin si c'est necessaire
			if (this.isOver()) {
				fin();
			}else {
				// on prepare la roi suivante
				indexRoi++;
				this.preparerRoi();
			}
		}
	}

	/**
	 * Renvoie le nombre de roi avec le meme nom dans le Roi Manager
	 * 
	 * @param nomRoi
	 * 
	 * @return nombre de roi avec le meme nom
	 */
	public String getSameNameRoiCount(String nomRoi) {
		String[] roiNames = new String[this.roiManager.getCount()];
		for (int i = 0; i < roiNames.length; i++) {
			roiNames[i] = this.roiManager.getRoisAsArray()[i].getName();
		}

		int count = 0;
		for (int i = 0; i < roiNames.length; i++) {
			if (roiNames[i].contains(nomRoi)) {
				count++;
			}
		}

		return String.valueOf(count);
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

	/**
	 * Renvoie le numero de slice ou doit se trouver la roi d'index roiIndex
	 * 
	 * @param roiIndex
	 *            Index de la roi dont il faut determiner le numero de slice
	 * @return le numero de slice ou se trouve la roi
	 */
	public abstract int getSliceNumberByRoiIndex(int roiIndex);

	/**
	 * Permet de determiner si la roi indexRoi est ant
	 * 
	 * @return true si la roi d'index indexRoi est post, false si elle est ant
	 */
	public abstract boolean isPost();

	/**
	 * Renvoie la roi qui sera utilisée dans la methode preparerRoi, appellée lors
	 * du clic sur les boutons précédent et suivant <br>
	 * See also {@link #preparerRoi()}
	 * 
	 * @return la roi utilisée dans la methode preparerRoi
	 */
	public abstract Roi getOrganRoi();

	/**
	 * Sauvegarde la roi dans le roi manager
	 * 
	 * @param nomRoi
	 *            nom de la roi a sauvegarder
	 * @return true si la sauvegarde est reussie, false si elle ne l'est pas
	 */
	public boolean saveCurrentRoi(String nomRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus

			// on enregistre la ROI dans le modele
			leModele.enregisterMesure(nomRoi, laVue.getFen_application().getImagePlus());

			String nom2 = nomRoi.substring(0, nomRoi.lastIndexOf(" "));
			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(this.getIndexRoi()) == null) {
				this.roiManager.addRoi(laVue.getFen_application().getImagePlus().getRoi());
				this.roiManager.rename(this.getIndexRoi(), nom2);

			} else { // Si il existe on fait un update
				this.roiManager.select(this.getIndexRoi());
				this.roiManager.runCommand("Update");

				// on supprime le roi nouvellement ajoute de la vue
				laVue.getFen_application().getImagePlus().killRoi();
			}
			return true;
		} else {
			System.out.println("Roi perdue");
			return false;
		}

	}

	/**
	 * Vide l'overlay et ajoute le lettres G et D
	 */
	public void clearOverlay() {
		laVue.getOverlay().clear();
		VueScin.setOverlayDG(laVue.getOverlay(), laVue.getFen_application().getImagePlus());
	}

	/**
	 * Affiche la slice nSlice et l'overlay correspondant selon la methode
	 * {@link #getSliceNumberByRoiIndex(int)}
	 * 
	 * @param nSlice
	 *            numero de la slice a afficher
	 */
	public void showSliceWithOverlay(int nSlice) {
		this.clearOverlay();
		this.getVue().getFen_application().getImagePlus().killRoi();

		laVue.getFen_application().showSlice(nSlice);

		// on affiche les roi pour cette slide
		for (Roi roi : this.getRoisSlice(this.getImp().getCurrentSlice())) {
			// on ajoute les roi dans l'overlay si ce n'est pas la roi courante
			if (roi != this.roiManager.getRoi(getIndexRoi())) {
				this.ajouterRoiOverlay(roi);
			} else { // sinon on la selectionne
				this.setRoi(roi);
			}
		}

		laVue.getFen_application().updateSliceSelector();
	}

	/**
	 * Renvoie toutes les rois se trouvant sur une slice selon la methode
	 * {@link #getSliceNumberByRoiIndex(int)}
	 * 
	 * @param nSlice
	 *            numero de la slice
	 * @return Tableau de roi se trouvant sur la slice nSlice
	 */
	public Roi[] getRoisSlice(int nSlice) {

		List<Roi> rois = new ArrayList<Roi>();

		for (int i = 0; i < this.roiManager.getCount(); i++) {
			if (this.getSliceNumberByRoiIndex(i) == nSlice) {
				Roi roiIt = (Roi) this.roiManager.getRoi(i);
				if (roiIt != null) {
					rois.add(roiIt);
				}
			}

		}

		return rois.toArray(new Roi[0]);
	}

	/**
	 * Rajoute au nom de l'organe son type de prise (A pour Ant / P pour Post) ainsi
	 * qu'un numero pour eviter les doublons
	 * 
	 * @param nomOrgane
	 *            nom de l'organe
	 * @return nouveau nom
	 */
	public String createNomRoi(String nomOrgane) {
		if (this.isPost()) {
			nomOrgane += " P";
		} else {
			nomOrgane += " A";
		}

		if (this.roiManager.getRoi(this.getIndexRoi()) == null) {
			nomOrgane += this.getSameNameRoiCount(nomOrgane);
		} else {
			nomOrgane = this.roiManager.getRoi(this.getIndexRoi()).getName();
		}

		return nomOrgane;
	}

	private void attachListener() {
		this.laVue.getImagePlus();
		ImagePlus.addImageListener(new ControleurImp(this));
	}

	/**
	 * ajoute une roi sur l'overlay
	 * 
	 * @param roi
	 *            Roi a ajouter sur l'overlay
	 */
	public void ajouterRoiOverlay(Roi roi) {
		this.laVue.getImp().getOverlay().add(roi);
	}

	public void setRoi(Roi roi) {
		laVue.getImagePlus().setRoi(roi);
	}

	/**
	 * Renvoie la roi de l'image plus
	 * 
	 * @return roi en cours d'édition de l'image
	 */
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

	public ImagePlus getImp() {
		return this.laVue.getImp();
	}

	public ModeleScin getModele() {
		return this.leModele;
	}

	public RoiManager getRoiManager() {
		return roiManager;
	}

}
