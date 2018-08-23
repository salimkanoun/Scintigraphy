package org.petctviewer.scintigraphy.esophageus.application;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.StaticMethod;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Toolbar;

public class FenApplication_EsophagealTransit extends Scintigraphy {
	/*
	 * pour cette application on aura 2 phases:
	 * phase 1 : affichage de chaque stack pour chaque acquisition, avec la possiblité de changer d'acqui.
	 * On aura un selecteur d'acquisiton pour pouvoir changer le stack (acqui) affiché
	 * A l'appuie sur "start exam", on lance la phase 2
	 * 
	 * Phase 2 : affichage du projet de chaque acquistion dans 1 stack avec 1 acquisition par slice
	 * 	vec selection de la roi pour chaque acqui
	 */
	
	
	private int[] frameDurations;
	
	//[0: ant | 1: post][numAcquisition]
	private ImagePlus[][] sauvegardeImagesSelectDicom;
	
	// imp du projet de chaque Acqui
	private ImagePlus impProjeteAllAcqui;

	private int nbAcquisition;

	private int numAcquisiton;

	
	public FenApplication_EsophagealTransit() {
		super("Esophageal Transit");
	}

	//possible de refactorier le trie des images....
	@Override
	protected ImagePlus preparerImp(ImagePlus[] imagesSelectDicom) {
		//entrée : tableau de toutes les images passées envoyé par la selecteur de dicom

		//sauvegarde des images pour le modele
		sauvegardeImagesSelectDicom = new  ImagePlus[2][imagesSelectDicom.length];

		// oblige de faire duplicate sinon probleme 
		
		// trier les images par date et que avec les ant
		//on creer une liste avec toutes les images plus 
		ArrayList<ImagePlus> imagePourTrieAnt = new ArrayList<>();
		
		// la meme chose pour la ant
		ArrayList<ImagePlus> imagePourTriePost = new ArrayList<>();

		//poour chaque acquisition
		for(int i =0; i< imagesSelectDicom.length; i++){
			//on ne sauvegarde que la ant
			//null == pas d'image ant et/ou une image post et != une image post en [0]
			if(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[0] != null) {
				imagePourTrieAnt.add(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[0].duplicate());
			}
			// [1] : c'est la post
			// si null : pas dimage post 
			if(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[1] != null) {
				//trie + inversement de la post
				imagePourTriePost.add(Scintigraphy.flipStackHorizontal(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[1].duplicate()));
			}
			imagesSelectDicom[i].close();

		}
		
		//on appelle la fonction de trie 
		// on met les imageplus (ANT) dans cette fonction pour les trier, ensuite on stock le tout dans le tableau en [0]
		sauvegardeImagesSelectDicom[0] = Scintigraphy.orderImagesByAcquisitionTime(imagePourTrieAnt);
		//Pareil pour la post
		sauvegardeImagesSelectDicom[1] = Scintigraphy.orderImagesByAcquisitionTime(imagePourTriePost);
	
		//test de verification de la taille des stack
		if(sauvegardeImagesSelectDicom[0].length != sauvegardeImagesSelectDicom[1].length) {
			System.err.println("(EsophagealTransit) Le nombre de slice ant est différent du nombre de slice post -> seules les ant seront pris en comptes");
			sauvegardeImagesSelectDicom[1] = new ImagePlus[0];
		}
		
		
		nbAcquisition  = sauvegardeImagesSelectDicom[0].length;
		
		
		// preparetion de l'image plus la 2eme phase 
		// image plus du projet de chaque acquisiton avec sur chaque slice une acquistion
		 impProjeteAllAcqui = null;
		if(imagesSelectDicom != null && imagesSelectDicom.length>0) {
			ArrayList<ImagePlus> imagesAnt = new ArrayList<>();
			for(int i =0; i< imagePourTrieAnt.size(); i++) {
				//null == pas d'image ant et/ou une image post et != une image post en [0]
				
				ImagePlus impAnt = imagePourTrieAnt.get(i);
				ImagePlus impAntProjete = DynamicScintigraphy.projeter(impAnt,0,impAnt.getStackSize(),"max");
				impAntProjete.setProperty("Info", impAnt.getInfoProperty());
				imagesAnt.add(impAntProjete);
				
			}
			//renvoi un stack trié des projection des images 
			//orderby ... renvoi un tableau d'imp trie par ordre chrono, avec en paramètre la liste des imp Ant
			//captureTo.. renvoi un stack avec sur chaque slice une imp du tableau passé en param ( un image trié, projeté et ant)
			//ImagePlus[] tabProj = Scintigraphy.orderImagesByAcquisitionTime(imagesAnt);
			impProjeteAllAcqui = new ImagePlus("EsoStack",StaticMethod.captureToStack(Scintigraphy.orderImagesByAcquisitionTime(imagesAnt)));
			//SK VOIR METHODE POUR GARDER LES METADATA ORIGINALE DANS LE STACK GENEREs
			impProjeteAllAcqui.setProperty("Info", sauvegardeImagesSelectDicom[0][0].getInfoProperty());
		}
		
		
		
		// phase 1
		// on retourne la stack de la 1ere acquisition
		return sauvegardeImagesSelectDicom[0][0];
	}


	@Override
	public void lancerProgramme() {
		// phase 1
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication fen = new FenApplication(this.getImp(), "Oesophageus");
		fen.getPanel_Quit_Draw_Contrast_btns().remove(fen.getBtn_drawROI());
		fen.getPanelInstructionsTextBtn().removeAll();
		
		
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new GridLayout(nbAcquisition, 1));
		
	    ButtonGroup buttonGroup = new ButtonGroup();    
	    JRadioButton[] radioButton = new JRadioButton[nbAcquisition];
	    for( int i =0; i< nbAcquisition; i++) {
	    	int num=i;
	    	radioButton[i] = new JRadioButton("Acquisition "+(i+1));
	    	radioButton[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					fen.setImp(sauvegardeImagesSelectDicom[0][num]);
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
				fen.getPanel_Quit_Draw_Contrast_btns().add(fen.getBtn_drawROI());
				fen.getPanelPrincipal().remove(startQuantificationButton);
				fen.getPanelPrincipal().remove(radioButtonPanelFlow);
				fen.getPanelInstructionsTextBtn().add(fen.getTextfield_instructions());
				fen.getPanelInstructionsTextBtn().add(fen.createPanelInstructionsBtns());
				
				fen.revalidate();
				
				
				fen.setImp(impProjeteAllAcqui);
				fen.getImagePlus().setSlice(1);
				fen.updateSliceSelector();
				FenApplication_EsophagealTransit.this.setImp(impProjeteAllAcqui);
				IJ.setTool(Toolbar.RECTANGLE);

				Controleur_EsophagealTransit cet = new Controleur_EsophagealTransit(FenApplication_EsophagealTransit.this, sauvegardeImagesSelectDicom);
				FenApplication_EsophagealTransit.this.getFenApplication().setControleur(cet);

			}
		});
	
		fen.getPanelPrincipal().add(radioButtonPanelFlow);
		fen.getPanelPrincipal().add(startQuantificationButton);
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		
		/*
		ControleurDynamique_EsophagealTransit cdet = new ControleurDynamique_EsophagealTransit(this);
		this.getFenApplication().setControleur(cdet);*/
		this.getFenApplication().setVisible(true);

		fen.resizeCanvas();
	}

	
	

	
	public int[] getFrameDurations() {
		return frameDurations;
	}


}
