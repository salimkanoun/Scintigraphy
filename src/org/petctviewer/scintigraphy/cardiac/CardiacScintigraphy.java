package org.petctviewer.scintigraphy.cardiac;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Overlay;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;

public class CardiacScintigraphy extends Scintigraphy {

	public static final String FULL_BODY_IMAGE = "FULL_BODY", ONLY_THORAX_IMAGE = "ONLY_THORAX",
			COLUMN_TYPE_TITLE = "Image Type";
	private Column imageTypeColumn;

	List<ImageSelection> fullBodyImages;
	List<ImageSelection> onlyThoraxImage;

	public CardiacScintigraphy() {
		super("Cardiac");
	}

	@Override
	public void run(String arg) {
		// Override to use custom dicom selection window
		FenSelectionDicom fen = new FenSelectionDicom(this);

		// Orientation column
		String[] orientationValues = { Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString() };
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Organ column
		String[] typesValues = { FULL_BODY_IMAGE, ONLY_THORAX_IMAGE };
		this.imageTypeColumn = new Column(COLUMN_TYPE_TITLE, typesValues);

		// Choose columns to display
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation, this.imageTypeColumn };
		fen.declareColumns(cols);

		fen.setVisible(true);
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {

		this.fullBodyImages = new ArrayList<>();
		this.onlyThoraxImage = new ArrayList<>();

		// Check number
		for (ImageSelection selected : selectedImages) {
			if (selected.getValue(this.imageTypeColumn.getName()) == FULL_BODY_IMAGE) {
				if (fullBodyImages.size() == 2)
					throw new WrongNumberImagesException(fullBodyImages.size(), 1, 2);
				fullBodyImages.add(selected);
			}
			if (selected.getValue(this.imageTypeColumn.getName()) == ONLY_THORAX_IMAGE) {
				if (onlyThoraxImage.size() == 1)
					throw new WrongNumberImagesException(fullBodyImages.size(), 1);
				onlyThoraxImage.add(selected);
			}
		}

		if (onlyThoraxImage.size() == 0 && fullBodyImages.size() == 0)
			throw new WrongNumberImagesException(0, 1, 3);

		ArrayList<ImageSelection> mountedImages = new ArrayList<>();

		int[] frameDuration = new int[2];

		for (int i = 0; i < fullBodyImages.size(); i++) {

			if (fullBodyImages.get(i).getImageOrientation() == Orientation.ANT_POST
					|| fullBodyImages.get(i).getImageOrientation() == Orientation.POST_ANT) {
				ImageSelection imp = fullBodyImages.get(i);
				String info = imp.getImagePlus().getInfoProperty();
				ImageSelection impReversed = Library_Dicom.ensureAntPostFlipped(imp);
				MontageMaker mm = new MontageMaker();
				ImageSelection montageImage = impReversed.clone();
				montageImage.setImagePlus(mm.makeMontage2(impReversed.getImagePlus(), 2, 1, 1.0, 1, 2, 1, 0, false));
				montageImage.getImagePlus().setProperty("Info", info);
				frameDuration[i] = Integer.parseInt(DicomTools.getTag(imp.getImagePlus(), "0018,1242").trim());
				mountedImages.add(montageImage);
			} else {
				throw new WrongColumnException.OrientationColumn(fullBodyImages.get(i).getRow(),
						fullBodyImages.get(i).getImageOrientation(),
						new Orientation[] { Orientation.ANT_POST, Orientation.POST_ANT });
			}
			fullBodyImages.get(i).getImagePlus().close();
		}

		List<ImageSelection> mountedSorted = new ArrayList<>();
		mountedImages.sort(new ChronologicalAcquisitionComparator());
		mountedSorted = mountedImages;

		// si il y a plus de 3 minutes de diff�rence entre les deux prises
		if (Math.abs(frameDuration[0] - frameDuration[1]) > 3 * 60 * 1000) {
			IJ.log("Warning, frame duration differ by " + Math.abs(frameDuration[0] - frameDuration[1]) / (1000 * 60)
					+ " minutes");
		}

		if (this.onlyThoraxImage.size() != 0)
			mountedSorted.add(onlyThoraxImage.get(0));

		return mountedSorted;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 7);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.YELLOW);

		String[] infoOfAllImages = new String[selectedImages.length];
		for (int indexImage = 0; indexImage < selectedImages.length; indexImage++)
			infoOfAllImages[indexImage] = selectedImages[indexImage].getImagePlus().duplicate().getInfoProperty();

		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(selectedImages[0], this.getStudyName(),
				this.fullBodyImages.size() != 0, this.onlyThoraxImage.size() != 0));
		selectedImages[0].getImagePlus().setOverlay(overlay);

		// Cree controller
		this.getFenApplication()
				.setController(new ControllerWorkflowCardiac(this, (FenApplicationWorkflow) this.getFenApplication(),
						new Model_Cardiac(this, selectedImages, "Cardiac", infoOfAllImages), this.fullBodyImages.size(),
						this.onlyThoraxImage.size()));

		// ((ControllerWorkflowCardiac)this.getFenApplication().getController()).setFullBodyImages(this.fullBodyImages.size());
		// ((ControllerWorkflowCardiac)this.getFenApplication().getController()).setOnlyThoraxImage(this.onlyThoraxImage.size());

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Cardiac Scintigraphy";
	}

	@Override
	public Column[] getColumns() {

		// Orientation column
		String[] orientationValues = { Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString() };
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Organ column
		String[] typesValues = { FULL_BODY_IMAGE, ONLY_THORAX_IMAGE };
		this.imageTypeColumn = new Column(COLUMN_TYPE_TITLE, typesValues);

		// Choose columns to display
		Column[] cols = { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation, this.imageTypeColumn };
		return cols;
	}

	@Override
	public String instructions() {
		// TODO Auto-generated method stub
		return "You should open 1 or 2 full body images, or/and 1 thorax image.";
	}

}