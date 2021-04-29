package org.petctviewer.scintigraphy.cardiac;

import ij.IJ;
import ij.gui.Overlay;
import ij.plugin.MontageMaker;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CardiacScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "Amylose";

	public static final String FULL_BODY_IMAGE = "FULL_BODY", ONLY_THORAX_IMAGE = "ONLY_THORAX",
			COLUMN_TYPE_TITLE = "Image Type";
	List<ImageSelection> fullBodyImages;
	List<ImageSelection> onlyThoraxImage;
	private Column imageTypeColumn;

	public CardiacScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("With Kidney", "RAPEZZI Jacc 2011",
																   "https://www.ncbi.nlm.nih.gov/pubmed/21679902"));
		doc.addReference(DocumentationDialog.Field.createLinkField("Thorax alone", "SINGH J Nucl Cardiol 2018",
																   "https://www.ncbi.nlm.nih.gov/pubmed/30569412"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		initOverlayOnPreparedImages(preparedImages, 7);
		Library_Gui.setOverlayDG(preparedImages.get(0).getImagePlus(), Color.YELLOW);

		String[] infoOfAllImages = new String[preparedImages.size()];
		for (int indexImage = 0; indexImage < preparedImages.size(); indexImage++)
			infoOfAllImages[indexImage] = preparedImages.get(indexImage).getImagePlus().duplicate().getInfoProperty();

		// fenetre de l'application
		this.setFenApplication(new FenApplication_Cardiac(preparedImages.get(0), this.getStudyName(),
				this.fullBodyImages.size() > 0, this.onlyThoraxImage.size() > 0));

		// Cree controller
		this.getFenApplication().setController(
				new ControllerWorkflowCardiac((FenApplicationWorkflow) this.getFenApplication(),
											  new Model_Cardiac(this, preparedImages.toArray(new ImageSelection[0]),
																"Cardiac", infoOfAllImages),
											  this.fullBodyImages.size(),
											  this.onlyThoraxImage.size()));

		this.createDocumentation();

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
		return new Column[] { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation, this.imageTypeColumn };
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException {
		this.fullBodyImages = new ArrayList<>();
		this.onlyThoraxImage = new ArrayList<>();

		// Check number
		for (ImageSelection selected : selectedImages) {
			if (selected.getValue(this.imageTypeColumn.getName()).equals(FULL_BODY_IMAGE)) {
				if (fullBodyImages.size() == 2)
					throw new WrongNumberImagesException(fullBodyImages.size(), 1, 2);
				fullBodyImages.add(selected);
			}
			if (selected.getValue(this.imageTypeColumn.getName()).equals(ONLY_THORAX_IMAGE)) {
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
		}

		mountedImages.sort(new ChronologicalAcquisitionComparator());

		// si il y a plus de 3 minutes de diff�rence entre les deux prises
		int diff = Math.abs(frameDuration[0] - frameDuration[1]);
		if (diff > 3 * 60 * 1000) {
			IJ.log("Warning, frame duration differ by " + diff / (1000 * 60) + " minutes");
		}

		if (this.onlyThoraxImage.size() != 0)
			mountedImages.add(onlyThoraxImage.get(0).clone());

		for (ImageSelection selected : selectedImages)
			selected.close();

		return mountedImages;
	}

	@Override
	public String instructions() {
		return "You should open 1 or 2 full body images, or/and 1 thorax image.";
	}
}
