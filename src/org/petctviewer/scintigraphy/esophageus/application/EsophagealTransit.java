package org.petctviewer.scintigraphy.esophageus.application;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.Orientation;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.exceptions.WrongInputException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.ChronologicalAcquisitionComparator;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

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

	// possible de refactorier le trie des images....
	@Override
	public ImageSelection[] preparerImp(ImageSelection[] selectedImages) throws WrongInputException {
		// entrée : tableau de toutes les images passées envoyé par la selecteur de
		// dicom

		// sauvegarde des images pour le modele
		sauvegardeImagesSelectDicom = new ImageSelection[2][selectedImages.length];

		// oblige de faire duplicate sinon probleme

		// trier les images par date et que avec les ant
		// on creer une liste avec toutes les images plus
		List<ImageSelection> imagePourTrieAnt = new ArrayList<>();

		// la meme chose pour la ant
		List<ImageSelection> imagePourTriePost = new ArrayList<>();

		// poour chaque acquisition
		for (int i = 0; i < selectedImages.length; i++) {
			if (selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT_POST || selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_POST_ANT) {
				// on ne sauvegarde que la ant
				// null == pas d'image ant et/ou une image post et != une image post en [0]
				ImageSelection[] splited = Library_Dicom.splitDynamicAntPost(selectedImages[i]);
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
			}else if(selectedImages[i].getImageOrientation() == Orientation.DYNAMIC_ANT)
				imagePourTrieAnt.add(selectedImages[i].clone());
			else
				throw new WrongInputException(
						"Unexpected Image type.\n Accepted : DYNAMIC_ANT | DYNAMIC_ANT_POST | DYNAMIC_POST_ANT");
			selectedImages[i].getImagePlus().close();

		}

		// on appelle la fonction de trie
		ChronologicalAcquisitionComparator chronologicalOrder = new ChronologicalAcquisitionComparator();
		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on
		// stock le tout dans le tableau en [0]
		Collections.sort(imagePourTrieAnt, chronologicalOrder);
		sauvegardeImagesSelectDicom[0] = imagePourTrieAnt.toArray(new ImageSelection[imagePourTrieAnt.size()]);
		// Pareil pour la post
		Collections.sort(imagePourTriePost, chronologicalOrder);
		sauvegardeImagesSelectDicom[1] = imagePourTriePost.toArray(new ImageSelection[imagePourTriePost.size()]);

		// test de verification de la taille des stack
		if (sauvegardeImagesSelectDicom[0].length != sauvegardeImagesSelectDicom[1].length) {
			System.err.println(
					"(EsophagealTransit) Le nombre de slice ant est différent du nombre de slice post -> seules les ant seront pris en comptes");
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
		ImageSelection[] selection = new ImageSelection[1];
//		ImageSelection imsProjeteAllAcqui = sauvegardeImagesSelectDicom[0][0].clone();
//		imsProjeteAllAcqui.setImagePlus(impProjeteAllAcqui);
//		selection[0] = imsProjeteAllAcqui;
		selection[0] = sauvegardeImagesSelectDicom[0][0];
		return selection;
	}

	@Override
	public void lancerProgramme(ImageSelection[] selectedImages) {
		// phase 1
		Overlay overlay = Library_Gui.initOverlay(selectedImages[0].getImagePlus(), 12);
		Library_Gui.setOverlayDG(selectedImages[0].getImagePlus(), Color.yellow);

		FenApplication fen = new FenApplication(selectedImages[0].getImagePlus(), "Oesophageus");
		fen.getPanel_btns_gauche().remove(fen.getBtn_drawROI());
		fen.getPanel_Instructions_btns_droite().removeAll();

		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(nbAcquisition, 1));

		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton[] radioButton = new JRadioButton[nbAcquisition];
		for (int i = 0; i < nbAcquisition; i++) {
			int num = i;
			radioButton[i] = new JRadioButton("Acquisition " + (i + 1));
			radioButton[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					fen.setImage(sauvegardeImagesSelectDicom[0][num].getImagePlus());
				}
			});
			buttonGroup.add(radioButton[i]);
			radioButtonPanel.add(radioButton[i]);
			radioButton[i].setSelected(false);
		}
		radioButton[0].setSelected(true);

		JPanel radioButtonPanelFlow = new JPanel();
		radioButtonPanelFlow.setLayout(new FlowLayout());
		radioButtonPanelFlow.add(radioButtonPanel);

		JButton startQuantificationButton = new JButton("Start Quantification");
		startQuantificationButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
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

//				Controleur_EsophagealTransit cet = new Controleur_EsophagealTransit(EsophagealTransit.this,
//						sauvegardeImagesSelectDicom, "Esophageal Transit");
				ControllerWorkflowEsophagealTransit cet = new ControllerWorkflowEsophagealTransit(EsophagealTransit.this, EsophagealTransit.this.getFenApplication(), new Modele_EsophagealTransit(sauvegardeImagesSelectDicom, "Esophageal Transit", EsophagealTransit.this));
				EsophagealTransit.this.getFenApplication().setControleur(cet);

			}
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

}
