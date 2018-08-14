package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.util.DicomTools;

public class DynamicScintigraphy {
	
	/**
	 * Make projection of stacck
	 * @param imp : image to project
	 * @param startSlice : first index slice
	 * @param stopSlice : last index slice
	 * @param type : "avg" or "max"
	 * @return  projected imageplus (of all slice)
	 */
	public static ImagePlus projeter(ImagePlus imp, int startSlice, int stopSlice , String type) {
		ImagePlus pj = ZProjector.run(imp, type, startSlice ,stopSlice);
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

}
