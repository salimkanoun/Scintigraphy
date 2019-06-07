package org.petctviewer.scintigraphy.os;

import ij.ImagePlus;
import ij.Prefs;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabBone;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * DISCLAIMER : Dans cette application, il a été fait comme choix d'initialiser
 * le module par le biais du Contrôleur, qui va ensuite créer la vue et le
 * modèle.
 */
public class Modele_Os {

	// Tableau permettant de savoir quel DynamicImage sont selectionnées
	final boolean[][] selected;

	// Tableau à double dimension contenant les ImagePlus liées aux DynamicImage
	// de(s) Scintigraphie(s) Osseuse(s)
	private final ImageSelection[][] imps;

	// Tableau à double dimension contenant les ImagePlus liées aux DynamicImage
	// de(s) Scintigraphie(s) Osseuse(s)
	private final ImageSelection imp;

	// Tableau à double dimension contenant les DynamicImage liées aux ImagePlus
	// de(s) Scintigraphie(s) Osseuse(s)
	final DynamicImage[][] dynamicImps;
	private final int nbScinty;
	private boolean reversed;

	private Integer uid;

	public Modele_Os(ImageSelection[] imps) {
		nbScinty = imps.length;

		this.reversed = false;

		this.selected = new boolean[nbScinty][2];
		this.dynamicImps = new DynamicImage[nbScinty][2];
		this.imps = new ImageSelection[nbScinty][2];

		for (int i = 0; i < imps.length; i++) {
			for (int j = 0; j < 2; j++) {

				ImageSelection Ant = imps[i].clone();
				Ant.setImagePlus(new ImagePlus("Ant", imps[i].getImagePlus().getStack().getProcessor(1)));
				// Ant.getImagePlus().setProperty("Info",
				// imps[i].getImagePlus().getStack().getSliceLabel(1));
				Ant.getImagePlus().setProperty("Info", imps[i].getImagePlus().getInfoProperty());
				this.imps[i][0] = Ant;

				ImageSelection Post = imps[i].clone();
				Post.setImagePlus(new ImagePlus("Post", imps[i].getImagePlus().getStack().getProcessor(2)));
				// Post.getImagePlus().setProperty("Info",
				// imps[i].getImagePlus().getStack().getSliceLabel(2));
				Post.getImagePlus().setProperty("Info", imps[i].getImagePlus().getInfoProperty());
				this.imps[i][1] = Post;
			}
		}

		this.imp = this.imps[0][0];
		// Récupération dee la préférence d'application de la Lut (coloration des
		// images). Si il faut appliquer une Lut particulière
		// Appelle de la méthode permettant d'appliquer la Lut si on applique pas la Lut
		// par défaut.
		if (Prefs.get(PrefTabBone.PREF_USE_CUSTOM_LUT, false)) {
			for (ImageSelection[] imgs : this.imps)
				for (ImageSelection img : imgs)
					Library_Gui.setCustomLut(img.getImagePlus(), PrefTabBone.PREF_LUT);
		}

		// For every Scintigraphy
		for (int i = 0; i < nbScinty; i++) {
			// For ANT and POST of the Scintigraphy
			for (int j = 0; j < 2; j++) {
				if (this.dynamicImps[i][j] == null) {
					// If it is not already displayed.
					if (this.imps[i][j] != null) {
						// Getting Image from the list of ImagePlus
						BufferedImage imgbuffered = this.imps[i][j].getImagePlus().getBufferedImage();
						// Creating the new Panel displaying the Image
						this.dynamicImps[i][j] = new DynamicImage(imgbuffered);
						// Drawing informations in the image
						displayInformations(dynamicImps[i][j], i, j);

					}
				}
			}
		}

		if (Prefs.get("bone.defaultInverse.preferred", true))
			this.inverser();

	}

	/**
	 * Permmet d'inverser la LUT de chaque image, et donc son contraste.
	 *
	 */
	public void inverser() {
		// Pour toutes les images
		for (int i = 0; i < nbScinty; i++) {
			for (int j = 0; j < 2; j++) {
				// On inverse la LUT
				imps[i][j].getImagePlus().setLut(imps[i][j].getImagePlus().getLuts()[0].createInvertedLut());
				// On recharge lea DynamicImage depuis la ImagePlus correspondante.
				dynamicImps[i][j].setImage(imps[i][j].getImagePlus().getBufferedImage());
				// On réaffiche
				dynamicImps[i][j].repaint();
				// On affiche les informations (sinon elles disparaissent)
				displayInformations(dynamicImps[i][j], i, j);
				if (reversed) {
					dynamicImps[i][j].setBackground(Color.black);
				} else {
					dynamicImps[i][j].setBackground(Color.white);
				}
			}
		}
		this.reversed = !this.reversed;
		Prefs.set("bone.defaultInverse.preferred", reversed);
		Prefs.savePreferences();
	}

	/**
	 * Affiche les information sur une DynamicImage.<br/>
	 * 1 - Récupère les information de l'image correspondante.<br/>
	 * 2 - Charge un Object Graphique, associé à l'ImageDynamic<br/>
	 * 3 - Ecrit un rectangle et la date sur l'Objet Graphique créé<br/>
	 *
	 * @param dyn
	 *            DynamicImage sur laquelle écrire.
	 * @param i
	 *            int représentant la position du patient
	 * @param j
	 *            int représentant la position de l'image du patient (0=ANT |
	 *            1=POST).
	 */
	public void displayInformations(DynamicImage dyn, int i, int j) {
		ImagePlus impCurrent = imps[i][j].getImagePlus();
		// On récupère les informations liées à l'ImagePlus
		HashMap<String, String> infoPatient = Library_Capture_CSV.getPatientInfo(impCurrent);

		int fontLenght = impCurrent.getWidth() / infoPatient.get("date").length();

		// On crée un objet graphique qui va être appliquer à la Image de notre Dynamic
		// Image
		Graphics g = dyn.getImage().getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(4, impCurrent.getHeight() * 97 / 100 - fontLenght,
				infoPatient.get("date").length() * fontLenght / 2 + 3, fontLenght + 3);
		g.setColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.PLAIN, fontLenght));
		// On dessine le texte sur l'image
		g.drawString(infoPatient.get("date"), 5, impCurrent.getHeight() * 97 / 100);
		g.dispose();

	}

	public DynamicImage getDynamicImage(int index) {
		return dynamicImps[index / 2][index % 2];
	}

	public DynamicImage getDynamicImage(int i, int j) {
		return dynamicImps[i][j];
	}

	public ImageSelection getImagePlus(int index) {
		return imps[index / 2][index % 2];
	}

	public ImageSelection getImagePlus(int i, int j) {
		return imps[i][j];
	}

	/**
	 * On change le contraste pour toutes les DynamicImage selectionnée, en
	 * parcourant toutes les ImagePlus, en changeant leur LUT, puis en ré affichant
	 * les DynamicImage correspondantes.
	 */
	void setContrast(JSlider slider) {

		if (this.noImageSelected()) {
			SwingUtilities.invokeLater(() -> {
				// Pour toutes les DynamicImage
				for (int i = 0; i < nbScinty; i++)
					for (int j = 0; j < 2; j++) {
						imps[i][j].getImagePlus().getProcessor().setMinAndMax(0,
								(slider.getModel().getMaximum() - slider.getValue()) + 1);
						// On récupère l'ImagePlus associée
						dynamicImps[i][j].setImage(imps[i][j].getImagePlus().getBufferedImage());
						// On l'actualise
						dynamicImps[i][j].repaint();
						// On affiche les informations (sinon elles disparaissent)
						displayInformations(dynamicImps[i][j], i, j);
					}
			});

		} else {
			// Lancement en tache de fond, pour ne pas bloquer le thread principal
			SwingUtilities.invokeLater(() -> {
				// Pour toutes les DynamicImage
				for (int i = 0; i < nbScinty; i++)
					for (int j = 0; j < 2; j++)
						if (isSelected(dynamicImps[i][j])) {
							imps[i][j].getImagePlus().getProcessor().setMinAndMax(0,
									(slider.getModel().getMaximum() - slider.getValue()) + 1);
							// On récupère l'ImagePlus associée
							dynamicImps[i][j].setImage(imps[i][j].getImagePlus().getBufferedImage());
							// On l'actualise
							dynamicImps[i][j].repaint();
							// On affiche les informations (sinon elles disparaissent)
							displayInformations(dynamicImps[i][j], i, j);
						}
			});
		}
	}

	/**
	 * Permet de renseigner la selection ou l'arrêt de sa selection.<br/>
	 * Pour savoir si un ImagePlus et son DynamicImage correspondante est
	 * selectionnée, <br/>
	 * un tableau de boolean enregistre les position de chaque ImagePlus et indique
	 * si elle est selectionnée ou non.
	 *
	 * @param i
	 *            int représentant la position du patient
	 * @param j
	 *            int représentant la position de l'image du patient (0=ANT |
	 *            1=POST).
	 */
	public void perform(int i, int j) {
		selected[i][j] = !selected[i][j];
	}

	/**
	 * Retourne si la imp est selectionnée ou non, grâce à sa position passée en
	 * paramètre.
	 *
	 * @param i
	 *            int représentant la position du patient dans le tableau de
	 *            ImagePlus
	 * @param j
	 *            int représentant la position de l'image du patient (0=ANT |
	 *            1=POST).
	 * @return boolean
	 */
	public boolean isSelected(int i, int j) {
		return this.selected[i][j];
	}

	/**
	 * Retourne si la DynamicImage passée en paramètre est selectionnée ou non.<br/>
	 * Récupère d'abord la position via position(DynamicImage)
	 *
	 * @param dyn
	 *            DynamicImage dont il faut retourner la position.
	 * @return boolean
	 */
	public boolean isSelected(DynamicImage dyn) {
		int[] position = position(dyn);
		return this.selected[position[0]][position[1]];
	}

	/**
	 * Retourne si la DynamicImage passée en paramètre est selectionnée ou non.
	 *
	 * @param dyn
	 *            DynamicImage dont il faut retourner la position.
	 * @return boolean
	 */
	public boolean isSelected(DynamicImage dyn, int i, int j) {
		return this.selected[i][j];
	}

	/**
	 * Retourne si la ImagePlus passée en paramètre est selectionnée ou non.<br/>
	 * Récupère d'abord la position via position(ImagePlus)
	 *
	 * @param imp
	 *            ImagePlus dont il faut retourner la position.
	 * @return boolean
	 */
	public boolean isSelected(ImageSelection imp) {
		return this.selected[position(imp)[0]][position(imp)[1]];
	}

	/**
	 * Retourne si la imp passée en paramètre est selectionnée ou non.
	 *
	 * @param imp
	 *            ImagePlus dont il faut retourner la position.
	 * @param i
	 *            int représentant la position du patient dans le tableau de
	 *            ImagePlus
	 * @param j
	 *            int représentant la position de l'image du patient (0=ANT |
	 *            1=POST).
	 * @return boolean
	 */
	public boolean isSelected(ImagePlus imp, int i, int j) {
		return this.selected[i][j];
	}

	public boolean noImageSelected() {
		for (boolean[] bool : selected)
			for (boolean boo : bool)
				if (boo)
					return false;
		return true;
	}

	public List<Integer> getSelected() {
		List<Integer> selectionnes = new ArrayList<>();
		for (int i = 0; i < this.selected.length * this.selected[0].length; i++) {
			if (selected[i / 2][i % 2]) {
				selectionnes.add(i);
			}
		}

		return selectionnes;
	}

	/**
	 * Parcours le tableau stockant les DynamixImage et retourne la position de la
	 * DynamicImage passée en paramètre.
	 *
	 * @param dyn
	 *            DynamicImage dont il faut retourner la position dans le tableau
	 *            stockant les DynamicImage.
	 * @return int[] (Tableau de 2 entiers correspondant aux position dans le
	 *         tableau à double entrée stockant les ImagePlus)
	 */
	public int[] position(DynamicImage dyn) {
		int[] location = new int[2];
		for (int i = 0; i < nbScinty; i++) {
			for (int j = 0; j < 2; j++) {
				if (dyn == dynamicImps[i][j]) {
					location[0] = i;
					location[1] = j;
				}
			}
		}
		return location;
	}

	/**
	 * Parcours le tableau stockant les ImagePlus et retourne la position de la
	 * ImagePlus passée en paramètre.
	 *
	 * @param image
	 *            ImagePlus dont il faut retourner la position dans le tableau
	 *            stockant les ImagePlus.
	 * @return int[] (Tableau de 2 entiers correspondant aux position dans le
	 *         tableau à double entrée stockant les ImagePlus)
	 */
	public int[] position(ImageSelection image) {
		int[] location = new int[2];
		for (int i = 0; i < nbScinty; i++) {
			for (int j = 0; j < 2; j++) {
				if (image == imps[i][j]) {
					location[0] = i;
					location[1] = j;
				}
			}
		}
		return location;
	}

	public int getNbScinti() {
		return imps.length;
	}

	public ImageSelection getImageSelection() {
		return this.imp;
	}

	public void deselectAll() {
		for (int i = 0; i < this.selected.length; i++)
			for (int j = 0; j < this.selected[0].length; j++) {
				this.selected[i][j] = false;
			}
	}


	public String getStudyName() {
		return "Bone Scintigraphy";
	}

	public ImagePlus getImagePlus() {
		return this.imp.getImagePlus();
	}

	public String getUID6digits() {
		if (this.uid == null) {
			this.uid = (int) (Math.random() * 1000000.);
		}
		return this.uid.toString();
	}

	@Override
	public String toString() {
		return "";
	}

}
