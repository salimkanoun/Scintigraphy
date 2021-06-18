package org.petctviewer.scintigraphy.mibg;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DisplayState;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class MIBGScintigraphy extends Scintigraphy {

	public static final String STUDY_NAME = "MIBG Scintigraphy";

	// [0: ant | 1: post][numAcquisition]
	private ImageSelection[] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

	public MIBGScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.setDeveloper("Angele Mateos");
		doc.addReference(DocumentationDialog.Field.createLinkField("",
				"CARRIÓ - JACC Cardiovasc Imaging. 2010", "https://pubmed.ncbi.nlm.nih.gov/20129538/"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	
	/** 
	 * @param listener
	 */
	private void inflateMenuBar(ItemListener listener) {
		// Menu change labels
		Menu menu = new Menu("Display");
		RadioGroup group = new RadioGroup();

		CheckboxMenuItem itemChangeLabelRL = new CheckboxMenuItem(DisplayState.RIGHT_LEFT.label);
		group.addRadioItem(itemChangeLabelRL);
		menu.add(itemChangeLabelRL);

		CheckboxMenuItem itemChangeLabelLR = new CheckboxMenuItem(DisplayState.LEFT_RIGHT.label);
		group.addRadioItem(itemChangeLabelLR);
		menu.add(itemChangeLabelLR);

		CheckboxMenuItem itemChangeLabelAP = new CheckboxMenuItem(DisplayState.ANT_POST.label, true); // default
		group.addRadioItem(itemChangeLabelAP);
		menu.add(itemChangeLabelAP);

		// Add listeners
		itemChangeLabelRL.addItemListener(listener);
		itemChangeLabelLR.addItemListener(listener);
		itemChangeLabelAP.addItemListener(listener);

		this.getFenApplication().getMenuBar().add(menu);
	}

	
	/** 
	 * @return ImageSelection
	 */
	public ImageSelection getImgPrjtAllAcqui() {
		ImageSelection returned = this.sauvegardeImagesSelectDicom[0];
		returned.setImagePlus(impProjeteAllAcqui);
		return returned;
	}

	
	/** 
	 * @param preparedImages
	 */
	@Override
	public void start(List<ImageSelection> preparedImages) {
		// Start program
		this.initOverlayOnPreparedImages(preparedImages);
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(new ControllerWorkflowMIBG(STUDY_NAME,
				(FenApplicationWorkflow) getFenApplication(), this.sauvegardeImagesSelectDicom));

		this.createDocumentation();
		this.inflateMenuBar((ControllerWorkflowMIBG) this.getFenApplication().getController());

		this.getFenApplication().setVisible(true);
	}

	
	/** 
	 * @return Column[]
	 */
	@Override
	public Column[] getColumns() {
		// Orientation column
		String[] orientationValues = { Orientation.ANT.toString(), Orientation.ANT_POST.toString(),
				Orientation.POST_ANT.toString() };
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Choose columns to display
		return new Column[] { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
				Column.STACK_SIZE, orientation };
	}

	
	/** 
	 * @param selectedImages
	 * @return List<ImageSelection>
	 * @throws WrongInputException
	 */
	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
			throws WrongInputException {
		// Check that number of images is correct
		if (selectedImages.size() != 2)
			throw new WrongNumberImagesException(selectedImages.size(), 2);

		// sauvegarde des images pour le modele
		this.sauvegardeImagesSelectDicom = new ImageSelection[selectedImages.size()];

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
		List<ImageSelection> imagePourTrie = new ArrayList<>();

		// pour chaque acquisition
		for (ImageSelection selectedImage : selectedImages) {
			if (selectedImage.getImageOrientation() == Orientation.ANT_POST
					|| selectedImage.getImageOrientation() == Orientation.POST_ANT) {
				imagePourTrie.add(Library_Dicom.ensureAntPostFlipped(selectedImage.clone()));
			}

			if (selectedImage.getImageOrientation() == Orientation.ANT) {
				imagePourTrie.add(selectedImage.clone());
			} else {
				throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
						selectedImage.getImageOrientation(),
						new Orientation[] { Orientation.DYNAMIC_ANT, Orientation.ANT });
			}
			selectedImage.getImagePlus().close();

		}

		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		imagePourTrie.sort(chronologicalOrder);

		this.sauvegardeImagesSelectDicom = imagePourTrie.toArray(new ImageSelection[0]);

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(sauvegardeImagesSelectDicom[0]);
		return selection;
	}

	
	/** 
	 * @return String
	 */
	@Override
	public String instructions() {
		return "2 images in Ant or DynamicAnt orientation";
	}

	private class RadioGroup implements ItemListener {

		private final Set<CheckboxMenuItem> items;

		public RadioGroup() {
			this.items = new HashSet<>();
		}

		public void addRadioItem(CheckboxMenuItem item) {
			this.items.add(item);
			item.addItemListener(this);
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// Uncheck all
				this.items.forEach(i -> i.setState(false));
			}
			// Activate only source
			((CheckboxMenuItem) e.getSource()).setState(true);
		}
	}
	/*public MIBGScintigraphy() {
		super(STUDY_NAME);
	}

	private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Carrio JACC 2010",
																   "https://www.ncbi.nlm.nih.gov/pubmed/20129538"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
	}

	@Override
	public void start(List<ImageSelection> preparedImages) {
		this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
		this.getFenApplication().setController(
				new ControllerWorkflowMIBG(STUDY_NAME, (FenApplicationWorkflow) this.getFenApplication(),
										   preparedImages.toArray(new ImageSelection[0])));
		this.createDocumentation();

	}

	@Override
	public Column[] getColumns() {
		// Orientation column
		String[] orientationValues =
				{Orientation.ANT_POST.toString(), Orientation.POST_ANT.toString(), Orientation.ANT.toString()};
		Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

		// Choose columns to display
		return new Column[]{Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
							Column.STACK_SIZE, orientation};
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> openedImages) throws WrongInputException {
		// Check number
		if (openedImages.size() != 2) throw new WrongNumberImagesException(openedImages.size(), 2);

		List<ImageSelection> impSelect = new ArrayList<>();
		for (ImageSelection openedImage : openedImages) {
			if (openedImage.getImageOrientation() == Orientation.ANT_POST ||
					openedImage.getImageOrientation() == Orientation.POST_ANT) {
				impSelect.add(Library_Dicom.ensureAntPostFlipped(openedImage));
			} else if (openedImage.getImageOrientation() == Orientation.ANT) {
				impSelect.add(openedImage);
			} else {
				throw new WrongColumnException.OrientationColumn(openedImage.getRow(),
																 openedImage.getImageOrientation(),
																 new Orientation[]{Orientation.ANT_POST,
																				   Orientation.POST_ANT,
																				   Orientation.ANT});
			}
		}

		// Order images by time
		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		impSelect.sort(chronologicalOrder);

		// Close images
		for (ImageSelection ims : openedImages)
			ims.close();

		return impSelect;
	}

	@Override
	public String instructions() {
		return "2 images. Ant-Post / Post-Ant or Ant orientations accepted.";
	}*/
}
