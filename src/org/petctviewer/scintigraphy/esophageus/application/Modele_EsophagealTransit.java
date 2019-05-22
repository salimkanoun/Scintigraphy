package org.petctviewer.scintigraphy.esophageus.application;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.library.Library_Dicom;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModeleScinDyn;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Modele_EsophagealTransit  extends ModeleScinDyn{
	
	// sauvegarde des imageplus de depart avec tous leur stack chacun : pour pouvoir faire les calculs de mean dans le temps//trié 
	private ImageSelection [][] sauvegardeImagesSelectDicom;

	// list : liste des examen
	// list->map : map des 4 roi ( entier, premier tier, deuxieme tier et troisieme tier)
	// list->map->list : list des mean(double) pour tous le stack
	private ArrayList<HashMap<String, ArrayList<Double>>> examenMean;
 
	//pour le condensé dynamique
	ArrayList<Object[]> dicomRoi;
	
	public EsophagealTransit esoPlugIn;
	
	public Modele_EsophagealTransit( ImageSelection [][] sauvegardeImagesSelectDicom, String studyName, EsophagealTransit esoPlugIn) {
		super(sauvegardeImagesSelectDicom[0], studyName, esoPlugIn.getFrameDurations());
		this.sauvegardeImagesSelectDicom = sauvegardeImagesSelectDicom;
		
		examenMean = new ArrayList<>();
		
		this.esoPlugIn = esoPlugIn;
	}
	
	// utlisé apres que toutes les roi ai ete dessiné
	public void setRoiManager(RoiManager roiManager) {
		this.roiManager = roiManager;
	}
	
	
	//vas ordorner les imageplus pour les envoyer au model fenettre resultats pour les calculs
	@Override
	public void calculerResultats() {
		
		// pour etre sur quon a le meme nombre de roi que d'image plus
		if(sauvegardeImagesSelectDicom[0].length != this.roiManager.getCount()) {
			System.err.println("nombre d'imageplus different du nombre de roi");
		}
		
		examenMean = new ArrayList<>();
		//for each imageplus (ant) and roi qui lui correspond     each acqui
		for(int i =0; i< sauvegardeImagesSelectDicom[0].length; i++) {
			
			
			HashMap<String, ArrayList<Double>> map4rois = new HashMap<>();
			// on stock les temps d'acquisition
			int[] tempsInt =   (Library_Dicom.buildFrameDurations(sauvegardeImagesSelectDicom[0][i].getImagePlus()));// on prends la ant
			
			double [] tempsSeconde = new double[tempsInt.length];
			for(int j =0;j<tempsInt.length; j++) {
				tempsSeconde[j] = tempsInt[j]/1000.0D;
			}
			
			
			ArrayList<Double> temps = new ArrayList<>();
			Double memtemps = 0.0;
			for(int j =0; j< tempsSeconde.length; j++) {
				memtemps += tempsSeconde[i];
				temps.add(memtemps);
			}
			map4rois.put("temps",temps);
			
			
			//decoupage de la roi en 3
			Roi premiereRoi = this.roiManager.getRoi(i);
			
			Point pointOrigine = premiereRoi.getContainedPoints()[0];
			Double largeurRef = premiereRoi.getBounds().getWidth();
			Double hauteurRef = premiereRoi.getBounds().getHeight();
			
			int tierHauteurRoiRef = (int) Math.round(hauteurRef/3);
			
			// roi 1er 1/3
			Roi unTier 	  = new Roi	(pointOrigine.getX(), pointOrigine.getY(), 										 largeurRef, tierHauteurRoiRef);
			Roi deuxTier  = new Roi	(pointOrigine.getX(), pointOrigine.getY()+tierHauteurRoiRef+1, 					 largeurRef, tierHauteurRoiRef);
			Roi troisTier = new Roi	(pointOrigine.getX(), pointOrigine.getY()+tierHauteurRoiRef+tierHauteurRoiRef+2, largeurRef, tierHauteurRoiRef);

			
			//toutes les listes contenant les mean pour chaque imp
			ArrayList<Double> roiEntier = new ArrayList<>();
			ArrayList<Double> unTierList = new ArrayList<>();
			ArrayList<Double> deuxTierList = new ArrayList<>();
			ArrayList<Double> troisTierList = new ArrayList<>();
						
			//si on a pas de post on utlise que les ant
			if(sauvegardeImagesSelectDicom[1].length==0) {
				//pour chaque slice de l'image plus
				for(int j =1; j<= sauvegardeImagesSelectDicom[0][i].getImagePlus().getStackSize(); j++) {
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setSlice(j);

					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(premiereRoi);
					roiEntier.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
					
					//for each roi (ici 3)
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(unTier);
					unTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(deuxTier);
					deuxTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(troisTier);
					troisTierList.add(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus())/tempsSeconde[j-1]);
				}
			}else {
				//pour chaque slice de l'image plus
				for(int j =1; j<= sauvegardeImagesSelectDicom[0][i].getImagePlus().getStackSize(); j++) {
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setSlice(j);
					sauvegardeImagesSelectDicom[1][i].getImagePlus().setSlice(j);


					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(premiereRoi);
					sauvegardeImagesSelectDicom[1][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[1][i].getImagePlus().setRoi(premiereRoi);
					roiEntier.add(Library_Quantif.moyGeom(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus()),
													 Library_Quantif.getCounts(sauvegardeImagesSelectDicom[1][i].getImagePlus()))
								/tempsSeconde[j-1]);
					// moygeom( cout(ant), cout(post)) /temps
					
					//for each roi (ici 3)
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(unTier);
					sauvegardeImagesSelectDicom[1][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[1][i].getImagePlus().setRoi(unTier);
					unTierList.add(Library_Quantif.moyGeom(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus()),
							 						Library_Quantif.getCounts(sauvegardeImagesSelectDicom[1][i].getImagePlus()))
								/tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(deuxTier);
					sauvegardeImagesSelectDicom[1][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[1][i].getImagePlus().setRoi(deuxTier);
					deuxTierList.add(Library_Quantif.moyGeom(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus()),
	 												  Library_Quantif.getCounts(sauvegardeImagesSelectDicom[1][i].getImagePlus()))
							 	  /tempsSeconde[j-1]);
					
					sauvegardeImagesSelectDicom[0][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[0][i].getImagePlus().setRoi(troisTier);
					sauvegardeImagesSelectDicom[1][i].getImagePlus().deleteRoi();
					sauvegardeImagesSelectDicom[1][i].getImagePlus().setRoi(troisTier);
					troisTierList.add(Library_Quantif.moyGeom(Library_Quantif.getCounts(sauvegardeImagesSelectDicom[0][i].getImagePlus()),
														Library_Quantif.getCounts(sauvegardeImagesSelectDicom[1][i].getImagePlus()))
									/tempsSeconde[j-1]);
				}

			}
			
			
			
			//on met tous dans une map pour faciliter le tranfert
			map4rois.put("entier", roiEntier);
			map4rois.put("unTier", unTierList);			
			map4rois.put("deuxTier", deuxTierList);
			map4rois.put("troisTier", troisTierList);
			
			//un le calcul fini pour une image plus on rajoute la map a la liste qui sera envoyé au modele du resultat
			examenMean.add(map4rois);
		
		}	
	
		
		//condense
		dicomRoi = new ArrayList<>();
		for(int i =0; i< sauvegardeImagesSelectDicom[0].length; i++) {
			Object [] content = {sauvegardeImagesSelectDicom[0][i].getImagePlus(), this.roiManager.getRoi(i).getBounds()};
			dicomRoi.add(content);	
		}
	}

	public ArrayList<HashMap<String, ArrayList<Double>>> getExamenMean(){
		return this.examenMean;
	}

	public ArrayList<Object[]> getDicomRoi() {
		return this.dicomRoi;
	}
	
}
