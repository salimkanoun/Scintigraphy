package org.petctviewer.scintigraphy.scin.library;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;

public class Library_Quantif {

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
	
	
}
