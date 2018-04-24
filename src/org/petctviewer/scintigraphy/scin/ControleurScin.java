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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

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

		this.notifyClic(arg0);
	}

	/**
	 * Est appelée a la fin de action performed, son corps est vide <b> Cette
	 * methode existe uniquement pour etre override </b>
	 */
	public void notifyClic(ActionEvent arg0) {
	}

	/**
	 * Prepare la roi qui se situera a indexRoi
	 */
	public void preparerRoi() {
		// on affiche la slice
		int indexSlice = this.getSliceNumberByRoiIndex(this.indexRoi);
		this.setSlice(indexSlice);

		// on charge la roi de l'organe identique precedent

		if (this.getOrganRoi() != null) {
			this.laVue.getImp().setRoi((Roi) this.getOrganRoi().clone());
		}

		// on affiche les prochaines instructions
		this.setInstructions(this.indexRoi % this.getOrganes().length);
		
	}
	
	public void setInstructions(int nOrgane) {
		this.laVue.getFen_application().setInstructions(nOrgane);
	}

	public void setSlice(int indexSlice) {
		this.clearOverlay();
		this.getVue().getFen_application().getImagePlus().killRoi();
		
		this.laVue.getImp().setSlice(indexSlice);
		
		for (int i = 0; i < this.roiManager.getCount(); i++) {
			Roi roi = this.roiManager.getRoi(i);
			if (roi.getZPosition() == indexSlice) {
				if(i != indexRoi) {
					this.laVue.getImp().getOverlay().add(roi);
				} else {
					this.laVue.getImp().setRoi(roi);
				}
			}
		}
	}

	private void clicPrecedent() {
		// sauvegarde du ROI courant
		this.saveCurrentRoi(this.getNomOrgane(indexRoi), indexRoi);

		if (this.indexRoi > 0) {
			indexRoi--;
		} else {
			// si c'est le dernier roi, on desactive le bouton
			this.getVue().getFen_application().getBtn_precedent().setEnabled(false);
		}

		this.preparerRoi();
	}

	private void clicSuivant() {
		// sauvegarde du ROI actuel
		boolean saved = this.saveCurrentRoi(this.getNomOrgane(indexRoi), indexRoi);
		// si la sauvegarde est reussie
		if (saved) {
			// on active le bouton precedent
			this.getVue().getFen_application().getBtn_precedent().setEnabled(true);

			// on avtive la fin si c'est necessaire
			if (this.isOver()) {
				fin();
			} else {
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
		//on construit le tableau de roi
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
	 * Renvoie la roi qui sera utilisée dans la methode preparerRoi, appellée lors
	 * du clic sur les boutons précédent et suivant <br>
	 * See also {@link #preparerRoi()}
	 * 
	 * @return la roi utilisée dans la methode preparerRoi, null si il n'y en a pas
	 * 
	 */
	public abstract Roi getOrganRoi();

	/**
	 * Sauvegarde la roi dans le roi manager
	 * 
	 * @param nomRoi
	 *            nom de la roi a sauvegarder
	 * @return true si la sauvegarde est reussie, false si elle ne l'est pas
	 */
	public boolean saveCurrentRoi(String nomRoi, int indexRoi) {
		if (this.getSelectedRoi() != null) { // si il y a une roi sur l'image plus

			// on enregistre la ROI dans le modele
			leModele.enregisterMesure(this.addTag(nomRoi), laVue.getImp());

			// On verifie que la ROI n'existe pas dans le ROI manager avant de l'ajouter
			// pour eviter les doublons
			if (this.roiManager.getRoi(indexRoi) == null) {
				this.roiManager.addRoi(laVue.getImp().getRoi());
				this.roiManager.getRoi(indexRoi).setPosition(this.getSliceNumberByRoiIndex(indexRoi));
				this.roiManager.rename(indexRoi, nomRoi);

			} else { // Si il existe on fait un update
				this.roiManager.select(indexRoi);
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
	 * Permet de determiner si la roi indexRoi est post ou ant
	 * 
	 * @return true si la roi d'index indexRoi est post, false si elle est ant
	 */
	public abstract boolean isPost();

	/**
	 * Rajoute au nom de l'organe son type de prise (A pour Ant / P pour Post) ainsi
	 * qu'un numero pour eviter les doublons
	 * 
	 * @param nomOrgane
	 *            nom de l'organe
	 * @return nouveau nom
	 */
	public String addTag(String nomOrgane) {
		String count = this.getSameNameRoiCount(nomOrgane);
		
		if (this.isPost()) {
			nomOrgane += " P";
		} else {
			nomOrgane += " A";
		}
		
		nomOrgane += count;

		return nomOrgane;
	}
	
	public static void setCaptureButton(JButton btn_capture, JLabel lbl_credits, VueScin vue, JFrame jf) {

		btn_capture.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JButton b = (JButton) (e.getSource());
				b.setVisible(false);
				lbl_credits.setVisible(true);
				
				jf.pack();
				Container c = jf.getContentPane();

				// Capture, nouvelle methode a utiliser sur le reste des programmes
				BufferedImage capture = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
				c.paint(capture.getGraphics());
				ImagePlus imp = new ImagePlus("capture", capture);
				
				jf.dispose();
				
				imp.setProperty("Info", ModeleScin.genererDicomTagsPartie1(vue.getImp(), vue.getExamType())
						+ ModeleScin.genererDicomTagsPartie2(vue.getImp()));

				imp.show();
				IJ.setTool("hand");
			
				String[] arrayRes = vue.getFen_application().getControleur().getModele().getResultsAsArray();

				try {
					ModeleScin.exportAll(arrayRes, 2, vue.getFen_application().getControleur().getRoiManager(),
							vue.getExamType(), imp);

					vue.getFen_application().getControleur().getRoiManager().close();
					imp.killRoi();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				
				try {
					IJ.run("myDicom...");
				}catch(Exception e1){
					e1.printStackTrace();
				}
				
				
				System.gc();
			}
		});
	}

	private void attachListener() {
		ImagePlus.addImageListener(new ControleurImp(this));
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

	public ModeleScin getModele() {
		return this.leModele;
	}

	public String getNomOrgane(int index) {
		return this.getOrganes()[index % this.getOrganes().length];
	}

	public RoiManager getRoiManager() {
		return roiManager;
	}

}
