package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.ZProjector;
import ij.util.DicomTools;

public abstract class VueScinDyn extends VueScin {

	protected ImagePlus impAnt, impPost;

	private int[] frameDurations;

	public VueScinDyn(String title) {
		super(title);
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if (titresFenetres.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		ImagePlus[] images = new ImagePlus[titresFenetres.length];
		for(int i = 0; i < titresFenetres.length; i++) {
			images[i] = WindowManager.getImage(titresFenetres[i]);
		}
		
		ImagePlus[] imps = VueScin.splitAntPost(images);
		
		if(imps[0] != null) {
			this.impAnt = imps[0].duplicate();
		}
		
		if(imps[1] != null) {
			this.impPost = imps[1].duplicate();
		}
		
		for(int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}

		ImagePlus impProjetee = projeter(this.impPost);
		
		this.frameDurations = buildFrameDurations(this.impPost);
		
		this.setImp(impProjetee);
		
		VueScin.setCustomLut(this.getImp());		
		
		for(String s : titresFenetres) {
			WindowManager.getImage(s).close();
		}
	}
	
	public static ImagePlus projeter(ImagePlus imp) {
		ImagePlus pj = ZProjector.run(imp, "sum");
		pj.setProperty("Info", imp.getInfoProperty());
		return pj;
	}
	
	public static ImagePlus projeter(ImagePlus imp, int startSlice, int stopSlice) {
		ImagePlus pj = ZProjector.run(imp, "sum", startSlice ,stopSlice);
		pj.setProperty("Info", imp.getInfoProperty());
		return pj;
	}

	private static int[] buildFrameDurations(ImagePlus imp) {
		int[] frameDurations = new int[imp.getStackSize()];
		
		int nbPhase = Integer.parseInt(DicomTools.getTag(imp, "0054,0031").trim());
		if (nbPhase == 1) {
			int duration = Integer.parseInt(DicomTools.getTag(imp, "0018,1242").trim());
			for (int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = duration;
			}
		} else {
			String[] phasesStr = DicomTools.getTag(imp, "0054,0030").trim().split(" ");
			int[] phases = new int[phasesStr.length];
			Integer[] durations = VueScinDyn.getDurations(imp);
			for(int i = 0; i < phases.length; i++) {
				phases[i] = Integer.parseInt(phasesStr[i]);
			}
			
			for(int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = durations[phases[i] - 1];
			}
		}
		
		return frameDurations;
	}

	private static Integer[] getDurations(ImagePlus imp) {
		List<Integer> duration = new ArrayList<>();
		String info = imp.getInfoProperty();
		String[] split = info.split("\n");
		for(String s : split) {
			if(s.startsWith("0018,1242")) {
				String[] mots = s.split(" ");
				duration.add(Integer.parseInt(mots[mots.length - 1]));
			}
		}
		return duration.toArray(new Integer[0]);
	}

	public ImagePlus getImpAnt() {
		return this.impAnt;
	}

	public ImagePlus getImpPost() {
		return this.impPost;
	}

	public int[] getFrameDurations() {
		if(this.frameDurations == null) {
			this.frameDurations = buildFrameDurations(this.impPost);
		}
		return this.frameDurations;
	}

}
