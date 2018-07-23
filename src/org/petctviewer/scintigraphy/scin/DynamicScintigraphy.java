package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.util.DicomTools;

public abstract class DynamicScintigraphy extends Scintigraphy {

	protected ImagePlus impAnt, impPost, impProjetee, impProjeteeAnt;

	private int[] frameDurations;

	public DynamicScintigraphy(String title) {
		super(title);
	}

	@Override
	protected ImagePlus preparerImp(ImagePlus[] images) {
		if (images.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}
		
		ImagePlus[] imps = Scintigraphy.sortAntPost(images);
		if(imps[0] != null) {
			this.impAnt = imps[0].duplicate();
		}
		
		if(imps[1] != null) {
			this.impPost = imps[1].duplicate();
			for(int i = 1; i <= this.impPost.getStackSize(); i++) {
				this.impPost.getStack().getProcessor(i).flipHorizontal();
			}
		}
		
		if( this.impAnt !=null ) {
			impProjeteeAnt = projeter(this.impAnt);
			impProjetee=impProjeteeAnt;
			this.frameDurations = buildFrameDurations(this.impAnt);
		}
		if ( this.impPost !=null ) {
			impProjetee = projeter(this.impPost);
			this.frameDurations = buildFrameDurations(this.impPost);
		}

		return impProjetee.duplicate();
	}
	
	public static ImagePlus projeter(ImagePlus imp) {
		ImagePlus pj = ZProjector.run(imp, "avg");
		pj.setProperty("Info", imp.getInfoProperty());
		return pj;
	}
	
	public static ImagePlus projeter(ImagePlus imp, int startSlice, int stopSlice) {
		ImagePlus pj = ZProjector.run(imp, "avg", startSlice ,stopSlice);
		pj.setProperty("Info", imp.getInfoProperty());
		return pj;
	}

	public static int[] buildFrameDurations(ImagePlus imp) {
		int[] frameDurations = new int[imp.getStackSize()];
		int nbPhase;
		System.out.println(DicomTools.getTag(imp, "0054,0031"));
		if(DicomTools.getTag(imp, "0054,0031") != null) {
			nbPhase = Integer.parseInt(DicomTools.getTag(imp, "0054,0031").trim());
		}
		else nbPhase=1;
		
		
		if (nbPhase == 1) {
			int duration = Integer.parseInt(Scintigraphy.getFrameDuration(imp));
			for (int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = duration;
			}
		} else {
			String[] phasesStr = DicomTools.getTag(imp, "0054,0030").trim().split(" ");
			int[] phases = new int[phasesStr.length];
			Integer[] durations = DynamicScintigraphy.getDurations(imp);
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
