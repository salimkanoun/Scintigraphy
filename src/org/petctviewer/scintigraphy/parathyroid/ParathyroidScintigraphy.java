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
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
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
    private Column organColumn;
    private Column traceurColumn;

    // [0: ant | 1: post][numAcquisition]
	private ImageSelection[] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
    private ImagePlus impProjeteAllAcqui;


    public ParathyroidScintigraphy() {
        super(STUDY_NAME);
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
				(FenApplicationWorkflow) getFenApplication(), new ModelParathyroid(this.sauvegardeImagesSelectDicom, STUDY_NAME)));

        this.createDocumentation();
        this.inflateMenuBar((ControllerWorkflowParathyroid) this.getFenApplication().getController());

        this.getFenApplication().setVisible(true);
    }

    @Override
    public Column[] getColumns() {
        // Orientation column
        String[] orientationValues = { Orientation.ANT.toString(), Orientation.DYNAMIC_ANT.toString()};
        Column orientation = new Column(Column.ORIENTATION.getName(), orientationValues);

        // Organ column
        String[] organValues = { ORGAN_THYROID, ORGAN_PARATHYROID };
        this.organColumn = new Column("Organ", organValues);

        // Traceur column
        String[] isotopeValues = {Isotope.IODE_123.toString(), Isotope.TECHNETIUM_99.toString()};
        this.traceurColumn = new Column("Isotope", isotopeValues);

        // Choose columns to display
        return new Column[] { Column.PATIENT, Column.STUDY, Column.DATE, Column.SERIES, Column.DIMENSIONS,
        Column.STACK_SIZE, orientation, this.organColumn, this.traceurColumn};
    }

    @Override
    public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
            throws WrongInputException, ReadTagException {
		// Check that number of images is correct
		if (selectedImages.size() != 2) throw new WrongNumberImagesException(selectedImages.size(), 2);

		// entrée : tableau de toutes les images passées envoyé par la selecteur de
		// dicom

		// sauvegarde des images pour le modele
		this.sauvegardeImagesSelectDicom = new ImageSelection[selectedImages.size()];

		// oblige de faire duplicate sinon probleme

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
		List<ImageSelection> imagePourTrieAnt = new ArrayList<>();


		// pour chaque acquisition
		for (ImageSelection selectedImage : selectedImages) {
			if (selectedImage.getImageOrientation() == Orientation.ANT || selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT){
                imagePourTrieAnt.add(selectedImage.clone());
            }
			else {
				throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
						selectedImage.getImageOrientation(), new Orientation[] { Orientation.DYNAMIC_ANT,
                                Orientation.ANT });
            }
			selectedImage.getImagePlus().close();

		}

		// on appelle la fonction de tri
		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on
		// stocke le tout dans le tableau en [0]
		imagePourTrieAnt.sort(chronologicalOrder);
		sauvegardeImagesSelectDicom = imagePourTrieAnt.toArray(new ImageSelection[0]);
		

		//this.nbAcquisition = sauvegardeImagesSelectDicom.length;

		// preparation de l'image plus la 2eme phase
		// image plus du projet de chaque acquisition avec sur chaque slice une
		// acquistion
		impProjeteAllAcqui = null;
		if (imagePourTrieAnt.size() > 0) {
			ImageSelection[] imagesAnt = new ImageSelection[imagePourTrieAnt.size()];
			for (int i = 0; i < imagePourTrieAnt.size(); i++) {
				// null == pas d'image ant et/ou une image post et != une image post en [0]
				imagesAnt[i] = Library_Dicom.project(imagePourTrieAnt.get(i), 0,
						imagePourTrieAnt.get(i).getImagePlus().getStackSize(), "max");
			}
			// renvoi un stack trié des projection des images
			// orderby ... renvoi un tableau d'imp trie par ordre chrono, avec en paramètre
			// la liste des imp Ant
			// captureTo.. renvoi un stack avec sur chaque slice une imp du tableau passé en
			// param ( un image trié, projeté et ant)
			// ImagePlus[] tabProj = Scintigraphy.orderImagesByAcquisitionTime(imagesAnt);
			Arrays.parallelSort(imagesAnt, chronologicalOrder);
			ImagePlus[] impsAnt = new ImagePlus[imagesAnt.length];
			for (int i = 0; i < imagesAnt.length; i++)
				impsAnt[i] = imagesAnt[i].getImagePlus();
			impProjeteAllAcqui = new ImagePlus("EsoStack", Library_Capture_CSV.captureToStack(impsAnt));
			// SK VOIR METHODE POUR GARDER LES METADATA ORIGINALE DANS LE STACK GENEREs
			impProjeteAllAcqui.setProperty("Info", sauvegardeImagesSelectDicom[0].getImagePlus().getInfoProperty());
		}

		// phase 1
		// on retourne la stack de la 1ere acquisition
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