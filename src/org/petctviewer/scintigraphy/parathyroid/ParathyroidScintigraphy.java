package org.petctviewer.scintigraphy.parathyroid;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif.Isotope;
import org.petctviewer.scintigraphy.shunpo.ControllerWorkflowShunpo.DisplayState;

import ij.ImagePlus;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;

public class ParathyroidScintigraphy extends Scintigraphy {

    public static final String STUDY_NAME = "Parathyroid";
	private static final String ORGAN_THYROID = "THYROID", ORGAN_PARATHYROID = "THYROID&PARA";
	private static final String NONE = "NONE";
    private Column organColumn;
    private Column traceurColumn;

    // [0: ant | 1: post][numAcquisition]
	private ImageSelection[] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

	private ImagePlus imgOptioToDisplay;
	
	Isotope isotope_thy;
	Isotope isotope_parathy;


    public ParathyroidScintigraphy() {
        super(STUDY_NAME);
	}
	
	public void setIsotopeThy(Isotope isotope){
		this.isotope_thy = isotope;
	}

	public void setIsotopeParathy(Isotope isotope){
		this.isotope_parathy = isotope;
	}

	public ImagePlus getImgToDisplay() {
		return this.imgOptioToDisplay;
	}

    private void createDocumentation() {
        DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
        doc.setDeveloper("Angele Mateos");
        doc.addReference(DocumentationDialog.Field.createTextField("With Kidney",
                "VILLANEUEVA-MEYER Clinical " + "Nuclear Medecine 1986"));
        doc.addReference(
                DocumentationDialog.Field.createLinkField("Brain alone", "KROWKA Chest 2000", "http://google" + ".fr"));
        doc.setYoutube("");
        doc.setOnlineDoc("");
        this.getFenApplication().setDocumentation(doc);
    }

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
	
	public ImageSelection getImgPrjtAllAcqui() {
		ImageSelection returned = this.sauvegardeImagesSelectDicom[0];
		returned.setImagePlus(impProjeteAllAcqui);
		return returned;
	}

    @Override
    public void start(List<ImageSelection> preparedImages) {
        // Start program
        this.setFenApplication(new FenApplicationWorkflow(preparedImages.get(0), this.getStudyName()));
        this.getFenApplication().setController(new ControllerWorkflowParathyroid(
				(FenApplicationWorkflow) getFenApplication(), new ModelParathyroid(this.sauvegardeImagesSelectDicom, STUDY_NAME, this.imgOptioToDisplay)));

        this.createDocumentation();
        this.inflateMenuBar((ControllerWorkflowParathyroid) this.getFenApplication().getController());

        this.getFenApplication().setVisible(true);
    }

    @Override
    public Column[] getColumns() {
        // Orientation column
        String[] orientationValues = { Orientation.ANT.toString(), Orientation.DYNAMIC_ANT.toString(), Orientation.UNKNOWN.toString()};
        Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

        // Organ column
        String[] organValues = { ORGAN_THYROID, ORGAN_PARATHYROID, NONE };
        this.organColumn = new Column("Organ", organValues);

		// Traceur column
		String[] isotopeValues = {Isotope.IODE_123.toString(), Isotope.TECHNETIUM_99.toString()};
		this.traceurColumn = new Column(Column.ISOTOPE.getName(), isotopeValues);

        // Choose columns to display
        return new Column[] { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
        Column.STACK_SIZE, orientation, this.organColumn, this.traceurColumn};
	}
	

    @Override
    public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
            throws WrongInputException, ReadTagException {
		// Check that number of images is correct
		if (selectedImages.size()<2 || selectedImages.size()>3) throw new WrongNumberImagesException(selectedImages.size(), 2, 3);

		// sauvegarde des images pour le modele
		this.sauvegardeImagesSelectDicom = new ImageSelection[selectedImages.size()];

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
		List<ImageSelection> imagePourTrieAnt = new ArrayList<>();


		// pour chaque acquisition
		for (ImageSelection selectedImage : selectedImages) {
			if (selectedImage.getImageOrientation() != Orientation.UNKNOWN){
				this.imgOptioToDisplay = null;
				if (selectedImage.getImageOrientation() == Orientation.ANT || selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT) {
					if (selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT) {
						ImageSelection img = selectedImage;
						img = Library_Dicom.project(img, 0, img.getImagePlus().getNSlices(), "sum");
						selectedImage = img;
					}
	
					if (selectedImage.getImageIsotope() == Isotope.IODE_123 || selectedImage.getImageIsotope() == Isotope.TECHNETIUM_99) {
						imagePourTrieAnt.add(selectedImage.clone());
					}
					else {
						throw new WrongIsotopeException.IsotopeColumn(selectedImage.getRow(),
								selectedImage.getImageIsotope(), new Isotope[] { Isotope.IODE_123, Isotope.TECHNETIUM_99 });
					}
				}
				else {
					throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
							selectedImage.getImageOrientation(), new Orientation[] { Orientation.DYNAMIC_ANT,
									Orientation.ANT });
				}
				selectedImage.getImagePlus().close();
			
			} else {
				this.imgOptioToDisplay = selectedImage.getImagePlus();
			}
		}

		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on vérifie si elles peuvent être triées
		// par traceur (isotope), puis on stocke le tout dans le tableau en [0]
		
        if (imagePourTrieAnt.get(1).getImageIsotope() == Isotope.IODE_123) {
			Collections.reverse(imagePourTrieAnt);
		}
		if (imagePourTrieAnt.get(0).getImageIsotope() == Isotope.TECHNETIUM_99 && 
			imagePourTrieAnt.get(1).getImageIsotope() == Isotope.TECHNETIUM_99) {
			// on appelle la fonction de tri
			ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
			imagePourTrieAnt.sort(chronologicalOrder);
        }
		

		this.sauvegardeImagesSelectDicom = imagePourTrieAnt.toArray(new ImageSelection[0]);
		

		List<ImageSelection> selection = new ArrayList<>();
		selection.add(sauvegardeImagesSelectDicom[0]);
		return selection;
	}

	@Override
	public String instructions() {
		return "2 images in Ant orientation";
	}
 
	private class RadioGroup implements ItemListener {

		private Set<CheckboxMenuItem> items;

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
}