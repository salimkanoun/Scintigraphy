package org.petctviewer.scintigraphy.scin;

import ij.plugin.PlugIn;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Debug;

import java.util.List;

public abstract class Scintigraphy implements PlugIn, ImagePreparator {

	private String studyName;
	private FenApplication fen_application;

	public Scintigraphy(String studyName) {
		this.studyName = Library_Debug.replaceNull(studyName);
	}

	/**
	 * Launch this application by first selecting the images needed for this program.
	 */
	@Override
	public void run(String arg) {
		FenSelectionDicom fen = new FenSelectionDicom(this);
		fen.setVisible(true);
		List<ImageSelection> selectedImages = fen.retrieveSelectedImages();
		this.start(selectedImages.toArray(new ImageSelection[0]));
	}

	/**
	 * Launches the program with the specified images. This method implies that the selectedImages are well formatted
	 * for this program specification.<br> For instance, if this program needs 2 images in an Ant/Post orientation in a
	 * specific order, then the selectedImages must complies to that specification.
	 *
	 * @param selectedImages Images that the program will use. This images must be well formatted according to this
	 *                       program specification.
	 */
	public abstract void start(ImageSelection[] selectedImages);

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
