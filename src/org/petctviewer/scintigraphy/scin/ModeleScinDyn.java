package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.ImagePlus;

public abstract class ModeleScinDyn extends ModeleScin {

	private HashMap<String, List<Double>> data;
	private int[] frameduration;

	private boolean lock = false;

	/**
	 * Enregistre et calcule les resultats d'une scintiigraphie dynamique
	 * @param frameDuration  duree de chaque frame en ms
	 */
	public ModeleScinDyn(int[] frameDuration) {
		this.data = new HashMap<>();
		this.frameduration = frameDuration;
	}

	public int getNbRoi() {
		return this.data.size();
	}

	@Override
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		//si le modele n'est pas bloque
		if (!this.lock) {
			String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));

			// on cree la liste si elle n'existe pas
			if (this.data.get(name) == null) {
				this.data.put(name, new ArrayList<Double>());
			}
			
			//on y ajoute le nombre de coups
			this.data.get(name).add(ModeleScin.getCounts(imp));
		}
	}

	/**
	 * renvoie la liste des series
	 * @return lliste des series
	 */
	public List<XYSeries> getSeries() {
		List<XYSeries> listSeries = new ArrayList<>();

		for (String k : this.data.keySet()) {
			List<Double> data = this.data.get(k);
			listSeries.add(createSerie(data, k));
		}
		
		return listSeries;
	}

	/**
	 * Renvoie la valeur de T1/2 observee
	 * @param series serie a traiter
	 * @param startX depart en x
	 * @return abscisse du point T1/2
	 */
	public static Double getTDemiObs(XYSeries series, Double startX) {
		
		//ordonnee du point Tmax
		int yDemi = (int) (getY(series, startX) / 2);
		
		for (int i = 1; i < series.getItemCount(); i++) {
			if (series.getY(i - 1).doubleValue() >= yDemi && series.getY(i).doubleValue() <= yDemi) {
				Double x = (series.getX(i - 1).doubleValue() + series.getX(i).doubleValue()) / 2;
				if (x >= startX)
					return x;
			}
		}
		return -1.0;
	}

	/**
	 * renvoie une serie avec les ordonnees en coups / sec
	 * 
	 * @param l
	 *            liste de points ajustes
	 * @param nom
	 *            nom de la serie
	 * @return la serie
	 */
	public XYSeries createSerie(List<Double> l, String nom) {
		if(l.size() != frameduration.length) {
			throw new IllegalArgumentException("List size does not match duration time");
		}
		
		XYSeries points = new XYSeries(nom, true);

		Double dureePriseOld = 0.0;
		for (int i = 0; i < l.size(); i++) {
			// en secondes
			Double dureePrise = frameduration[i] / 60000.0;
			points.add(dureePriseOld + dureePrise, l.get(i));
			dureePriseOld += dureePrise;
		}

		return points;
	}

	/**
	 * renvoie la valeur de 'abscisse correspondant a l'ordonnee maximale de la serie
	 * @param series la serie
	 * @return valeur de l'abscisse
	 */
	public static Double getAbsMaxY(XYSeries series) {
		Number maxY = series.getMaxY();
		
		@SuppressWarnings("unchecked")
		List<XYDataItem> items = series.getItems();
		
		for (XYDataItem i : items) {
			if (maxY.equals(i.getY())) {
				return i.getX().doubleValue();
			}
		}
		return null;
	}
	
	/**
	 * renvoie l'image de la serie en x
	 * @param abscisses de la serie 
	 * @param x abscisse
	 * @return ordonnee
	 */
	public Double getY(List<Double> points, Double x) {
		XYSeries s = this.createSerie(points, "");
		XYSeriesCollection data = new XYSeriesCollection(s);
		return DatasetUtils.findYValue(data, 0, x);
	}


	/**
	 * renvoie la valeur de 'abscisse correspondant a l'ordonnee maximale de la serie
	 * @param ds le dataset
	 * @param series numero de la serie
	 * @return valeur de l'abscisse
	 */
	public static double getAbsMaxY(XYDataset ds, int series) {
		Double maxY = 0.0;
		for (int i = 0; i < ds.getItemCount(series); i++) {
			if (ds.getY(series, i).doubleValue() > maxY) {
				maxY = ds.getYValue(series, i);
			}
		}

		for (int i = 0; i < ds.getItemCount(series); i++) {
			if (maxY.equals(ds.getY(series, i))) {
				return ds.getXValue(series, i);
			}
		}

		return 0.0;
	}

	/**
	 * renvoie la liste des ordonnees de la serie passee en parametre
	 * @param la serie
	 * @return liste des ordonnees
	 */
	public List<Double> seriesToList(XYSeries s) {
		ArrayList<Double> l = new ArrayList<>();
		for (int i = 0; i < s.getItemCount(); i++) {
			l.add(s.getY(i).doubleValue());
		}
		return l;
	}

	/**
	 * ajuste les valeurs en coups/sec
	 * @param values
	 * @return
	 */
	public List<Double> adjustValues(List<Double> values) {
		List<Double> valuesAdjusted = new ArrayList<>();

		for (int i = 0; i < values.size(); i++) {
			Double dureePrise = frameduration[i] / 60000.0;
			valuesAdjusted.add(values.get(i) / (dureePrise * 60));
		}

		return valuesAdjusted;
	}
	
	/**
	 * Renvoie le numero de la slice correspondant au temps passe en parametre (en ms)
	 * @param debut
	 * @return numero de la slice
	 */
	public static int getSliceIndexByTime(double time, int[] frameduration) {
		
		int summed = 0;
		for (int i = 0; i < frameduration.length; i++) {
			if(time <= summed) {
				return i;
			}
			summed += frameduration[i];
		}
		
		return 0;

	}

	@Override
	public String toString() {
		String s = "\n";
		for (String k : this.data.keySet()) {
			s += k;
			for (Double d : this.data.get(k)) {
				s += "," + d;
			}
			s += "\n";
		}
		return s;
	}

	public HashMap<String, List<Double>> getData() {
		return this.data;
	}

	public List<Double> getData(String key) {
		return this.data.get(key);
	}

	public void lock() {
		this.lock = true;
	}

	public void unlock() {
		this.lock = false;
	}

	public boolean isLocked() {
		return this.lock;
	}
	
	public int[] getFrameduration() {
		return frameduration;
	}

}
