package org.petctviewer.scintigraphy.scin;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

import ij.plugin.PlugIn;

public abstract class Scintigraphy implements PlugIn {

	private String examType;
	private FenApplication fen_application;

	protected Scintigraphy(String examType) {
		this.examType = examType;
	}

	/*************************** Public ************************/
	/**
	 * Lance la fen�tre de dialogue permettant le lancemet du programme. </br>
	 * passer "standby" en parametre pour ne pas executer l'application directement
	 */
	@Override
	public void run(String arg) {
		// SK FAIRE DANS UN AUTRE THREAD ?
		FenSelectionDicom fen = new FenSelectionDicom(this.getExamType(), this);
		fen.setVisible(true);
	}

	/*************************** Abstract ************************/
	/**
	 * This method should prepare the images that the user opened (like setting the
	 * right orientation...). This method should also check that the openedImages
	 * are correct (according to the program specificities).<br>
	 * For instance, if this program need a specific amount of images, then it
	 * should check for that amount.<br>
	 * If this method returns null, then the program will NOT be launched.<br>
	 * If this method returns something, then the program will be launched.
	 * 
	 * @param openedImages Images that the user selected when clicking on the
	 *                     'Select' button in the FenSelectionDicom
	 * @return The well formatted images. If this return value is null, then the
	 *         program will NOT be launched
	 */
	public abstract ImageSelection[] preparerImp(ImageSelection[] openedImages) throws Exception;

	/**
	 * Launches the program with the specified images. This method implies that the
	 * selectedImages are well formatted for this program specificities.<br>
	 * For instance, if this program needs 2 images in an Ant/Post orientation in a
	 * specific order, then the selectedImages must complies to that specification.
	 * 
	 * @param selectedImages Images that the program will use. This images must be
	 *                       well formatted according to this program specificities.
	 */
	public abstract void lancerProgramme(ImageSelection[] selectedImages);

	public void setExamType(String examType) {
		this.examType = examType;
	}

	public void setFenApplication(FenApplication fen_application) {
		this.fen_application = fen_application;
	}

	/********************** Getter **************************/

	public String getExamType() {
		return this.examType;
	}

	public FenApplication getFenApplication() {
		return this.fen_application;
	}

}
