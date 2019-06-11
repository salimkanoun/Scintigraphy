package org.petctviewer.scintigraphy.shunpo;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShunpoScintigraphy extends Scintigraphy {

	private static final String ORGAN_KIDNEY_PULMON = "KIDNEY-PULMON", ORGAN_BRAIN = "BRAIN";
	public static final String STUDY_NAME = "Pulmonary Shunt";
	private Column orgranColumn;

	public ShunpoScintigraphy() {
		super(STUDY_NAME);
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// Start program
		this.setFenApplication(new FenApplicationWorkflow(selectedImages[0], this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowShunpo(this, (FenApplicationWorkflow) getFenApplication(), selectedImages));
		this.getFenApplication().setVisible(true);
	}

	@Override
	public String getName() {
		return STUDY_NAME;
	}

	@Override
	public Column[] getColumns() {
		// Orientation column
		String[] orientationValues = {Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString()};
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Organ column
		String[] organValues = {ORGAN_KIDNEY_PULMON, ORGAN_BRAIN};
		this.orgranColumn = new Column("Organ", organValues);

		// Choose columns to display
		return new Column[]{Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
							Column.STACK_SIZE,
							orientation, this.orgranColumn};
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check that number of images is correct
		if (selectedImages.size() != 2) throw new WrongNumberImagesException(selectedImages.size(), 2);

		if (selectedImages.get(0).getValue(this.orgranColumn.getName()) == selectedImages.get(1).getValue(
				this.orgranColumn.getName())) throw new WrongColumnException(orgranColumn, selectedImages.get(0).getRow(),
																			 "expecting " + ORGAN_KIDNEY_PULMON +
																					 " and " + ORGAN_BRAIN);

		// Order selectedImages: 1st KIDNEY-PULMON; 2nd BRAIN
		if (!selectedImages.get(0).getValue(this.orgranColumn.getName()).equals(ORGAN_KIDNEY_PULMON)) {
			Collections.swap(selectedImages, 0, 1);
		}

		// Check orientation
		List<ImageSelection> result = new ArrayList<>();
		for(ImageSelection ims : selectedImages) {
			result.add(Library_Dicom.ensureAntPostFlipped(ims));
			ims.close();
		}

		return result;
	}

	@Override
	public String instructions() {
		return "2 images in Ant-Post or Post-Ant orientation";
	}
}