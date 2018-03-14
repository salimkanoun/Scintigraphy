package org.petctviewer.scintigraphy.tools;

import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.SubstackMaker;
import ij.plugin.filter.ImageMath;
import ij.util.DicomTools;
import trainableSegmentation.WekaSegmentation;

public class CT_Segmentation implements PlugIn {

	WekaSegmentation weka;
	ImagePlus inputImage;
	ImagePlus resultatFinal;
	
	@Override
	public void run(String arg0) {
		weka= new WekaSegmentation(true);
		InputStream classifier = ClassLoader.getSystemResourceAsStream("classifier5classes3d.model");
		weka.loadClassifier(classifier);
		CT_Segmentation_GUI gui= new CT_Segmentation_GUI(this);
		gui.pack();
		gui.setVisible(true);
	}
	
	public void setImageInput(ImagePlus imp) {
		inputImage=imp;
	}
	
	public void makeMaskedImage(int label) {
		//Binarize result to selected wanted tissue
		String rescaleIntercept = DicomTools.getTag(inputImage, "0028,1052");
		String rescaleSloap = DicomTools.getTag(inputImage, "0028,1053");
		System.out.println(rescaleIntercept);
		System.out.println(rescaleSloap);

		ImagePlus binaryMask=resultatFinal.duplicate();
		for (int i=1 ; i<=binaryMask.getImageStackSize() ; i++) {
			ImageMath.applyMacro(binaryMask.getImageStack().getProcessor(i), "if(v=="+label+") v=1 ; else v=0 ;", false);
		}
		ImageCalculator ic=new ImageCalculator();
		ImagePlus imp3 = ic.run("Multiply create stack", inputImage, binaryMask);
		imp3.show();
		//SK Reste à s'occuper du rescale slope et intercept
		//ValeurFinale du type image*rescaleSloap + Rescale Intercept
	}
	
	public void runSegmentation(JLabel status) {
		
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
			int numberOfSlice=inputImage.getImageStackSize();
			Double batch10 = numberOfSlice / (double) 10.0;
			ArrayList <ImagePlus> resultat = new ArrayList<ImagePlus>();
			
			@Override
			protected Void doInBackground() throws Exception {
				
				for (int i=0 ; i<=batch10.intValue(); i++) {
					//Create Substack of 10 Slice to avoir Run out Memory
					SubstackMaker substackMaker= new SubstackMaker();
					ImagePlus image = null;
					if (i<batch10.intValue()) {
						image=substackMaker.makeSubstack(inputImage, ((i*10)+1)+"-"+((i*10)+10));
					}
					//if last batch
					else {
						if (numberOfSlice - (i*10) != 0) image=substackMaker.makeSubstack(inputImage, ((i*10)+1)+"-"+(numberOfSlice));
					}
					
					//Calculate Segmentation
					status.setText("Calculating batch "+((i*10)+1)+"-"+((i*10)+10)+" On "+ numberOfSlice);
					ImagePlus resultatTemp = weka.applyClassifier(image);
					//Put result to Final Stack
					resultat.add(resultatTemp);
					//Free Memory
					System.gc();
				}
				
				//Merge result to Final Stack
				Concatenator concatenator =new Concatenator() ;
				ImagePlus[] resultatTableau=new ImagePlus[resultat.size()];
				resultat.toArray(resultatTableau);
				resultatFinal=concatenator.concatenate(resultatTableau, false);
					
				return null;
			}

			@Override
			protected void done(){
				resultatFinal.show();
			}
		};
		
		worker.execute();
		
	}

}
