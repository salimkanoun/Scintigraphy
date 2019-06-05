package org.petctviewer.scintigraphy.scin.library;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.MathArrays;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Library_Quantif {

	/**
	 * arrondi la valeur
	 *
	 * @param value  valeur a arrondir
	 * @param places nb de chiffre apres la virgule
	 * @return valeur arrondie
	 */
	public static double round(Double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException("place doit etre superieur ou egal a zero");
		}

		if (value.equals(Double.NaN) || value.equals(Double.NEGATIVE_INFINITY) || value.equals(
				Double.POSITIVE_INFINITY)) {
			return value;
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * renvoie la moyenne geometrique
	 *
	 * @param a chiffre a
	 * @param b chiffre b
	 * @return moyenne geometrique
	 */
	public static double moyGeom(Double a, Double b) {
		double moyGeom;
		if (a * b > 0) {
			moyGeom = Math.sqrt(a * b);
		} else {
			moyGeom = 0;
		}

		return moyGeom;
	}

	/**
	 * Renvoie le nombre de coups sur la roi presente dans l'image plus
	 *
	 * @param imp l'imp
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
	 * @param imp l'imp
	 * @return nombre moyen de coups
	 */
	public static Double getAvgCounts(ImagePlus imp) {
		return imp.getStatistics().mean;
	}

	public static int getPixelNumber(ImagePlus imp) {
		return imp.getStatistics().pixelCount;
	}

	/**
	 * Calculate the counts/pixels mean of the Background , and subtract to the Roi
	 * count this mean applied to the Roi pixels
	 *
	 * @param imp        ImagePlus to apply the Roi and Background
	 * @param roi        Roi to correct
	 * @param background Roi used to correct
	 * @return The Roi count corrected
	 */
	public static Double getCountCorrectedBackground(ImagePlus imp, Roi roi, Roi background) {
		imp.setRoi(background);
		Double meanCountBackground = Library_Quantif.getAvgCounts(imp);
		imp.setRoi(roi);
		return Library_Quantif.getCounts(imp) - (meanCountBackground * imp.getStatistics().pixelCount);
	}

	/**
	 * Subtract to the Roi count the background counts/pixels mean applied to the
	 * Roi pixels
	 *
	 * @param imp                 ImagePlus to apply the Roi and Background
	 * @param roi                 Roi to correct
	 * @param meanCountBackground The mean count to use
	 * @return The Roi count corrected
	 */
	public static Double getCountCorrectedBackground(ImagePlus imp, Roi roi, double meanCountBackground) {
		imp.setRoi(roi);
		return Library_Quantif.getCounts(imp) - (meanCountBackground * imp.getStatistics().pixelCount);
	}

	/**
	 * Returns the corrected counts of the radioactive decay
	 *
	 * @param delayMs      Delay between the 2 images, in miliseconds
	 * @param mesuredCount Current count of the image
	 * @param isotope      Isotope used in this exam ({@link Isotope})
	 * @return The corrected count
	 */
	public static double calculer_countCorrected(int delayMs, double mesuredCount, Isotope isotope) {

		double decayedFraction = Math.pow(Math.E, ((Math.log(2) / isotope.getHalLifeMS()) * delayMs * (-1)));

		return mesuredCount / (decayedFraction);
	}

	/**
	 * Return the corrected counts of the second image.
	 *
	 * @param firstImage  Image of the first acquisition, used to take our reference
	 *                    time
	 * @param secondImage Image on which we want to correct counts
	 * @param isotope     Referencing the isotope used for the correction
	 *                    ({@link Isotope})
	 * @return The corrected count
	 */
	public static double calculer_countCorrected(ImagePlus firstImage, ImagePlus secondImage, Isotope isotope) {
		Date firstAcquisitionTime = Library_Dicom.getDateAcquisition(firstImage);
		Date SecondAcquisitionTime = Library_Dicom.getDateAcquisition(secondImage);
		return Library_Quantif
				.calculer_countCorrected((int) (firstAcquisitionTime.getTime() - SecondAcquisitionTime.getTime()),
						Library_Quantif.getCounts(secondImage), isotope);
	}

	/**
	 * Returns the counts with th radioactive decay applied.
	 *
	 * @param delayMs      between the 2 images, in miliseconds
	 * @param mesuredCount Current count of the image
	 * @param isotope      Isotope used in this exam ({@link Isotope})
	 * @return The corrected count
	 */
	public static double applyDecayFraction(int delayMs, double mesuredCount, Isotope isotope) {

		double decayedFraction = Math.pow(Math.E, ((Math.log(2) / isotope.getHalLifeMS()) * delayMs * (-1)));

		return mesuredCount * (decayedFraction);
	}

	/**
	 * Returns the counts of the second Image, with the radioactive decay applied.
	 *
	 * @param firstImage  Image representing the time delay
	 * @param secondImage Image to apply decay
	 * @param isotope     Isotope used
	 * @return Count of the second image, with decay applied
	 */
	public static double applyDecayFraction(ImagePlus firstImage, ImagePlus secondImage, Isotope isotope) {
		Date firstAcquisitionTime = Library_Dicom.getDateAcquisition(firstImage);
		Date SecondAcquisitionTime = Library_Dicom.getDateAcquisition(secondImage);

		return Library_Quantif.applyDecayFraction(
				Math.abs((int) (firstAcquisitionTime.getTime() - SecondAcquisitionTime.getTime())),
				Library_Quantif.getCounts(secondImage), isotope);
	}

	/**
	 * Convolve n times an array of double, using a kernel.
	 *
	 * @param values    The array ou double to convolve
	 * @param kernel    The kernel used in the convolution
	 * @param nbConvolv The number of convolution to apply
	 * @return The convolved array
	 */
	public static double[] processNConvolv(double[] values, double[] kernel, int nbConvolv) {

		List<double[]> list = new ArrayList<>();

		list.add(MathArrays.convolve(values, kernel));

		for (int i = 0; i < nbConvolv - 1; i++) {
			list.add(MathArrays.convolve(list.get(i), kernel));
		}

		double[] result = new double[values.length];

		for (int i = 0; i < list.get(list.size() - 1).length - nbConvolv * (kernel.length - 1); i++) {
			result[i] = list.get(list.size() - 1)[i + (nbConvolv * (kernel.length - 1) / 2)];
		}

		return result;
	}

	/**
	 * Convolve n times an array of double, using a kernel.
	 *
	 * @param values    The array ou double to convolve
	 * @param kernel    The kernel used in the convolution
	 * @param nbConvolv The number of convolution to apply
	 * @return The convolved array
	 */
	public static Double[] processNConvolv(Double[] values, Double[] kernel, int nbConvolv) {
		return ArrayUtils.toObject(
				processNConvolv(ArrayUtils.toPrimitive(values), ArrayUtils.toPrimitive(kernel), nbConvolv));
	}

	/**
	 * Convolve n times an array of double, using a kernel.
	 *
	 * @param values    The array ou double to convolve
	 * @param kernel    The kernel used in the convolution
	 * @param nbConvolv The number of convolution to apply
	 * @return The convolved array
	 */
	public static Double[] processNConvolv(List<Double> values, Double[] kernel, int nbConvolv) {
		return processNConvolv(values.toArray(new Double[0]), kernel, nbConvolv);
	}

	/**
	 * Create the deconvolution of the liver by the blood pool.
	 *
	 * @param values    The array ou double to convolve
	 * @param kernel    The kernel used in the convolution
	 * @param nbConvolv The number of convolution to apply
	 * @return The convolved array
	 * @deprecated => Work when you used convolved array. =======
	 * <p>
	 * /** Convolve n times an array of double, using a kernel.
	 */
	public static List<Double> processNConvolv(List<Double> values, List<Double> kernel, int nbConvolv) {
		return Arrays.asList(processNConvolv(values.toArray(new Double[0]), kernel.toArray(new Double[0]), nbConvolv));
	}

	/**
	 * Create the deconvolution of the liver by the blood pool.
	 *
	 * @deprecated => Work when you used convolved array (actually, working with a
	 * 6times convolved array) See
	 * {@link org.petctviewer.scintigraphy.hepatic.tab.TabDeconvolv}
	 * or {@link org.petctviewer.scintigraphy.renal.gui.TabDeconvolve}.
	 */
	public static List<Double> deconvolv(Double[] blood, Double[] liver, int init) {

//		System.out.println("\n\nConvolved Blood Pool");
//		System.out.println(Arrays.asList(blood));
//		System.out.println();
//		System.out.println("Convolved liver");
//		System.out.println(Arrays.asList(liver));
//		System.out.println("\n\n");

		List<Double> h = new ArrayList<>();

		for (int i = 0; i < init; i++) {
			h.add(0.0d);
		}

		for (int i = init; i < blood.length; i++) {

			double somme = 0;

			for (int j = init; j < i; j++) {
				somme += (i - j + 1) * (h.get(j));
			}

			// SK REMPLACER 1 PAR LA VALEUR DE TEMPS DE LA FRAME !, ou mettre les valeurs en
			// coups/sec
			double result2 = (1.0D / (blood[init])) * (liver[i] - somme);

			h.add(result2);

		}
		return h;
	}

	/**
	 * Create the deconvolution of the liver by the blood pool.
	 *
	 * @deprecated => Work when you used convolved array (actually, working with a
	 * 6times convolved array) See
	 * {@link org.petctviewer.scintigraphy.hepatic.tab.TabDeconvolv}
	 * or {@link org.petctviewer.scintigraphy.renal.gui.TabDeconvolve}.
	 */
	public static List<Double> deconvolv(List<Double> blood, List<Double> liver, int init) {
		return deconvolv(blood.toArray(new Double[0]), liver.toArray(new Double[0]), init);
	}

	/**
	 * Calculates the time between the specified times.
	 *
	 * @param time0 First time
	 * @param time  Time to calculate the difference with
	 * @return difference of time expressed in minutes (negative value if the
	 * specified time is before the ingestion's time)
	 */
	public static double calculateDeltaTime(Date time0, Date time) {
		return (time.getTime() - time0.getTime()) / 1000. / 60.;
	}

	/**
	 * Enumeration of isotopes, with the associated half-life.<br>
	 * Each isotope is represented by a code in the DICOM file. See
	 * <a href="http://dicom.nema.org/medical/Dicom/2015b/output/chtml/part16/sect_CID_18.html">http://dicom.nema
	 * .org/medical/Dicom/2015b/output/chtml/part16/sect_CID_18.html</a>.
	 * Current isotope :
	 * <table border=1 >
	 * <tr>
	 * <th>Isotope</th>
	 * <th>&nbsp; Half Life (miliseconds) &nbsp;</th>
	 * </tr>
	 * <tr align=center>
	 * <td>INDIUM_111</td>
	 * <td>242330000</td>
	 * </tr>
	 * <tr align=center>
	 * <td>&nbsp; TECHNETIUM_99 &nbsp;</td>
	 * <td>21620880</td>
	 * </tr>
	 * <tr align=center>
	 * <td>CHROME_51</td>
	 * <td>2393500000</td>
	 * </tr>
	 * </table>
	 * </p>
	 */
	public enum Isotope {
		INDIUM_111(242330000L, "C-145A4"), TECHNETIUM_99(21620880L, "C-163A8"), CHROME_51(2393500000L, "C-129A2");

		private final long halfLifeMS;
		private final String code;

		Isotope(long isotope, String code) {
			this.halfLifeMS = isotope;
			this.code = code;
		}

		public static Isotope getIsotopeFromCode(String code) {
			for (Isotope i : Isotope.values())
				if (i.code.equals(code)) return i;
			return null;
		}

		public String getCode() {
			return this.code;
		}

		public long getHalLifeMS() {
			return this.halfLifeMS;
		}


		@Override
		public String toString() {
			switch (this) {
				case INDIUM_111:
					return "Indium (111)";
				case TECHNETIUM_99:
					return "Technetium (99)";
				case CHROME_51:
					return "Chrome (51)";
			}
			return super.toString();
		}
	}

}
