package org.petctviewer.scintigraphy.scin.library;

import java.util.Comparator;
import java.util.Date;

import org.petctviewer.scintigraphy.scin.ImageSelection;

import ij.ImagePlus;

/**
 * Compares two ImageSelection by their acquisition time. This is used to sort
 * images from the newest acquisition to the oldest acquisition.
 * 
 * @author Titouan QUÉMA
 *
 */
public class ReversedChronologicalAcquisitionComparator implements Comparator<ImageSelection> {
	@Override
	public int compare(ImageSelection imp1, ImageSelection imp2) {
		Date d1 = Library_Dicom.getDateAcquisition(imp1.getImagePlus());
		Date d2 = Library_Dicom.getDateAcquisition(imp2.getImagePlus());
		return d2.compareTo(d1);
	}

	public static class ImagePlusComparator implements Comparator<ImagePlus> {
		@Override
		public int compare(ImagePlus imp1, ImagePlus imp2) {
			Date d1 = Library_Dicom.getDateAcquisition(imp1);
			Date d2 = Library_Dicom.getDateAcquisition(imp2);
			return d2.compareTo(d1);
		}
	}
}