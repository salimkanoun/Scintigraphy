package org.petctviewer.scintigraphy.gastric_refactored;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;

public class GastricScintigraphy extends Scintigraphy {

	protected GastricScintigraphy() {
		super("Gastric Scintigraphy");
	}

	@Override
	public ImageSelection[] preparerImp(ImageSelection[] openedImages) throws WrongInputException {
		return null;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// TODO Auto-generated method stub

	}

}
