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
	}

	@Override
	public abstract void start(List<ImageSelection> preparedImages);

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
