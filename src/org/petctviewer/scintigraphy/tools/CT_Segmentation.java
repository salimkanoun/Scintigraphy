package org.petctviewer.scintigraphy.tools;

import java.io.InputStream;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.plugin.SubstackMaker;
import trainableSegmentation.WekaSegmentation;

public class CT_Segmentation implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		ImagePlus stack=WindowManager.getCurrentImage();
		SubstackMaker substackMaker= new SubstackMaker();
		ImagePlus image=substackMaker.makeSubstack(stack, "1-10");
		
		WekaSegmentation weka= new WekaSegmentation(true);
		InputStream classifier = ClassLoader.getSystemResourceAsStream("classifier5classes3d.model");
		weka.loadClassifier(classifier);
		ImagePlus resultat = weka.applyClassifier(image);
		resultat.show();
	}

}
