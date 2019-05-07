package org.petctviewer.scintigraphy.scin;

import java.util.HashMap;

import org.json.simple.JSONObject;
import org.petctviewer.scintigraphy.lympho.gui.TabPost;
import org.petctviewer.scintigraphy.lympho.post.ControllerWorkflowPelvis;
import org.petctviewer.scintigraphy.lympho.post.ModelePost;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;
import ij.plugin.frame.RoiManager;
import ij.util.DicomTools;

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
	
	
	
	/**
	 * Get tags to put in CSV.
	 */
	@Override
	public String toString() {
		String s = "";

		HashMap<String, String> mapTags = new HashMap<>();
		mapTags.put("0008,0020", DicomTools.getTag(this.getImagePlus(), "0008,0020"));
		mapTags.put("0008,0021", DicomTools.getTag(this.getImagePlus(), "0008,0021"));
		mapTags.put("0008,0030", DicomTools.getTag(this.getImagePlus(), "0008,0030"));
		mapTags.put("0008,0031", DicomTools.getTag(this.getImagePlus(), "0008,0031"));
		mapTags.put("0008,0050", DicomTools.getTag(this.getImagePlus(), "0008,0050"));
		mapTags.put("0008,0060", DicomTools.getTag(this.getImagePlus(), "0008,0060"));
		mapTags.put("0008,0070", DicomTools.getTag(this.getImagePlus(), "0008,0070"));
		mapTags.put("0008,0080", DicomTools.getTag(this.getImagePlus(), "0008,0080"));
		mapTags.put("0008,0090", DicomTools.getTag(this.getImagePlus(), "0008,0090"));
		mapTags.put("0008,1030", DicomTools.getTag(this.getImagePlus(), "0008,1030"));
		mapTags.put("0010,0010", DicomTools.getTag(this.getImagePlus(), "0010,0010"));
		mapTags.put("0010,0020", DicomTools.getTag(this.getImagePlus(), "0010,0020"));
		mapTags.put("0010,0030", DicomTools.getTag(this.getImagePlus(), "0010,0030"));
		mapTags.put("0010,0040", DicomTools.getTag(this.getImagePlus(), "0010,0040"));
		mapTags.put("0020,000D", DicomTools.getTag(this.getImagePlus(), "0020,000D"));
		mapTags.put("0020,000E", DicomTools.getTag(this.getImagePlus(), "0020,000E"));
		mapTags.put("0020,0010", DicomTools.getTag(this.getImagePlus(), "0020,0010"));
		mapTags.put("0020,0032", DicomTools.getTag(this.getImagePlus(), "0020,0032"));
		mapTags.put("0020,0037", DicomTools.getTag(this.getImagePlus(), "0020,0037"));

		String tags = JSONObject.toJSONString(mapTags);

		s += "\n" + "tags," + tags;
		
		return s;
	}

}