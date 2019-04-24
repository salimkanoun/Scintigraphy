package org.petctviewer.scintigraphy.scin.library;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;

public class Library_Quantif {
	
	
	/**
	 * Enumeration of isotopes, with the associated halflife
	 * Current isotope :
	 * 
	 * <p>
	 *	<table>
	 * 		<tr><th>Isotope</th><th>Half Life</th></tr>
	 * 		<tr><td>RADIUM_111</td>	<td>242330000</td></tr>
	 * 		<tr><td>TECHNETIUM_99</td>	<td>21620880</td></tr>
	 * 		<tr><td>CHROME_51</td>	<td>2393500000</td></tr>
	 *	</table>
	 * </p>
	 */
	public enum Isotope {
			INDIUM_111(242330000l), TECHNICIUM_99(21620880l), CHROME_51(2393500000l);
			
			private long halfLifeMS;
	
			private Isotope(long isotope) {
				this.halfLifeMS = isotope;
			}
			
			public long getHalLifeMS() {
				return this.halfLifeMS;
			}
		}
	


	/**
	 * arrondi la valeur
	 * 
	 * @param value
	 *            valeur a arrondir
	 * @param places
	 *            nb de chiffre apres la virgule
	 * @return valeur arrondie
	 */
	public static double round(Double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException("place doit etre superieur ou egal a zero");
		}
		
		if(value.equals(Double.NaN) || value .equals(Double.NEGATIVE_INFINITY) || value.equals(Double.POSITIVE_INFINITY)) {
			return value;
		}
	
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/********** Public Static  ********/
	
	/**
	 * renvoie la moyenne geometrique
	 * 
	 * @param a
	 *            chiffre a
	 * @param b
	 *            chiffre b
	 * @return moyenne geometrique
	 */
	public static double moyGeom(Double a, Double b) {
		double moyGeom;
		if(a*b>0) {
			moyGeom=Math.sqrt(a * b);
		}else {
			moyGeom=0;
		}
		
		return moyGeom;
	}

	/********** Public Static Getter ********/
	
	/**
	 * Renvoie le nombre de coups sur la roi presente dans l'image plus
	 * 
	 * @param imp
	 *            l'imp
	 * @return nombre de coups
	 */
	public static Double getCounts(ImagePlus imp) {
		Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
		Analyzer analyser = new Analyzer(imp);
		analyser.measure();
		ResultsTable density = Analyzer.getResultsTable();
		return density.getValueAsDouble(ResultsTable.RAW_INTEGRATED_DENSITY, 0);
	}

	/**
	 * renvoie le nombre de coups moyens de la roi presente sur l'imp
	 * 
	 * @param imp
	 *            l'imp
	 * @return nombre moyen de coups
	 */
	public static Double getAvgCounts(ImagePlus imp) {
		return imp.getStatistics().mean;
	}

	public static int getPixelNumber(ImagePlus imp) {
		int area = imp.getStatistics().pixelCount;
		return area;
	}
	
	
	public static Double getCountCorrectedBackground(ImagePlus imp, Roi roi, Roi background) {
		
		imp.setRoi(background);
		Double meanCountBackground = Library_Quantif.getAvgCounts(imp);	
		imp.setRoi(roi);
		return Library_Quantif.getCounts(imp) - (meanCountBackground * imp.getStatistics().pixelCount);
	}
	
	/**
	 * Calcul les coups corriges par rapport a heure d'injection et ajoute la valeur corrig�e dans les objets mesures
	 * @param mesureCollection
	 * @param injectionDate
	 */
	public static double calculer_countCorrected(int delayMs, double mesuredCount, Isotope isotope) {
		
		double decayedFraction=Math.pow(Math.E, ((Math.log(2)/isotope.getHalLifeMS())*delayMs*(-1)));
		double correctedCount=mesuredCount/(decayedFraction);
		
		return correctedCount;
	}
	
	
	/**
	 * Return the corrected counts of the second image.
	 * @param firstImage
	 *             Image of the first acquisition, from where we take our reference time
	 * @param secondImage
	 *             Image on which we want to correct counts
	 * @param isotope
	 *            Referencing the isotope used for the correction ({@link Isotope})
	 */
	public static double calculer_countCorrected(ImagePlus firstImage,ImagePlus secondImage, Isotope isotope) {
		Date firstAcquisitionTime = Library_Dicom.getDateAcquisition(firstImage);
		Date SecondAcquisitionTime = Library_Dicom.getDateAcquisition(secondImage);
		System.out.println("Difference de temps : "+(int) (firstAcquisitionTime.getTime() - SecondAcquisitionTime.getTime())/1000);
		return Library_Quantif.calculer_countCorrected((int) (firstAcquisitionTime.getTime() - SecondAcquisitionTime.getTime())/1000,Library_Quantif.getCounts(secondImage),isotope);
	}
	
	
}
