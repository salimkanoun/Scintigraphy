package org.petctviewer.scintigraphy.scin;

import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

/**
 * Represents the model in the MVC pattern.
 * 
 * @author Titouan QUÉMA
 *
 */
public abstract class ModeleScin {

	private Integer uid;
	protected RoiManager roiManager;
	protected ImageSelection[] selectedImages;

	protected String studyName;

	public ModeleScin(ImageSelection[] selectedImages, String studyName) {
		this.roiManager = new RoiManager(false);
		this.selectedImages = selectedImages;
		this.studyName = studyName;
	}

	/**
	 * Calculates the results for this study. This method should be called by the
	 * controller when all the data has been loaded in this model.
	 */
	public abstract void calculerResultats();

	/**
	 * Consider using {@link #getImagePlus()} if you just need to get access to the
	 * first (main) image of this model, since this method is creating a new array,
	 * it is less efficient.
	 * 
	 * @return all of the selected images passed to this model converted to
	 *         ImagePlus
	 * @deprecated This method is not optimized, please consider using
	 *             {@link #getImageSelection()} instead!
	 */
	@Deprecated
	public ImagePlus[] getImagesPlus() {
		ImagePlus[] selection = new ImagePlus[this.selectedImages.length];
		for (int i = 0; i < this.selectedImages.length; i++)
			selection[i] = this.selectedImages[i].getImagePlus();
		return selection;
	}

	/**
	 * @return all of the selected images passed to this model
	 */
	public ImageSelection[] getImageSelection() {
		return this.selectedImages;
	}

	/**
	 * This method returns the first image of the selected images passed to this
	 * model.<br>
	 * This method is preferable from using this.getImagesPlus()[0].getImagePlus().
	 * 
	 * @return ImagePlus the first (main) image of this model
	 */
	public ImagePlus getImagePlus() {
		return this.selectedImages[0].getImagePlus();
	}

	/**
	 * This method is mainly used for display purposes.
	 * 
	 * @return study name of this model
	 */
	public String getStudyName() {
		return this.studyName;
	}

	/**
	 * Modifies the set of images used for this application.<br>
	 * <i>Be careful: </i>This method is not trivial and will result in the
	 * modification of the images used by all of the program.
	 * 
	 * @param newImages New images to use
	 */
	public void setImages(ImageSelection[] newImages) {
		this.selectedImages = newImages;
	}

	// ============================== OLD CLASS ========================

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

	public RoiManager getRoiManager() {
		return this.roiManager;
	}

}