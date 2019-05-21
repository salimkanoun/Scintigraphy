package org.petctviewer.scintigraphy.colonic;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;

public class ColonicScintigraphy extends Scintigraphy {

	public ColonicScintigraphy(String studyName) {
		super(studyName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// TODO Auto-generated method stub

	}

}
