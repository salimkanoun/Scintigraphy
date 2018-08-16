package org.petctviewer.scintigraphy.esophageus;

import java.awt.Color;
import java.beans.ConstructorProperties;
import java.util.ArrayList;

import org.petctviewer.scintigraphy.esophageus.application.Controleur_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.application.FenApplication_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.DynamicScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import loci.poi.util.SystemOutLogger;

public class EsophagealTransit extends Scintigraphy {

	private ImagePlus impAnt, impPost, impProjetee, impProjeteeAnt;
	private int[] frameDurations;
	
	// [ant:0 | post:1][numero de acquisition]
	private ImagePlus[][] sauvegardeImagesSelectDicom;
	
	public EsophagealTransit() {
		super("Eso");
	}


	@Override
	public void lancerProgramme() {
		Overlay overlay = Scintigraphy.initOverlay(this.getImp(), 12);
		Scintigraphy.setOverlayDG(overlay, this.getImp(), Color.yellow);
		
		FenApplication_EsophagealTransit fen = new FenApplication_EsophagealTransit(this.getImp(),this);
		this.setFenApplication(fen);
		this.getImp().setOverlay(overlay);
		
		Controleur_EsophagealTransit cet = new Controleur_EsophagealTransit(this, sauvegardeImagesSelectDicom, this);
		this.getFenApplication().setControleur(cet);
		this.getFenApplication().setVisible(true);
	}

	
	//possible de refactorier le trie des images....
	@Override
	protected ImagePlus preparerImp(ImagePlus[] imagesSelectDicom) {
		//entrée : tableau de toutes les images passées envoyé par la selecteur de dicom

		sauvegardeImagesSelectDicom = new  ImagePlus[2][imagesSelectDicom.length];

		//sauvegarde des images pour le modele
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
		
		ImagePlus imTest = null;
		if(imagesSelectDicom != null && imagesSelectDicom.length>0) {
			ArrayList<ImagePlus> imagesAnt = new ArrayList<>();
			for(int i =0; i< imagesSelectDicom.length; i++) {
				//null == pas d'image ant et/ou une image post et != une image post en [0]
				if(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[0] != null) {
					imagesAnt.add(DynamicScintigraphy.projeter(Scintigraphy.sortDynamicAntPost(imagesSelectDicom[i])[0]/*la ant*/,0,imagesSelectDicom[i].getStackSize(),"max"));
					//System.out.println("i:"+i+" is ant  j : "+imagesAnt.size());
				}
			}
			//renvoi un stack trié des projection des images 
			//orderby ... renvoi un tableau d'imp trie par ordre chrono, avec en paramètre la liste des imp Ant
			//captureTo.. renvoi un stack avec sur chaque slice une imp du tableau passé en param ( un image trié, projeté et ant)
			imTest = new ImagePlus("test",ModeleScin.captureToStack(Scintigraphy.orderImagesByAcquisitionTime(imagesAnt)));
			imTest.setProperty("Info", sauvegardeImagesSelectDicom[0][0].getInfoProperty());
		}
		return imTest;
	}


	
	public int[] getFrameDurations() {
		return frameDurations;
	}

	

}
