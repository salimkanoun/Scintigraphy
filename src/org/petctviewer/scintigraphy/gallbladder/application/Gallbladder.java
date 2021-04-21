package org.petctviewer.scintigraphy.gallbladder.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import java.awt.*;

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
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.gui.Overlay;
import ij.ImagePlus;

/*
l'objectif est de tracer chaque point pour chaque slice en délimitant son nombre de coups (gallblader - liver)
divisé par le temps d'acquisition de la slice
*/

public class Gallbladder extends Scintigraphy{

    /**
     * Phase 1 : affichage de chaque stack
     * pour chaque acquisition, avec la possibilité" de changer d'acqui. On aura un sélecteur d'acquisition
     * pour pouvoir changer le stack (acqui) affiché. A l'appuie sur "start exam", on lance la phase 2
     * Phase 2 : affichage du projet de chaque acquisition dans 1 stack avec 1 acquisition par slice vec selection
     * de la roi pour chaque acqui
     */

     private int[] frameDurations;

    // [0: ant | 1: post][numAcquisition]
	private ImageSelection[][] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

    private int nbAcquisition;
    
    public Gallbladder(){
        super("Gallbladder");
    }


    private void createDocumentation() {
		DocumentationDialog doc = new DocumentationDialog(this.getFenApplication());
		doc.addReference(DocumentationDialog.Field.createLinkField("", "Gallblader explanation",
				"https://www.google.com/url?sa=i&url=https%3A%2F%2Fcentre.chl.lu%2Ffr%2Fdossier%2Fablation-de-la-v%25C3%25A9sicule-biliaire-chol%25C3%25A9cystectomie-au-centre-hospitalier-de-luxembourg&psig=AOvVaw3VFTeHaZ9gZZuQjF_4J5ri&ust=1590496020646000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCOCp3sWBz-kCFQAAAAAdAAAAABAD"));
		doc.setYoutube("");
		doc.setOnlineDoc("");
		this.getFenApplication().setDocumentation(doc);
    }
    
    @Override
	public void start(List<ImageSelection> preparedImages) {

        //SK A EVALUER LES IMAGES NE SEMBLENT PLUS AVOIR D OVERLAY INITIALISE PAR DEFAUT, PEUT ETRE A ETENDRE DANS TOUS LES PROGRAMMES
        initOverlayOnPreparedImages(preparedImages);
        
		//phase 1
        Overlay overlay = Library_Gui.initOverlay(preparedImages.get(0).getImagePlus(), 12);
        Library_Gui.setOverlayDG(preparedImages.get(0).getImagePlus(), Color.yellow);

        FenApplicationWorkflow fen = new FenApplicationWorkflow(preparedImages.get(0), "Gallblader");
        fen.setVisualizationEnable(false);

        JPanel radioButtonPanelFlow = new JPanel();
        radioButtonPanelFlow.setLayout(new FlowLayout());
        //radioButtonPanelFlow.add(radioButtonPanel);
        
        fen.getPanelPrincipal().add(radioButtonPanelFlow);
        this.setFenApplication(fen);
        preparedImages.get(0).getImagePlus().setOverlay(overlay);

        this.getFenApplication().setVisible(true);
        this.createDocumentation();
        fen.resizeCanvas();

        ControllerWorkflowGallbladder cg = new ControllerWorkflowGallbladder(
            (FenApplicationWorkflow) Gallbladder.this.getFenApplication(), new Model_Gallbladder(
                sauvegardeImagesSelectDicom, "Gallbladder", Gallbladder.this,
                this.getImgPrjtAllAcqui()));
        this.getFenApplication().setController(cg);

        this.createDocumentation();

        this.getFenApplication().setVisible(true);
    }
    
    public int[] getFrameDurations() {
		return this.frameDurations;
    }
    
    public ImageSelection getImgPrjtAllAcqui() {
		ImageSelection returned = sauvegardeImagesSelectDicom[0][0].clone(Orientation.ANT);
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
        if(selectedImages.size() == 0)
                throw new WrongNumberImagesException(selectedImages.size(), 1, Integer.MAX_VALUE);
        // en entrée : tableau de toutes les images passées envoyé par le selecteur de dicom
        
        //sauvegarde des images pour le modèle
        this.sauvegardeImagesSelectDicom = new ImageSelection[2][selectedImages.size()];

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
        List<ImageSelection> imagePourTrieAnt = new ArrayList<>();
        
        //pour chaque acquisition
        for(ImageSelection selectedImage : selectedImages){
            if(selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT_POST
            || selectedImage.getImageOrientation() == Orientation.DYNAMIC_POST_ANT){
                //on ne sauvegarde que la ant
                //null = pas d'image ant et/ou une image post et != une image post en [0]
                ImageSelection[] splited = Library_Dicom.splitDynamicAntPost(selectedImage);
                if(splited[0] != null){
                    imagePourTrieAnt.add(splited[0]);
                }
                //on ne gère pas le post donc pas de splited[1]
            }else if (selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT)
                imagePourTrieAnt.add(selectedImage.clone());
            else
                throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
                selectedImage.getImageOrientation(), new Orientation[] {Orientation.DYNAMIC_ANT,
                Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT});
            selectedImage.getImagePlus().close();
        }
        // on appelle la fonction de tri
		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on
		// stocke le tout dans le tableau en [0]
		imagePourTrieAnt.sort(chronologicalOrder);
        sauvegardeImagesSelectDicom[0] = imagePourTrieAnt.toArray(new ImageSelection[0]);
        
        this.nbAcquisition = sauvegardeImagesSelectDicom[0].length;

        //on prépare l'imagePlus de la 2ème phase
        //imagePlus du projet de chaque acquisition avec sur chaque slice une acquisition
        impProjeteAllAcqui = null;
        if(imagePourTrieAnt.size() > 0){
            ImageSelection[] imagesAnt = new ImageSelection[imagePourTrieAnt.size()];
            for(int i = 0; i < imagePourTrieAnt.size(); i++){
                imagesAnt[i] = Library_Dicom.project(imagePourTrieAnt.get(i), 0,
                imagePourTrieAnt.get(i).getImagePlus().getStackSize(), "max");
            }
            // renvoi un stack trié des projection des images
			// orderby ... renvoi un tableau d'imp trie par ordre chrono, avec en paramètre
			// la liste des imp Ant
			// captureTo.. renvoi un stack avec sur chaque slice une imp du tableau passé en
            // param ( un image trié, projeté et ant)
            Arrays.parallelSort(imagesAnt, chronologicalOrder);
            ImagePlus[] impsAnt = new ImagePlus[imagesAnt.length];
            for(int i = 0; i < imagesAnt.length; i++){
                impsAnt[i] = imagesAnt[i].getImagePlus();
            }
            impProjeteAllAcqui = new ImagePlus("GallbladderStack", Library_Capture_CSV.captureToStack(impsAnt));
            impProjeteAllAcqui.setProperty("Info", sauvegardeImagesSelectDicom[0][0].getImagePlus().getInfoProperty());
        }
        //phase 1
        //on retourne la stack de la 1ere acquisition
        List<ImageSelection> selection = new ArrayList<>();
        selection.add(sauvegardeImagesSelectDicom[0][0]);

        //Build frame duration
        this.frameDurations = Library_Dicom.buildFrameDurations(this.impProjeteAllAcqui);
        return selection;
     }

	@Override
	public String instructions() {
		return "Minimum 1 image.";
	}
}