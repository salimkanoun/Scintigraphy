package org.petctviewer.scintigraphy.scin.library;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.Modele_Renal;

public class Library_JFreeChart {

	/**
	 * renvoie l'image de la serie en x
	 * 
	 * @param series
	 *            serie a traite
	 * @param x
	 *            abscisse
	 * @return ordonnee
	 */
	public static Double getY(XYSeries series, double x) {
		XYSeriesCollection data = new XYSeriesCollection(series);
		return DatasetUtils.findYValue(data, 0, x);
	}

	/**
	 * renvoie la valeur de l'abscisse correspondant a l'ordonnee maximale de la
	 * serie
	 * 
	 * @param series
	 *            la serie
	 * @return valeur de l'abscisse
	 */
	public static Double getAbsMaxY(XYSeries series) {
		Number maxY = series.getMaxY();
		
		List<XYDataItem> items = series.getItems();
	
		for (XYDataItem i : items) {
			if (maxY.equals(i.getY())) {
				return i.getX().doubleValue();
			}
		}
		return null;
	}

	/**
	 * renvoie la valeur de l'abscisse correspondant a l'ordonnee maximale de la
	 * serie
	 * 
	 * @param ds
	 *            le dataset
	 * @param series
	 *            numero de la serie
	 * @return valeur de l'abscisse
	 */
	public static double getAbsMaxY(XYDataset ds, int series) {
		//on recupere la serie demandée dans un XYCollection (casté depuis un XYDataset)
		return getAbsMaxY( 
				((XYSeriesCollection)ds).getSeries(series) 
				);
	}

	/*
	 * return the linearaly interpolated X for a given Y value 
	 */
	public static Double getInterpolatedX (XYSeries serie, Double pointYRecherche ) {
		
		for(int i =0; i< serie.getItemCount(); i++) {
			//si on a deja le point
			if(serie.getY(i) == pointYRecherche) {
				return (Double)serie.getX(i);
			}
			
			//si on a pas le point on fait un feat entre le point juste avant de dépacer et celui apres
			if((Double)serie.getY(i) > pointYRecherche) {
				double[][] m = new double[2][2];
				
				m[0][0] = (Double)serie.getX(i-1);
				m[0][1] = (Double)serie.getY(i-1);
				m[1][0] = (Double)serie.getX(i);
				m[1][1] = (Double)serie.getY(i);
				
				double fit []  = Regression.getOLSRegression(m);
				
				return (fit[0]+fit[1]*pointYRecherche);
			}
		}
		
		return Double.NaN;
	}

	/*
	 * return the linearaly interpolated Y for a given X value 
	 */
	public static Double getInterpolatedY (XYSeries serie, Double pointXRecherche ) {
		
		for(int i =0; i< serie.getItemCount(); i++) {
			//si on a deja le point
			if(serie.getX(i) == pointXRecherche) {
				return (Double)serie.getY(i);
			}
			
			//si on a pas le point on fait un feat entre le point juste avant de dépacer et celui apres
			if((Double)serie.getX(i) > pointXRecherche) {
				double[][] m = new double[2][2];
				
				m[0][0] = (Double)serie.getX(i-1);
				m[0][1] = (Double)serie.getY(i-1);
				m[1][0] = (Double)serie.getX(i);
				m[1][1] = (Double)serie.getY(i);
				
				double fit []  = Regression.getOLSRegression(m);
				
				return (fit[0]+fit[1]*pointXRecherche);
			}
		}
		
		return Double.NaN;
	}

	/************* Public Static Getter ********/
	/**
	 * Renvoie la valeur de T1/2 observee
	 * 
	 * @param series
	 *            serie a traiter
	 * @param startX
	 *            depart en x
	 * @return abscisse du point T1/2
	 */
	public static Double getTDemiObs(XYSeries series, Double startX) {
	
		// ordonnee du point Tmax
		int yDemi = (int) (getY(series, startX) / 2);
	
		for (int i = 1; i < series.getItemCount(); i++) {
			if (series.getY(i - 1).doubleValue() >= yDemi && series.getY(i).doubleValue() <= yDemi) {
				Double x = (series.getX(i - 1).doubleValue() + series.getX(i).doubleValue()) / 2;
				if (x >= startX)
					return x;
			}
		}
		return Double.NaN;
	}

	// renvoie l'aire sous la courbe entre les points startX et endX
	public static List<Double> getIntegralSummed(XYSeries series, Double startX, Double endX) {
	
		List<Double> integrale = new ArrayList<>();
	
		// on recupere les points de l'intervalle voulu
		XYSeries croppedSeries = Modele_Renal.cropSeries(series, startX, endX);
	
		// on calcule les aires sous la courbe entre chaque paire de points
		Double airePt1 = croppedSeries.getX(0).doubleValue() * croppedSeries.getY(0).doubleValue() / 2;
		integrale.add(airePt1);
		for (int i = 0; i < croppedSeries.getItemCount() - 1; i++) {
			Double aire = ((croppedSeries.getX(i + 1).doubleValue() - croppedSeries.getX(i).doubleValue())
					* (croppedSeries.getY(i).doubleValue() + croppedSeries.getY(i + 1).doubleValue())) / 2;
			integrale.add(aire);
		}
	
		// on en deduit l'integrale
		List<Double> integraleSum = new ArrayList<>();
		integraleSum.add(integrale.get(0));
		for (int i = 1; i < integrale.size(); i++) {
			integraleSum.add(integraleSum.get(i - 1) + integrale.get(i));
		}
	
		return integraleSum;
	}

	/**
	 * renvoie la liste des ordonnees de la serie passee en parametre
	 * 
	 * @param la
	 *            serie
	 * @return liste des ordonnees
	 */
	public static List<Double> seriesToList(XYSeries s) {
		ArrayList<Double> l = new ArrayList<>();
		for (int i = 0; i < s.getItemCount(); i++) {
			l.add(s.getY(i).doubleValue());
		}
		return l;
	}

	/************* Public Static *********/
		public static ChartPanel associateSeries(String[] asso, List<XYSeries> series) {
			XYSeriesCollection dataset = new XYSeriesCollection();
	
			for (String j : asso) { // pour chaque cle de l'association
				for (int k = 0; k < series.size(); k++) { // pour chaque element de la serie
					// si la cle correspond, on l'ajout au dataset
					if (series.get(k).getKey().equals(j)) {
						dataset.addSeries(series.get(k));
					}
				}
			}
	
			// on cree un jfreehart avec lle datasert precedemment construit
			JFreeChart xylineChart = ChartFactory.createXYLineChart("", "min", "counts/sec", dataset,
					PlotOrientation.VERTICAL, true, true, true);
	
			final XYPlot plot = xylineChart.getXYPlot();
	
			// on masque les marqueurs des points
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			for (int c = 0; c < dataset.getSeriesCount(); c++) {
				renderer.setSeriesShapesVisible(c, false);
			}
			plot.setRenderer(renderer);
	
			ChartPanel c = new ChartPanel(xylineChart);
	
			// on desactive le fond
			c.getChart().getPlot().setBackgroundPaint(null);
			return c;
	}

	// recupere les valeurs situees entre startX et endX
	public static XYSeries cropSeries(XYSeries series, Double startX, Double endX) {
		XYSeries cropped = new XYSeries(series.getKey() + " cropped");
		for (int i = 0; i < series.getItemCount(); i++) {
			if (series.getX(i).doubleValue() >= startX && series.getX(i).doubleValue() <= endX) {
				cropped.add(series.getX(i), series.getY(i));
			}
		}
		return cropped;
	}

	// recupere les valeurs situees entre startX et endX
	public static XYDataset cropDataset(XYDataset data, Double startX, Double endX) {
		XYSeriesCollection dataset = new XYSeriesCollection();
	
		for (int i = 0; i < data.getSeriesCount(); i++) {
			XYSeries series = new XYSeries("" + i);
			for (int j = 0; j < data.getItemCount(0); j++) {
				series.add(data.getX(i, j), data.getY(i, j));
			}
			dataset.addSeries(cropSeries(series, startX, endX));
		}
	
		return dataset;
	}

	//TODO renvoyer un seul chartpanel, passer un tableau de string a une dimension
	/**
	 * renvoie des chartPanels avec les series associees
	 * 
	 * @param asso
	 *            association des series selon leur cle ( ex : {{"S1", "S2"}, {"S1",
	 *            "S3"}} )
	 * @param series
	 *            liste des series
	 * @return chartPanels avec association
	 */
	public static ChartPanel[] associateSeries(String[][] asso, List<XYSeries> series) {
		ArrayList<ChartPanel> cPanels = new ArrayList<>();
	
		// pour chaque association
		for (String[] i : asso) {
			if (i.length > 0) {
				cPanels.add(associateSeries(i, series));
			}
		}
	
		return cPanels.toArray(new ChartPanel[0]);
	}

}