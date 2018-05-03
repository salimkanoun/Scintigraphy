package org.petctviewer.scintigraphy.dynamic;

import java.util.ArrayList;
import java.util.List;
import org.petctviewer.scintigraphy.scin.VueScin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.ZProjector;
import ij.util.DicomTools;

public abstract class Vue_Dynamic extends VueScin {

	private ImagePlus impProjetee, impAnt, impPost;

	private int[] frameDurations;

	public Vue_Dynamic() {
		super("Dynamic scintigraphy");
	}

	@Override
	protected void ouvertureImage(String[] titresFenetres) {
		if (titresFenetres.length > 2) {
			IJ.log("Please open a dicom containing both ant and post or two separated dicoms");
		}

		ImagePlus[] imps = this.splitAntPost(titresFenetres);
		this.impAnt = imps[0];
		this.impPost = imps[1];
		
		for(int i = 1; i <= this.impPost.getStackSize(); i++) {
			this.impPost.getStack().getProcessor(i).flipHorizontal();
		}

		this.impProjetee = ZProjector.run(this.impAnt, "sum");
		this.impProjetee.setProperty("Info", this.impAnt.getInfoProperty());

		this.frameDurations = buildFrameDurations(this.impAnt);		

		this.setImp(this.impProjetee);
		VueScin.setCustomLut(this.getImp());
		
		for(String s : titresFenetres) {
			WindowManager.getImage(s).close();
		}
	}

	private int[] buildFrameDurations(ImagePlus imp) {
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
			Integer[] durations = this.getDurations(imp);
			for(int i = 0; i < phases.length; i++) {
				phases[i] = Integer.parseInt(phasesStr[i]);
			}
			
			for(int i = 0; i < frameDurations.length; i++) {
				frameDurations[i] = durations[phases[i] - 1];
			}
		}
		
		return frameDurations;
	}

	private Integer[] getDurations(ImagePlus imp) {
		List<Integer> duration = new ArrayList<Integer>();
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
		return impAnt;
	}

	public ImagePlus getImpPost() {
		return impPost;
	}
	
	public ImagePlus getImpProjetee() {
		return impProjetee;
	}

	public int[] getFrameDurations() {
		return frameDurations;
	}

}
