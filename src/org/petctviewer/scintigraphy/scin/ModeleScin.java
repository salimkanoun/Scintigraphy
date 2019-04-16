package org.petctviewer.scintigraphy.scin;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

public abstract class ModeleScin {

	private Integer uid;
	protected RoiManager roiManager;
	protected ImageSelection[] selectedImages;
	
	protected String studyName;
	
	public ModeleScin(ImageSelection[] selectedImages, String studyName) {
		this.roiManager = new RoiManager(false);
		this.setImageSelection(selectedImages);
		this.studyName = studyName;
	}

	public RoiManager getRoiManager() {
		return this.roiManager;
	}
	
	public void setImageSelection(ImageSelection[] selectedImages) {
		this.selectedImages = selectedImages;
	}
	
	public ImagePlus[] getImagesPlus() {
		ImagePlus[] selection = new ImagePlus[this.selectedImages.length];
		for(int i = 0; i<this.selectedImages.length; i++)
			selection[i] = this.selectedImages[i].getImagePlus();
		return selection;
	}
	
	public ImageSelection[] getImageSelection() {
		return this.selectedImages;
	}
	
	public ImagePlus getImagePlus() {
		return this.selectedImages[0].getImagePlus();
	}
	
	public String getStudyName() {
		return this.studyName;
	}

	/*********** Public Abstract *********/
	/**
	 * Enregistrer la mesure de la roi courante de l'image plus dans le format
	 * souhait�
	 * 
	 * @param nomRoi nom de la roi presente sur l'image plus
	 * @param imp    ImagePlus a traiter
	 */
	// public abstract void enregistrerMesure(String nomRoi, ImagePlus imp);

	public abstract void calculerResultats();

	/**
	 * calcule la decay fraction (countsCorrected=counts/decayedFraction)
	 * 
	 * @param delaySeconds
	 * @param halLifeSeconds
	 * @return
	 */
	public static double getDecayFraction(int delaySeconds, int halLifeSeconds) {
		double tcLambdaSeconds = (Math.log(2) / (halLifeSeconds));
		double decayedFraction = Math.pow(Math.E, (tcLambdaSeconds * delaySeconds * (-1)));
		// Decayed fraction est la fraction de la radioactivit� qui a disparu
		// Pour avoir les coups corrige de la decroissance
		// countsCorrected=counts/decayedFraction
		return decayedFraction;
	}

	/********** Public *******************/
	public String getUID6digits() {
		if (this.uid == null) {
			this.uid = (int) (Math.random() * 1000000.);
		}
		return this.uid.toString();
	}

	/**
	 * Permet de generer la 1ere partie du Header qui servira a la capture finale,
	 * l'iud est genere aleatoirement au premier appel de la fonction et reste le
	 * meme pour tout le modele
	 * 
	 * @param imp          : imageplus originale (pour recuperer des elements du
	 *                     Header tels que le nom du patient...)
	 * @param nomProgramme : nom du programme qui l'utilise si par exemple
	 *                     "pulmonary shunt" la capture sera appelee "Capture
	 *                     Pulmonary Shunt"
	 * @return retourne la premi�re partie du header en string auquelle on ajoutera
	 *         la 2eme partie via la deuxieme methode
	 */
	public String genererDicomTagsPartie1SameUID(ImagePlus imp, String nomProgramme) {
		String uid = getUID6digits();
		return Library_Capture_CSV.genererDicomTagsPartie1(imp, nomProgramme, uid);
	}

}