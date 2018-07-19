package org.petctviewer.scintigraphy.calibration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JFrame;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.ImageCalculator;
import ij.plugin.Stack_Statistics;
import ij.plugin.Thresholder;
import ij.plugin.filter.ImageMath;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.StackStatistics;
import loci.formats.FormatException;
import loci.formats.in.NiftiReader;
import loci.formats.tools.BioFormatsExtensionPrinter;
import loci.plugins.BF;
import loci.plugins.LociImporter;
import loci.plugins.in.ImporterOptions;
import net.imagej.display.SelectWindow;
import ome.xml.model.Mask;

public class FenResultatsCalibration extends JFrame{
	
	private RoiManager rm;
	
	public FenResultatsCalibration(ArrayList<String[]> examList) {
		ControleurCalibration cc = new ControleurCalibration(examList);
		
		
		String maskPath="/Users/diego/Desktop/rep2/src_rec_apr_24_2018_mask.nii" ;
		String floatPath="/Users/diego/Desktop/rep2/src_rec_apr_24_2018_float.nii";
		rm=new RoiManager();

		// for each sphere
		for(int i =1; i<= 1; i++) {
		//int i =2;
			System.out.println("********DEBUT n°"+i);
			//for one sphere
			/*********sur mask*******/

			//dublication
			ImagePlus impMaskDuplicated = openImagePlus(maskPath);
			
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
			ImagePlus impFloatDuplicated =openImagePlus(floatPath);
			
			//on met la roi du mask sur le float
			impFloatDuplicated.setRoi((Roi) rm.getRoi(0).clone());

			//crop
			IJ.run(impFloatDuplicated ,"Crop", "");
		
			//multiplication des 2 images mask et float
			ImageCalculator ic = new ImageCalculator();
			ImagePlus im = ic.run("Multiply stack create", impFloatDuplicated, impMaskDuplicated);
			
			//pour enlever les valeurs 0 et mettrte anan pour mean
			IJ.run(im,"Macro...", "code=[if(v==0) v=NaN] stack");

			im.show();
			rm.reset();
			System.out.println("********FIN n°"+i);
			
			IJ.log("\t\tindice : "+i);
			StackStatistics ss = new StackStatistics(im);
			IJ.log("mean");
			IJ.log(ss.mean+"");
			
			IJ.log("pixekCount");
			IJ.log(ss.pixelCount+"");
			// pour seuil
			Double suvMax = ss.max;
			ImagePlus im70 = im.duplicate();
			IJ.run(im70,"Macro...", "code=[if(v<"+suvMax*0.7+") v=NaN] stack");
			
			ss = new StackStatistics(im70);
			Double mean70 = ss.mean;
			
			IJ.log("mean 70");
			IJ.log(mean70+"");
			
			
			IJ.log("pixekCount2");
			IJ.log(ss.pixelCount+"");
			IJ.log("");
		
			Double volume = 
					im.getLocalCalibration().pixelDepth*
					im.getLocalCalibration().pixelHeight*
					im.getLocalCalibration().pixelWidth*
					Math.pow(10,-12);
			
		
			HashMap<Double,Double> listeSeuil = new HashMap<>();
			for(Double j=0.0D; j<suvMax; j+=0.1D) {
				IJ.run(im,"Macro...", "code=[if(v<"+j+") v=NaN] stack");
				ss = new StackStatistics(im);
				//IJ.log( "diffrencer"+(ss.pixelCount*volume -18.5D) +" seuil "+ j);
				//Double[] coupleseuildiff = {Math.abs(ss.pixelCount*volume -18.5D),j};
				listeSeuil.put(Math.abs(ss.pixelCount*volume -18.5D),j);
			}
			
			IJ.log(listeSeuil.get(Collections.min(listeSeuil.keySet()))+"");

		}
		//1ml = 10^12dm3
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
}
