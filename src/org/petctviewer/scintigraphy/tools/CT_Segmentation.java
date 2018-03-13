package org.petctviewer.scintigraphy.tools;

import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.PlugIn;
import ij.plugin.SubstackMaker;
import trainableSegmentation.WekaSegmentation;

public class CT_Segmentation implements PlugIn {

	WekaSegmentation weka;
	ImagePlus inputImage;
	
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
	
	public void runSegmentation(JLabel status) {
		
		
		
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
			int numberOfSlice=inputImage.getImageStackSize();
			Double batch10 = numberOfSlice / (double) 10.0;
			ImagePlus resultat = null;
			@Override
			protected Void doInBackground() throws Exception {
				
				for (int i=0 ; i<batch10.intValue(); i++) {
					//Create Substack of 10 Slice to avoir Run out Memory
					SubstackMaker substackMaker= new SubstackMaker();
					ImagePlus image=substackMaker.makeSubstack(inputImage, ((i*10)+1)+"-"+((i*10)+10));
					//Calculate Segmentation
					status.setText("Calculating batch "+((i*10)+1)+"-"+((i*10)+10)+" On "+ numberOfSlice);
					ImagePlus resultatTemp = weka.applyClassifier(image);
					//Put result to Final Stack
					if (i==0) resultat = weka.applyClassifier(image);
					else {
						Concatenator concatenator =new Concatenator() ;
						// SK ENCHAINER PLUTOT
						resultat=concatenator.concatenate(resultat, resultatTemp, false);
					}
				}
				return null;
			}

			@Override
			protected void done(){
				resultat.show();
			}
		};
		worker.execute();
		
	}

}
