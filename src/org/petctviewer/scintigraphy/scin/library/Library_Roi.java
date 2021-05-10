package org.petctviewer.scintigraphy.scin.library;

import java.awt.Color;
import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

public class Library_Roi {

	public static final int HEART = 0, INFLAT = 1, KIDNEY = 2, INFLATGAUCHE = 3, INFLATDROIT = 4;

	/**
	 * Return the first ROI with the given name (roiName) stored in the RoiManager
	 * @param rm
	 * @param roiName
	 * @return
	 */
	public static Roi getRoiByName(RoiManager rm, String roiName) {
		Roi res = null;
		for (Roi r: rm.getRoisAsArray()) {
			if (r.getName().equals(roiName)) {
				res = r;
				break;
			}
		}

		return res;
	}

	/** cree la roi de bruit de fond
	 *
	 * @param roi
	 * @param imp
	 * @param organ
	 * @return
	 */
	public static Roi createBkgRoi(Roi roi, ImagePlus imp, int organ) {
		Roi bkg = null;
		RoiManager rm = new RoiManager(true);

		switch (organ) {
		case Library_Roi.KIDNEY:
			// largeur a prendre autour du rein
			int largeurBkg = 1;
			if (imp.getDimensions()[0] >= 128) {
				largeurBkg = 2;
			}

			rm.addRoi(roi);

			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());

			rm.select(rm.getCount() - 1);
			IJ.run(imp, "Enlarge...", "enlarge=" + largeurBkg + " pixel");
			rm.addRoi(imp.getRoi());

			rm.setSelectedIndexes(new int[] { rm.getCount() - 2, rm.getCount() - 1 });
			rm.runCommand(imp, "XOR");

			bkg = imp.getRoi();
			break;

		case Library_Roi.HEART:
			// TODO
			break;

		case Library_Roi.INFLATGAUCHE:
			bkg = Library_Roi.createBkgInfLat(roi, imp, -1, rm);
			break;

		case Library_Roi.INFLATDROIT:
			bkg = Library_Roi.createBkgInfLat(roi, imp, 1, rm);
			break;

		default:
			bkg = roi;
			break;
		}

		rm.dispose();

		bkg.setStrokeColor(Color.GRAY);
		return bkg;
	}

	/***************************** Private Static ************************/
	static Roi createBkgInfLat(Roi roi, ImagePlus imp, int xOffset, RoiManager rm) {
		// on recupere ses bounds
		Rectangle bounds = roi.getBounds();

		Roi liver = (Roi) roi.clone();
		rm.addRoi(liver);

		int[] size = {(bounds.width / 4) * xOffset, (bounds.height / 4)};

		Roi liverShift = (Roi) roi.clone();
		liverShift.setLocation(liverShift.getXBase() + size[0], liverShift.getYBase() + size[1]);
		rm.addRoi(liverShift);

		// renvoi une section de la roi
		rm.setSelectedIndexes(new int[] { 0, 1 });
		rm.runCommand(imp, "XOR");
		rm.runCommand(imp, "Split");

		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;
		int w = size[0] * imp.getWidth();
		int h = size[1] * imp.getHeight();

		// permet de diviser la roi
		Rectangle splitter;

		if (w > 0) {
			splitter = new Rectangle(x, y, w, h);
		} else {
			splitter = new Rectangle(x + w, y, -w, h);
		}

		Roi rect = new Roi(splitter);
		rm.addRoi(rect);

		rm.setSelectedIndexes(new int[] { rm.getCount() - 1, rm.getCount() - 2 });
		rm.runCommand(imp, "AND");

		Roi bkg = (Roi) imp.getRoi().clone();
		// int[] offset = new int[] { size[0] / 4, size[1] / 4 };

		// on deplace la roi pour ne pas qu'elle soit collee
		bkg.setLocation(bkg.getXBase() + xOffset, bkg.getYBase() + 1);

		return bkg;
	}

}
