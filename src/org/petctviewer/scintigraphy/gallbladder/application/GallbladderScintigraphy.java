package org.petctviewer.scintigraphy.gallbladder.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.ReadTagException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongColumnException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.exceptions.WrongNumberImagesException;
import org.petctviewer.scintigraphy.scin.gui.DocumentationDialog;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom;
import org.petctviewer.scintigraphy.scin.gui.FenSelectionDicom.Column;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;

import ij.ImagePlus;

/*
l'objectif est de tracer chaque point pour chaque slice en délimitant son nombre de coups (gallblader - liver)
divisé par le temps d'acquisition de la slice
*/

public class GallbladderScintigraphy extends Scintigraphy{

    /**
     * Phase 1 : affichage de chaque stack
     * pour chaque acquisition, avec la possibilité" de changer d'acqui. On aura un sélecteur d'acquisition
     * pour pouvoir changer le stack (acqui) affiché. A l'appuie sur "start exam", on lance la phase 2
     * Phase 2 : affichage du projet de chaque acquisition dans 1 stack avec 1 acquisition par slice vec selection
     * de la roi pour chaque acqui
     */

    public static final String STUDY_NAME = "GallBladder Ejection Fraction";

    private int[] frameDurations;

    // [0: ant | 1: post][numAcquisition]
	private ImageSelection[] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

    public GallbladderScintigraphy(){
        super(STUDY_NAME);
    }


    private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Gallbladder explanation (FR)",
				"https://centre.chl.lu/fr/dossier/ablation-de-la-v%C3%A9sicule-biliaire-chol%C3%A9cystectomie-au-centre-hospitalier-de-luxembourg"));
		doc.addReference(DocumentationDialog.Field.createLinkField("",
                "Tulchinsky - J Nucl Med Technol. 2010", "https://pubmed.ncbi.nlm.nih.gov/21078782/"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
    }
    
    @Override
	public void start(List<ImageSelection> preparedImages) {
        
		//phase 1
        this.initOverlayOnPreparedImages(preparedImages, 12);
        this.setFenApplication(new FenApplicationGallbladder(preparedImages.get(0), this.getStudyName(), this));
        this.getFenApplication().setController(
                new ControllerWorkflowGallbladder((FenApplicationWorkflow) this.getFenApplication(),
                        new ModelGallbladder(preparedImages.toArray(new ImageSelection[0]), STUDY_NAME, this.frameDurations)));
        this.createDocumentation();
    }

    public int[] getFrameDurations() {
		return this.frameDurations;
    }
    
    public ImageSelection getImgPrjtAllAcqui() {
		ImageSelection returned = sauvegardeImagesSelectDicom[0].clone(Orientation.ANT);
		returned.setImagePlus(impProjeteAllAcqui);
		return returned;
	}
    
	@Override
	public Column[] getColumns() {
        return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages)
			throws WrongInputException, ReadTagException {
        // Check number
        if(selectedImages.size() != 1)
                throw new WrongNumberImagesException(selectedImages.size(), 1);
        // en entrée : tableau de toutes les images passées envoyé par le selecteur de dicom
        
        //sauvegarde des images pour le modèle
        this.sauvegardeImagesSelectDicom = new ImageSelection[selectedImages.size()];

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
        List<ImageSelection> imagePourTrieAnt = new ArrayList<>();

        //check de l'orientation
        List<ImageSelection> selection = new ArrayList<>();
        Orientation[] acceptedOrientations = new Orientation[]{Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT};
        String hint = "You can use only 1 dynamic (Ant_Post or Post_Ant)";


        if (Arrays.stream(selectedImages.toArray(new ImageSelection[0])).noneMatch(o ->
                o.getImageOrientation() == Orientation.DYNAMIC_ANT_POST))
            throw new WrongColumnException.OrientationColumn(selectedImages.get(0).getRow(),
                    selectedImages.get(0).getImageOrientation(),
                    acceptedOrientations, hint);

        // Set images
        ImageSelection imps = selectedImages.get(0).clone();

        //pour chaque acquisition
        if(imps.getImageOrientation() == Orientation.DYNAMIC_ANT_POST
        || imps.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
            //on ne sauvegarde que la ant
            //null = pas d'image ant et/ou une image post et != une image post en [0]
            ImageSelection[] splited = Library_Dicom.splitDynamicAntPost(imps);
            if(splited[0] != null){
                imps = splited[0];
            }
            //on ne gère pas le post donc pas de splited[1]
        } else
            throw new WrongColumnException.OrientationColumn(imps.getRow(),
            imps.getImageOrientation(), new Orientation[] {Orientation.DYNAMIC_ANT,
            Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT});

        Library_Dicom.normalizeToCountPerSecond(imps);
        this.frameDurations = Library_Dicom.buildFrameDurations(imps.getImagePlus());
        selection.add(imps);

        // Close images
        selectedImages.forEach(ImageSelection::close);
        return selection;
     }

	@Override
	public String instructions() {
		return "Minimum 1 image.";
	}
}