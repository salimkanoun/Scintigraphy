package org.petctviewer.scintigraphy.calibration.chargement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.Doublet;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.StackStatistics;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class ModeleChargementCalibration {

	private RoiManager rm;
	
	private static final int NB_SPHERE = 7;
	
	// en 0 la sphere la plus grande
	public static final Double VOLUME_FANTOME_REF[] = {0.0D,26521.84878D,11494.04032D,5575.279763D,2572.440785D,1150.34651D,523.5987756D};
	
	//list des analyse / Liste des rois / map du SUV, TS et BG(calculé une seule fois)
	private ArrayList<ArrayList<HashMap<String,Object>>> paramResult;

	private ArrayList<ArrayList<HashMap<String,Object>>> paramResult2;

	private Doublet[][] resultData;
	
	private FenChargementCalibration fenCharg;
	
	public ModeleChargementCalibration(FenChargementCalibration fenCharg) {
		this.fenCharg = fenCharg;
	}
	
	public void runCalcul() {
		ArrayList<String[]> examList = fenCharg.getExamList();

		rm = new RoiManager(false);
		paramResult = new ArrayList<>();
		

		/*for each exam*/
		for(int k =0; k <examList.size(); k++) {
			
			fenCharg.setExamText("Exam "+(k+1)+"/"+examList.size());

	
		
			String floatPath = examList.get(k)[0];
			String maskPath = examList.get(k)[1];

			ImagePlus impMaskPropre = openImagePlus(maskPath);
			ImagePlus impFloatPropre = openImagePlus(floatPath);
			//System.out.println("numero acquisition "+k);

			
			ArrayList<HashMap<String,Object>> paramResultUnExam = new ArrayList<>();
			
			/*for each sphere (first sphere at v=1)(background at v=7)*/

			for(int i =1; i<= NB_SPHERE; i++) {
				fenCharg.setSphereText("Sphere "+i+"/"+NB_SPHERE);

				HashMap<String, Object> paramResultUnExamElements = new HashMap<>();
				
			//	System.out.println("********DEBUT n°"+i);
				
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
				
				// selection de la plus grande roi
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
    			paramResultUnExamElements.put("image", im.duplicate());
				
    			//im.show();
				//fin
				rm.reset();
				//System.out.println("********FIN n°"+i);
				
				StackStatistics ss = new StackStatistics(im);
				
				// pour seuil
				Double suvMax = ss.max;
				paramResultUnExamElements.put("SUVmax", ss.max);
				
				if(i==7) {
					paramResultUnExamElements.put("BG", ss.mean);// background
					paramResultUnExam.add(paramResultUnExamElements);
					continue;
				}
				
				ImagePlus im70 = im.duplicate();
				//70% du suv max 
				IJ.run(im70,"Macro...", "code=[if(v<"+suvMax*0.7+") v=NaN] stack");
				
				ss = new StackStatistics(im70);
				Double mean70 = ss.mean;
				paramResultUnExamElements.put("MEAN70", mean70);
				//Calcul du volume
				Double volume = 
						im.getLocalCalibration().pixelDepth*
						im.getLocalCalibration().pixelHeight*
						im.getLocalCalibration().pixelWidth*
						Math.pow(10,-12);
				// mettre le volume dans la hash map
				paramResultUnExamElements.put("VolumeVoxel", volume);

				
				//recherche plus petite difference entre la taille souhaitée et la taille relevée
				HashMap<Double,Double> listeSeuil = new HashMap<>();
				for(Double j=0.0D; j<suvMax; j+=0.1D) {
					IJ.run(im,"Macro...", "code=[if(v<"+j+") v=NaN] stack");
					ss = new StackStatistics(im);
					listeSeuil.put(Math.abs(ss.pixelCount*volume -(VOLUME_FANTOME_REF[i]/1000)),j);
				}
				
				//IJ.log(listeSeuil.get(Collections.min(listeSeuil.keySet()))+"");
				paramResultUnExamElements.put("TS", listeSeuil.get(Collections.min(listeSeuil.keySet())));
				paramResultUnExamElements.put("SUV", mean70);

				// put les autre valeurs
				paramResultUnExam.add(paramResultUnExamElements);
			}
			paramResult.add(paramResultUnExam);
		}
		

		paramResult2 = new ArrayList<>();

		
		//test de la list de list de map
		// oblige de le faire apres car le background est releve en dernier
	
		for(int  i =0; i< paramResult.size(); i++) {
			//System.out.println("***************exam(i) = "+i );
			ArrayList<HashMap<String,Object>> paramResultUnExam2 = new ArrayList<>();

			 Double BG = (Double) paramResult.get(i).get(paramResult.get(i).size()-1).get("BG");
	
			// System.out.println("BG :"+BG);
			for(int j=0; j < paramResult.get(i).size(); j++) {
				HashMap<String, Object> paramResultUnExamElements2 = new HashMap<>();

				if(j!=6) {
/*					 System.out.println("roi(j) = "+j);
					 System.out.println(" TS :"+paramResult.get(i).get(j).get("TS"));
					 System.out.println(" SUV :"+paramResult.get(i).get(j).get("SUV"));
					 System.out.println(" SUVmax :"+paramResult.get(i).get(j).get("SUVmax"));

					 System.out.println(" BG :"+paramResult.get(i).get(j).get("BG"));
*/					 
					 Double TS = (Double) paramResult.get(i).get(j).get("TS");	 
					 Double SUV = (Double) paramResult.get(i).get(j).get("SUV");
					 
//					 System.out.println(" X :"+((SUV-BG)/BG));
//				 System.out.println(" Y :"+(TS/(SUV-BG)));
					 
					 paramResultUnExamElements2.put("TS",paramResult.get(i).get(j).get("TS"));
					 paramResultUnExamElements2.put("SUV",paramResult.get(i).get(j).get("SUV"));
					 paramResultUnExamElements2.put("SUVmax",paramResult.get(i).get(j).get("SUVmax"));
					 paramResultUnExamElements2.put("BG",BG);
					 paramResultUnExamElements2.put("MEAN70",paramResult.get(i).get(j).get("MEAN70"));
					 paramResultUnExamElements2.put("image",paramResult.get(i).get(j).get("image"));
					 paramResultUnExamElements2.put("VolumeVoxel",paramResult.get(i).get(j).get("VolumeVoxel"));
					 paramResultUnExamElements2.put("TrueSphereVolume",VOLUME_FANTOME_REF[j+1] );

					 
					 paramResultUnExamElements2.put("x",((SUV-BG)/BG));
					 paramResultUnExamElements2.put("y",(TS/(SUV-BG)));

//					System.out.println();
					paramResultUnExam2.add(paramResultUnExamElements2);
				}
			}
			paramResult2.add(paramResultUnExam2);
		}	
//		System.out.println("a1= "+ paramResult2.get(0).get(0).get("x"));
		
		//fermeture du roi manager
		rm.close();
		
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
	 
	public  ArrayList<ArrayList<HashMap<String, Object>>> getDonnees2() {
		return this.paramResult2;
	}
	
	
	
	
	/**
	 public static void updateBouton(){
		    SwingWorker sw = new SwingWorker<Integer, String>(){
		    	
		    	
		      protected Integer doInBackground() throws Exception {
		        int i;
		        for(i = 0; i < 5; i++){
		          try {
		            //On change la propriété d'état
		            setProgress(i);
		            //On publie un résultat intermédiaire 
		            publish("Tour de boucle N° " + (i+1));
		            Thread.sleep(1000);
		          } catch (InterruptedException e) {
		            e.printStackTrace();
		          }               
		        }
		        return i;
		      }

		      public void done(){
		        if(SwingUtilities.isEventDispatchThread())
		          System.out.println("Dans l'EDT ! ");
		        try {
		          //On utilise la méthode get() pour récupérer le résultat
		          //de la méthode doInBackground()
		          bouton.setText("Traitement terminé au bout de "+get()+" fois !");
		        } catch (InterruptedException e) {
		          e.printStackTrace();
		        } catch (ExecutionException e) {
		          e.printStackTrace();
		        }
		      }   
		      //La méthode gérant les résultats intermédiaires
		      public void process(List<String> list){
		        for(String str : list)
		          System.out.println(str);
		      }
		    };
		    //On écoute le changement de valeur pour la propriété
		    sw.addPropertyChangeListener(new PropertyChangeListener(){
		      //Méthode de l'interface
		      public void propertyChange(PropertyChangeEvent event) {
		        //On vérifie tout de même le nom de la propriété
		        if("progress".equals(event.getPropertyName())){
		          if(SwingUtilities.isEventDispatchThread())
		            System.out.println("Dans le listener donc dans l'EDT ! ");
		          //On récupère sa nouvelle valeur
		          bouton.setText("Pause " + (Integer) event.getNewValue());
		        }            
		      }         
		    });
		    //On lance le SwingWorker
		    sw.execute();
		  }
*/	
}
