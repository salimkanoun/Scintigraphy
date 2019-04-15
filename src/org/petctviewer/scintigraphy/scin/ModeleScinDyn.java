package org.petctviewer.scintigraphy.scin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public abstract class ModeleScinDyn extends ModeleScin {

	private HashMap<String, List<Double>> data;
	private int[] frameduration;
	private boolean locked;
	
	/**
	 * Enregistre et calcule les resultats d'une scintiigraphie dynamique
	 * 
	 * @param frameDuration
	 *            duree de chaque frame en ms
	 */
	public ModeleScinDyn(int[] frameDuration) {
		super(null);
		this.data = new HashMap<>();
		this.frameduration = frameDuration;
	}

	
	/**************** Public *******************/
	//SK METHODE A EVITER POUR DISSOCIER MODELE ET CONTROLER
	/*
	public void enregistrerMesure(String nomRoi, ImagePlus imp) {
		// si le modele n'est pas bloque
		if (!this.isLocked()) {
			//recupere la phrase sans le dernier mot (tag)
			String name = nomRoi.substring(0, nomRoi.lastIndexOf(" "));
			// on cree la liste si elle n'existe pas
			if (this.data.get(name) == null) {
				this.data.put(name, new ArrayList<Double>());
			}
			// on y ajoute le nombre de coups
			this.data.get(name).add(Library_Quantif.getCounts(imp));
		}
	}*/
	
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
		if (l.size() != frameduration.length) {
			throw new IllegalArgumentException("List size does not match duration time");
		}

		XYSeries points = new XYSeries(nom, true);

		Double dureePriseOld = 0.0;
		for (int i = 0; i < l.size(); i++) {
			Double dureePrise = frameduration[i] / (60 * 1000.0); // axes x en minutes

			Double x = (dureePriseOld + dureePrise) - (dureePrise / 2);
			Double y = l.get(i);
			points.add(x, y);

			dureePriseOld += dureePrise;
		}

		return points;
	}
	
	/**
	 * ajuste les valeurs en coups/sec
	 * 
	 * @param values
	 * @return
	 */
	public List<Double> adjustValues(List<Double> values) {
		List<Double> valuesAdjusted = new ArrayList<>();

		for (int i = 0; i < values.size(); i++) {
			// calcul de la prise en secondes
			Double dureePrise = frameduration[i] / 1000.0;
			valuesAdjusted.add(values.get(i) / (dureePrise));
		}

		return valuesAdjusted;
	}

	public boolean isLocked() {
		return locked;
	}
	
	@Override
	public String toString() {
		String s = "\n";
		
		s += "time (s)";
		Double sum = 0.0;
		for(int i = 0; i < this.getFrameduration().length; i++) {
			sum += this.getFrameduration()[i] / 1000;
			s += "," + sum;
		}
		
		return s;
	}


	/**
	 * Renvoie le numero de la slice correspondant au temps passe en parametre (en
	 * ms)
	 * 
	 * @param debut
	 * @return numero de la slice
	 */
	public static int getSliceIndexByTime(double time, int[] frameduration) {

		int summed = 0;
		for (int i = 0; i < frameduration.length; i++) {
			if (time <= summed) {
				return i;
			}
			summed += frameduration[i];
		}

		return 0;
	}

	/************* Getter *************/
	public int getNbRoi() {
		return this.data.size();
	}

	/**
	 * renvoie une serie selon sa cle
	 * 
	 * @param key
	 *            la cle
	 * @return la serie
	 */
	public XYSeries getSerie(String key) {
		List<Double> data = this.getData().get(key);
		if (data == null) {
			throw new IllegalArgumentException("No series with key " + key);
		}
		return this.createSerie(data, key);
	}

	/**
	 * renvoie la liste des series
	 * 
	 * @return liste des series
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
	 * renvoie l'image de la serie en x
	 * 
	 * @param abscisses de la serie
	 * @param x abscisse
	 * @return ordonnee
	 */
	public Double getY(List<Double> points, Double x) {
		XYSeries s = this.createSerie(points, "");
		XYSeriesCollection data = new XYSeriesCollection(s);
		return DatasetUtils.findYValue(data, 0, x);
	}

	public HashMap<String, List<Double>> getData() {
		return this.data;
	}
	
	public void setData(HashMap<String, List<Double>> data) {
		this.data=data;
	}

	public List<Double> getData(String key) {
		return this.data.get(key);
	}

	public int[] getFrameduration() {
		return frameduration;
	}
	
	
	/************** Setter *************/	
	public void setLocked(boolean locked) {
		this.locked = locked;
	}


}
