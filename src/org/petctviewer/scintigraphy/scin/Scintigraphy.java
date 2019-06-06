package org.petctviewer.scintigraphy.scin;

import ij.plugin.PlugIn;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;

public abstract class Scintigraphy implements PlugIn {

	private String studyName;
	private FenApplication fen_application;

	public Scintigraphy(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * Lance la fen�tre de dialogue permettant le lancemet du programme. </br>
	 * passer "standby" en parametre pour ne pas executer l'application directement
	 */
	@Override
	public void run(String arg) {
		// SK FAIRE DANS UN AUTRE THREAD ?
		FenSelectionDicom fen = new FenSelectionDicom(this);
		fen.setVisible(true);
	}

	/**
	 * This method should prepare the images that the user opened (like setting the
	 * right orientation...). This method should also check that the openedImages
	 * are correct (according to the program specification).<br>
	 * For instance, if this program need a specific amount of images, then it
	 * should check for that amount.<br>
	 * If this method returns null, then the program will NOT be launched.<br>
	 * If this method returns something, then the program will be launched.<br>
	 * During this process, if a DICOM tag could not be retrieve but can be ignored (like the name of the patient
	 * for instance) then this method do not have to throw a {@link ReadTagException}. But if a tag is necessary
	 * for this scintigraphy, then this method will throw an exception.
	 *
	 * @param openedImages Images that the user selected when clicking on the
	 *                     'Select' button in the FenSelectionDicom
	 * @return The well formatted images. If this return value is null, then the
	 * program will NOT be launched
	 * @throws WrongInputException if the images opened cannot be used by this controller (too many, not enough,
	 *                             wrong orientation...)
	 * @throws ReadTagException    if a DICOM tag could not be retrieve and <b>must</b> be present on the image
	 */
	public abstract ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException,
			ReadTagException;

	/**
	 * Launches the program with the specified images. This method implies that the
	 * selectedImages are well formatted for this program specification.<br>
	 * For instance, if this program needs 2 images in an Ant/Post orientation in a
	 * specific order, then the selectedImages must complies to that specification.
	 *
	 * @param selectedImages Images that the program will use. This images must be
	 *                       well formatted according to this program specification.
	 */
	public abstract void lancerProgramme(ImageSelection[] selectedImages);

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public FenApplication getFenApplication() {
		return this.fen_application;
	}

	public void setFenApplication(FenApplication fen_application) {
		this.fen_application = fen_application;
	}

}
