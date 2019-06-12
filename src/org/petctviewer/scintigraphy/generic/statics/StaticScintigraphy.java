package org.petctviewer.scintigraphy.generic.statics;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Toolbar;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaticScintigraphy extends Scintigraphy {
public static final String STUDY_NAME = "General static scintigraphy";
	private boolean isSingleSlice;
	private boolean isAnt;

	public StaticScintigraphy() {
		super(STUDY_NAME);
		this.isSingleSlice = true;
		this.isAnt = true;
	}

	@Override
	public void start(ImageSelection[] selectedImages) {

		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.white);

		this.setFenApplication(new FenApplication_ScinStatic(selectedImages[0], this.getStudyName()));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		this.getFenApplication().setController(
				new ControllerWorkflow_ScinStatic(this, (FenApplicationWorkflow) getFenApplication(), selectedImages,
												  getStudyName()));

		((ModelScinStatic) this.getFenApplication().getController().getModel()).setIsSingleSlide(this.isSingleSlice);
		((ModelScinStatic) this.getFenApplication().getController().getModel()).setIsAnt(this.isAnt);
		IJ.setTool(Toolbar.POLYGON);
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number
		if (selectedImages.size() != 1) {
			throw new WrongNumberImagesException(selectedImages.size(), 1);
		}
		ImageSelection selection = selectedImages.get(0);
		List<ImageSelection> selections = new ArrayList<>();
		// SK ETENDRE A SEULEMENT UNE INCIDENCE ??
		// SK PAS SUR QUE POST ANT SOIT BIEN PRIS EN COMPTE DANS LE FLIP / Ordre du
		// stack
		if (selection.getImageOrientation() == Orientation.ANT_POST ||
				selection.getImageOrientation() == Orientation.POST_ANT) {
			selections.add(Library_Dicom.ensureAntPostFlipped(selection));
			this.isSingleSlice = false;
		} else if (selection.getImageOrientation() == Orientation.ANT) {
			selections.add(selection.clone());
		} else if (selection.getImageOrientation() == Orientation.POST) {
			selections.add(selection.clone());
			this.isAnt = false;
		} else {
			throw new WrongColumnException.OrientationColumn(selection.getRow(), selection.getImageOrientation(),
															 new Orientation[]{Orientation.ANT_POST,
																			   Orientation.POST_ANT});
		}
		selection.close();

		return selections;
	}

	@Override
	public String instructions() {
		return "1 image oriented Ant-Post, Ant or Post";
	}
}
