package org.petctviewer.scintigraphy.calibration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.StackStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class ModeleCalibration {

	private RoiManager rm;
	
	private static final int NB_SPHERE = 7;
	
	private static final Double VOLUME_FANTOME_REF[] = {0.0D,26521.84878D,11494.04032D,5575.279763D,2572.440785D,1150.34651D,523.5987756D};
	// en 0 la sphere la plus grande
	
	//list des analyse / Liste des rois / map du SUV, TS et BG(calculé une seule fois)
	private ArrayList<ArrayList<HashMap<String,Double>>> paramResult;

	private Doublet[][] resultData;
	
	public ModeleCalibration(ArrayList<String[]> examList) {
		rm = new RoiManager();
		paramResult = new ArrayList<>();
		
		//for each exam
		for(int k =0; k <examList.size(); k++) {
			String floatPath = examList.get(k)[0];
			String maskPath = examList.get(k)[1];
			
			ImagePlus impMaskPropre =openImagePlus(maskPath);
			ImagePlus impFloatPropre = openImagePlus(floatPath);
			System.out.println("numero acquisition "+k);
			
			ArrayList<HashMap<String,Double>> paramResultUnExam = new ArrayList<>();
			
			// for each sphere (first sphere at v=1)(background at v=7)
			for(int i =1; i<= NB_SPHERE; i++) {
				
				HashMap<String, Double> paramResultUnExamElements = new HashMap<>();
				
				System.out.println("********DEBUT n°"+i);
				/*********sur mask*******/
	
				//dublication
				ImagePlus impMaskDuplicated = impMaskPropre.duplicate();

				// macros avec formule		
				IJ.run(impMaskDuplicated,"Macro...", "code=[if(v=="+(i)+") v=1 ; else v=0] stack");
				
				//threshold
				IJ.setThreshold(impMaskDuplicated,1, 1);
				IJ.run(impMaskDuplicated,"Convert to Mask", "method=Default background=Default");
				
				//recherche de la plus grande roi
				ArrayList<Double> listSliceAire = new ArrayList<>();
				listSliceAire.add( 0.0D);
				for(int j = 1; j< impMaskDuplicated.getStackSize(); j++) {
					impMaskDuplicated.setSlice(j);
					IJ.run(impMaskDuplicated ,"Create Selection", "");
					Roi roi = impMaskDuplicated.getRoi();
					if(roi != null) {
						//area
						listSliceAire.add(impMaskDuplicated.getRoi().getFloatHeight()*impMaskDuplicated.getRoi().getFloatWidth());
					}else {
						listSliceAire.add(0.0D);
					}
				}
				
				// selection de la plus grand roi
				impMaskDuplicated.setSlice(listSliceAire.indexOf(Collections.max(listSliceAire)));
				IJ.run(impMaskDuplicated ,"Create Selection", "");
	
				
				//sauvegarde roi 
				rm.addRoi(impMaskDuplicated.getRoi());
	
				impMaskDuplicated.deleteRoi();
	
				//si en dehors v=0 si on est à l'interieur v=1
				IJ.run(impMaskDuplicated,"Macro...", "code=[if(v==255) v=1] stack");
	
				//on remet la roi sauvegardée
				impMaskDuplicated.setRoi((Roi) rm.getRoi(0).clone());
			
				//crop
				IJ.run(impMaskDuplicated ,"Crop", "");
				
				
				/*********sur float*******/
				
				//dublication
				ImagePlus impFloatDuplicated =impFloatPropre.duplicate();

				//on met la roi du mask sur le float
				impFloatDuplicated.setRoi((Roi) rm.getRoi(0).clone());
	
				//crop
				IJ.run(impFloatDuplicated ,"Crop", "");
			
				//multiplication des 2 images mask et float
				ImageCalculator ic = new ImageCalculator();
				ImagePlus im = ic.run("Multiply stack create", impFloatDuplicated, impMaskDuplicated);
				
				//pour enlever les valeurs 0 et mettre nan pour mean
				IJ.run(im,"Macro...", "code=[if(v==0) v=NaN] stack");
	
			//	im.show();
				//fin
				rm.reset();
				System.out.println("********FIN n°"+i);
				
				
				
				StackStatistics ss = new StackStatistics(im);
				
				/*
				IJ.log("\t\tindice : "+i);
				IJ.log("mean");
				IJ.log(ss.mean+"");
				IJ.log("pixekCount");
				IJ.log(ss.pixelCount+"");
	*/
				// pour seuil
				Double suvMax = ss.max;
				if(i==7) {
					paramResultUnExamElements.put("BG", ss.mean);/// background
					paramResultUnExam.add(paramResultUnExamElements);

					continue;
				}
				
				ImagePlus im70 = im.duplicate();
				//70% du suv max 
				IJ.run(im70,"Macro...", "code=[if(v<"+suvMax*0.7+") v=NaN] stack");
				
				ss = new StackStatistics(im70);
				Double mean70 = ss.mean;
				
			/*	
				IJ.log("mean 70");
				IJ.log(mean70+"");
				
				IJ.log("pixekCount2");
				IJ.log(ss.pixelCount+"");
				IJ.log("");
			*/
				//Calcul du volume
				Double volume = 
						im.getLocalCalibration().pixelDepth*
						im.getLocalCalibration().pixelHeight*
						im.getLocalCalibration().pixelWidth*
						Math.pow(10,-12);
				
				//recherche plus petite difference entre la taille souhaitée et la taille relevé
				HashMap<Double,Double> listeSeuil = new HashMap<>();
				for(Double j=0.0D; j<suvMax; j+=0.1D) {
					IJ.run(im,"Macro...", "code=[if(v<"+j+") v=NaN] stack");
					ss = new StackStatistics(im);
					//IJ.log( "diffrencer"+(ss.pixelCount*volume -(VOLUME_FANTOME_REF[i]/1000)) +" seuil "+ j);
					//Double[] coupleseuildiff = {Math.abs(ss.pixelCount*volume -18.5D),j};
					listeSeuil.put(Math.abs(ss.pixelCount*volume -(VOLUME_FANTOME_REF[i]/1000)),j);
				}
				
				IJ.log(listeSeuil.get(Collections.min(listeSeuil.keySet()))+"");
				paramResultUnExamElements.put("TS", listeSeuil.get(Collections.min(listeSeuil.keySet())));
				paramResultUnExamElements.put("SUV", mean70);

				// put les autre valeurs
				paramResultUnExam.add(paramResultUnExamElements);
			}
			paramResult.add(paramResultUnExam);
		}
		
		
		//this.serieCollection = new XYSeriesCollection();
		resultData = new Doublet[examList.size()][NB_SPHERE-1];
		
	
		
		//test de la list de list de map
		// oblige de le faire apres car le background est releve en dernier
		for(int  i =0; i< paramResult.size(); i++) {
			//XYSeries serie = new XYSeries("Aqcui "+i);
			System.out.println("***************exam(i) = "+i );
			 Double BG = paramResult.get(i).get(paramResult.get(i).size()-1).get("BG");
			 System.out.println("BG :"+BG);
			for(int j=0; j < paramResult.get(i).size(); j++) {
				if(j!=6) {
					 System.out.println("roi(j) = "+j);
					 System.out.println(" TS :"+paramResult.get(i).get(j).get("TS"));
					 System.out.println(" SUV :"+paramResult.get(i).get(j).get("SUV"));
					 System.out.println(" BG :"+paramResult.get(i).get(j).get("BG"));
					 
					 Double TS = paramResult.get(i).get(j).get("TS");	 
					 Double SUV = paramResult.get(i).get(j).get("SUV");
	
					 
					 System.out.println(" X :"+((SUV-BG)/BG));
					 System.out.println(" Y :"+(TS/(SUV-BG)));
	
					//serie.add(((SUV-BG)/BG), (TS/(SUV-BG)));
					 resultData[i][j] = new Doublet((SUV-BG)/BG, TS/(SUV-BG));
					System.out.println();
				}
			}					
			//this.serieCollection.addSeries(serie);
		}
		
		
		//print valeur en dur 
		
		

	}
	
	private ImagePlus openImagePlus(String path) {
		ImporterOptions m;
		ImagePlus imp = null;
		try {
			m = new ImporterOptions();
			m.parseArg(path);
			//afficher le fichier
			//BF.openImagePlus(m)[0].show();	
			imp = BF.openImagePlus(m)[0];
		} catch (IOException|FormatException e) {
			e.printStackTrace();
		} 
		return imp;
	}
	
	private void afficherImagePLus(ImagePlus imp,Roi roi) {
		ImagePlus i=imp.duplicate();
		i.setRoi(roi);
		i.setTitle("salim");
		i.show();
	}

	public Doublet[][] getDonnees() {
		return this.resultData;
	}
	
	//to debug
	public static Doublet[][] setDonnees(){
		Doublet[][] d = new Doublet[5][6];
		d[0][0] = new Doublet(1063.3218358660768D, 0.44588858911374424D);
		d[0][1] = new Doublet(1067.5094504468002D, 0.3435332856457216D);
		d[0][2] = new Doublet(1075.6134848903816D, 0.29710920566867444D);
		d[0][3] = new Doublet(1085.4444906104673D, 0.3354437445920691D);
		d[0][4] = new Doublet(1144.5596754600492D, 0.35473641529112776D);
		d[0][5] = new Doublet(1300.609884418601D, 0.30411820110510795D);
		d[1][0] = new Doublet(14.421943348191165D, 0.4418919476045528D);
		d[1][1] = new Doublet(14.495709820355993D, 0.36148443059528607D);
		d[1][2] = new Doublet(14.954254789497297D, 0.3219893447923146D);
		d[1][3] = new Doublet(15.360010154478156D, 0.3227036671631728D);
		d[1][4] = new Doublet(15.89198067399009D, 0.32081290633860965D);
		d[1][5] = new Doublet(9.583768488319942D, 0.4433148708424135D);
		d[2][0] = new Doublet(5.8514917802345074D, 0.49435974657758197D);
		d[2][1] = new Doublet(5.983372882994303D, 0.42974527357662806D);
		d[2][2] = new Doublet(6.540263144717796D, 0.39315332726177543D);
		d[2][3] = new Doublet(5.8792817416813925D, 0.4191307258995072D);
		d[2][4] = new Doublet(5.498713317645333D, 0.44813895211650334D);
		d[2][5] = new Doublet(1.7609865956269029D, 1.216801906621926D);
		d[3][0] = new Doublet(3.6034874711797307D, 0.5812458860014016D);
		d[3][1] = new Doublet(3.663831775040131D, 0.5145053476348772D);
		d[3][2] = new Doublet(3.6224222603814282D, 0.49147650376663377D);
		d[3][3] = new Doublet(3.0897049253509867D, 0.5762153573602007D);
		d[3][4] = new Doublet(2.418959217577922D, 0.7359923287509254D);
		d[3][5] = new Doublet(0.2191369847740241D, 6.690603090247335D);
		d[4][0] = new Doublet(2.226015813509347D, 0.695322515892987D);
		d[4][1] = new Doublet(2.218766228629182D, 0.6510881150234067D);
		d[4][2] = new Doublet(2.30630754761493D, 0.6263745366352871D);
		d[4][3] = new Doublet(2.02730970060167D, 0.6616777528793599D);
		d[4][4] = new Doublet(0.872927442318167D, 1.536697853744085D);
		d[4][5] = new Doublet(0.018021787846651975D, 68.70789642125047D);
		return d;
	}
	
}
