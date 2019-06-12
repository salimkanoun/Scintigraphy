package org.petctviewer.scintigraphy.esophageus.application;

import ij.IJ;
import ij.ImagePlus;
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
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EsophagealTransit extends Scintigraphy {
	/*
	 * pour cette application on aura 2 phases: phase 1 : affichage de chaque stack
	 * pour chaque acquisition, avec la possiblité de changer d'acqui. On aura un
	 * selecteur d'acquisiton pour pouvoir changer le stack (acqui) affiché A
	 * l'appuie sur "start exam", on lance la phase 2
	 * 
	 * Phase 2 : affichage du projet de chaque acquistion dans 1 stack avec 1
	 * acquisition par slice vec selection de la roi pour chaque acqui
	 */

	private int[] frameDurations;

	// [0: ant | 1: post][numAcquisition]
	private ImageSelection[][] sauvegardeImagesSelectDicom;

	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

	private int nbAcquisition;

	public EsophagealTransit() {
		super("Esophageal Transit");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void start(ImageSelection[] selectedImages) {
		// phase 1
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.yellow);

		FenApplicationWorkflow fen = new FenApplicationWorkflow(selectedImages[0], "Oesophageus");
		fen.setVisualizationEnable(false);
		fen.getPanel_btns_gauche().remove(fen.getBtn_drawROI());
		fen.getPanel_Instructions_btns_droite().removeAll();

		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton[] radioButton = new JRadioButton[nbAcquisition];
		for (int i = 0; i < nbAcquisition; i++) {
			int num = i;
			radioButton[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButton[i].addItemListener(e -> fen.setImage(sauvegardeImagesSelectDicom[0][num]));
			buttonGroup.add(radioButton[i]);
			radioButtonPanel.add(radioButton[i]);
			radioButton[i].setSelected(false);
		}
		radioButton[0].setSelected(true);

		JPanel radioButtonPanelFlow = new JPanel();
		radioButtonPanelFlow.setLayout(new FlowLayout());
		radioButtonPanelFlow.add(radioButtonPanel);

		JButton startQuantificationButton = new JButton("Start Quantification");
		startQuantificationButton.addActionListener(e -> {
			fen.setVisualizationEnable(true);
			// passage a la phase 2
			fen.getPanel_btns_gauche().add(fen.getBtn_drawROI());
			fen.getPanelPrincipal().remove(startQuantificationButton);
			fen.getPanelPrincipal().remove(radioButtonPanelFlow);
			fen.getPanel_Instructions_btns_droite().add(fen.getTextfield_instructions());
			fen.getPanel_Instructions_btns_droite().add(fen.createPanelInstructionsBtns());
			fen.revalidate();

			fen.setImage(impProjeteAllAcqui);
			fen.getImagePlus().setSlice(1);
			fen.updateSliceSelector();
			IJ.setTool(Toolbar.RECTANGLE);

			ControllerWorkflowEsophagealTransit cet = new ControllerWorkflowEsophagealTransit(EsophagealTransit.this,
					(FenApplicationWorkflow) EsophagealTransit.this.getFenApplication(), new Model_EsophagealTransit(
							sauvegardeImagesSelectDicom, "Esophageal Transit", EsophagealTransit.this));
			EsophagealTransit.this.getFenApplication().setController(cet);

		});

		fen.getPanelPrincipal().add(radioButtonPanelFlow);
		fen.getPanelPrincipal().add(startQuantificationButton);
		this.setFenApplication(fen);
		selectedImages[0].getImagePlus().setOverlay(overlay);

		this.getFenApplication().setVisible(true);

		fen.resizeCanvas();
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

	public ImageSelection getImgPrjtAllAcqui() {
		ImageSelection returned = sauvegardeImagesSelectDicom[0][0].clone(Orientation.ANT);
		returned.setImagePlus(impProjeteAllAcqui);
		return returned;
	}

	@Override
	public FenSelectionDicom.Column[] getColumns() {
		return FenSelectionDicom.Column.getDefaultColumns();
	}

	@Override
	public List<ImageSelection> prepareImages(List<ImageSelection> selectedImages) throws WrongInputException,
			ReadTagException {
		// Check number
		if (selectedImages.size() == 0)
			throw new WrongNumberImagesException(selectedImages.size(), 1, Integer.MAX_VALUE);

		// entrée : tableau de toutes les images passées envoyé par la selecteur de
		// dicom

		// sauvegarde des images pour le modele
		sauvegardeImagesSelectDicom = new ImageSelection[2][selectedImages.size()];

		// oblige de faire duplicate sinon probleme

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
		List<ImageSelection> imagePourTrieAnt = new ArrayList<>();

		// la meme chose pour la ant
		List<ImageSelection> imagePourTriePost = new ArrayList<>();

		// poour chaque acquisition
		for (ImageSelection selectedImage : selectedImages) {
			if (selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT_POST
					|| selectedImage.getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
				// on ne sauvegarde que la ant
				// null == pas d'image ant et/ou une image post et != une image post en [0]
				ImageSelection[] splited = Library_Dicom.splitDynamicAntPost(selectedImage);
				if (splited[0] != null) {
					imagePourTrieAnt.add(splited[0]);
				}
				// [1] : c'est la post
				// si null : pas dimage post
				if (splited[1] != null) {
					// trie + inversement de la post
					ImageSelection ims = splited[1];
					Library_Dicom.flipStackHorizontal(ims);
					imagePourTriePost.add(ims);
				}
			} else if (selectedImage.getImageOrientation() == Orientation.DYNAMIC_ANT)
				imagePourTrieAnt.add(selectedImage.clone());
			else
				throw new WrongColumnException.OrientationColumn(selectedImage.getRow(),
																 selectedImage.getImageOrientation(), new Orientation[] { Orientation.DYNAMIC_ANT,
																														  Orientation.DYNAMIC_ANT_POST, Orientation.DYNAMIC_POST_ANT });
			selectedImage.getImagePlus().close();

		}

		// on appelle la fonction de trie
		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on
		// stock le tout dans le tableau en [0]
		imagePourTrieAnt.sort(chronologicalOrder);
		sauvegardeImagesSelectDicom[0] = imagePourTrieAnt.toArray(new ImageSelection[0]);
		// Pareil pour la post
		imagePourTriePost.sort(chronologicalOrder);
		sauvegardeImagesSelectDicom[1] = imagePourTriePost.toArray(new ImageSelection[0]);

		// test de verification de la taille des stack
		if (sauvegardeImagesSelectDicom[0].length != sauvegardeImagesSelectDicom[1].length) {
			System.err.println(
					"(EsophagealTransit) The number of ANT slices is different from the number of POST slices -> only the ANTs will be taken into account");
			sauvegardeImagesSelectDicom[1] = new ImageSelection[0];
		}

		nbAcquisition = sauvegardeImagesSelectDicom[0].length;

		// preparetion de l'image plus la 2eme phase
		// image plus du projet de chaque acquisiton avec sur chaque slice une
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
			impProjeteAllAcqui.setProperty("Info", sauvegardeImagesSelectDicom[0][0].getImagePlus().getInfoProperty());
		}

		// phase 1
		// on retourne la stack de la 1ere acquisition
		List<ImageSelection> selection = new ArrayList<>();
		selection.add(sauvegardeImagesSelectDicom[0][0]);
		return selection;
	}

	@Override
	public String instructions() {
		return "Minimum 1 image.";
	}
}
