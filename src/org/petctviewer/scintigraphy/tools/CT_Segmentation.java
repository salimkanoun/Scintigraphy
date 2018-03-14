package org.petctviewer.scintigraphy.tools;

import java.awt.Color;
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

	private WekaSegmentation weka;
	private ImagePlus inputImage;
	private ImagePlus resultatFinal;
	private CT_Segmentation_GUI gui;
	
	@Override
	public void run(String arg0) {
		weka= new WekaSegmentation(true);
		InputStream classifier = ClassLoader.getSystemResourceAsStream("classifier5classes3d.model");
		weka.loadClassifier(classifier);
		gui= new CT_Segmentation_GUI(this);
		gui.pack();
		gui.setVisible(true);
	}
	
	public void setImageInput(ImagePlus imp) {
		inputImage=imp;
	}
	
	public void makeMaskedImage(int label) {
		//Binarize result to selected wanted tissue
		String rescaleIntercept = DicomTools.getTag(inputImage, "0028,1052").trim();
		String rescaleSloap = DicomTools.getTag(inputImage, "0028,1053").trim();

		ImagePlus binaryMask=resultatFinal.duplicate();
		for (int i=1 ; i<=binaryMask.getImageStackSize() ; i++) {
			ImageMath.applyMacro(binaryMask.getImageStack().getProcessor(i), "if(v=="+label+") v=1 ; else v=0 ;", false);
		}
		ImageCalculator ic=new ImageCalculator();
		ImagePlus imp3 = ic.run("Multiply create stack", inputImage, binaryMask);
		for (int i=1 ; i<=imp3.getImageStackSize() ; i++) {
			ImageMath.applyMacro(imp3.getImageStack().getProcessor(i), ( "if(v==0) v=(-1000)-("+rescaleIntercept+")/("+rescaleSloap+");" ), false);
		}
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
				status.setForeground(Color.red);
				for (int i=0 ; i<=batch10.intValue(); i++) {
					//Create Substack of 10 Slice to avoir Run out Memory
					SubstackMaker substackMaker= new SubstackMaker();
					ImagePlus image = null;
					
					//User progress feedback
					status.setText("Calculating batch "+ (i+1) +" On "+ (batch10.intValue()+1));
					
					if (i<batch10.intValue()) {
						image=substackMaker.makeSubstack(inputImage, ((i*10)+1)+"-"+((i*10)+10));
						//Calculate Segmentation
						ImagePlus resultatTemp = weka.applyClassifier(image);
						//Put result to Final Stack
						resultat.add(resultatTemp);
					}
					
					//if last batch
					else {
						if (numberOfSlice - (i*10) != 0) {
							image=substackMaker.makeSubstack(inputImage, ((i*10)+1)+"-"+(numberOfSlice));
							//Calculate Segmentation
							ImagePlus resultatTemp = weka.applyClassifier(image);
							//Put result to Final Stack
							resultat.add(resultatTemp);
						}
					}
					
					//FreeMemory
					System.gc();
				}
				
				//Merge result to Final Stack
				if (resultat.size()>1) {
					Concatenator concatenator =new Concatenator() ;
					ImagePlus[] resultatTableau=new ImagePlus[resultat.size()];
					resultat.toArray(resultatTableau);
					resultatFinal=concatenator.concatenate(resultatTableau, false);
				}
				else resultatFinal=resultat.get(0);
				
					
				return null;
			}

			@Override
			protected void done(){
				resultatFinal.show();
				status.setText("Segmentation done");
				status.setForeground(Color.green);
				//Activate the button to generate the final Image
				gui.getGenerateMaskButton().setEnabled(true);
			}
		};
		
		worker.execute();
		
	}

}
