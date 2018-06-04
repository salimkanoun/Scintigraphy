package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;

public class SelectorListener implements ChartMouseListener {
	// ordre de priorite des selecteurs
	private List<ValueSelector> listenersPriority;

	private ChartPanel chartPanel;
	private ValueSelector current;
	private HashMap<Comparable, ValueSelector> listeners;

	private List<Area> areas;

	/**
	 * permet de gerer plusieurs selecteurs sur le meme chartPanel en conservant
	 * l'ordre en Z
	 * 
	 * @param chartPanel
	 */
	public SelectorListener(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
		this.listenersPriority = new ArrayList<>();
		this.listeners = new HashMap<Comparable, ValueSelector>();
		this.areas = new ArrayList<Area>();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// curseur par defaut
		Component c = (Component) event.getTrigger().getSource();
		c.setCursor(null);

		// si on avait un selecteur selectionne, on le deselectionne
		if (this.current != null) {
			this.current = null;
		}

		// on recupere le selecteur clique
		int xMouse = (int) event.getTrigger().getPoint().getX();
		Rectangle2D plotArea = this.chartPanel.getScreenDataArea();
		ValueSelector v = this.getSelector(xMouse, plotArea);

		// si il y a un selecteur sous la souris
		if (v != null) {
			// on active son clic et on le selectionne
			v.chartMouseClicked(event);
			this.current = v;

			// s'assure que le dernier marqueur clique est le premier dans la liste afin
			// qu'il prenne le focus
			this.listenersPriority.remove(v);
			this.listenersPriority.add(0, v);
		}
	}

	public ValueSelector getSelector(Comparable key) {
		return this.listeners.get(key);
	}

	private ValueSelector getSelector(int xMouse, Rectangle2D plotArea) {
		// marge a gauche et a droite du selecteur permettant le clic
		int marge = 5;

		XYPlot plot = this.chartPanel.getChart().getXYPlot();

		// pour chaque selecteur en respectant l'ordre de priorite
		for (ValueSelector v : this.listenersPriority) {
			// on converti l'abscisse du selecteur sur le tableau en abscisse sur la fenetre
			int xJava2D = (int) plot.getDomainAxis().valueToJava2D(v.getXValue(), plotArea, plot.getDomainAxisEdge());
			// si il y a un selecteur la ou on a clique, on le renvoie
			if (xJava2D > xMouse - marge && xJava2D < xMouse + marge) {
				return v;
			}
		}
		return null;
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		Component c = (Component) event.getTrigger().getSource();

		updateAreas();

		// on recupere le selecteur sous la souris
		int xMouse = (int) event.getTrigger().getPoint().getX();
		Rectangle2D plotArea = this.chartPanel.getScreenDataArea();
		ValueSelector v = this.getSelector(xMouse, plotArea);

		// si la souris est sur un selecteur ou qu'un selecteur est selectionne
		if (v != null || this.current != null) {
			// on change le curseur en main
			c.setCursor(new Cursor(Cursor.HAND_CURSOR));
		} else {
			// sinon on laisse le curseur par defaut
			c.setCursor(null);
		}

		// si un selecteur est selectionne, on appelle la methode chartMouseMoved
		if (this.current != null) {
			this.current.chartMouseMoved(event);
		}
	}

	public void updateAreas() {
		XYPlot plot = this.chartPanel.getChart().getXYPlot();
		plot.clearDomainMarkers();
		for (Area area : this.areas) {
			area.update();
		}
	}

	/**
	 * ajoute un selecteur a un index donne
	 * 
	 * @param v
	 *            le selecteur
	 * @param index
	 *            index du selecteur
	 */
	public void add(ValueSelector v, Comparable key) {
		// on passe le chartpanel au selecteur
		v.setChartPanel(this.chartPanel);
		v.setKey(key);

		// on ajoute le selecteur a la liste utilisee pour conserver l'ordre en Z
		this.listenersPriority.add(v);

		// on ajoute la valeur au bon emplacement dans la liste
		this.listeners.put(key, v);

		// on ajoute la couleur selon la courbe
		Color c = null;
		if (v.getSeries() == 1) {
			c = new Color(85, 85, 255);
		} else if (v.getSeries() == 0) {
			c = new Color(255, 85, 85);
		} else {
			c = new Color(85, 85, 85);
		}
		v.setPaint(c);

		// on ajoute le selecteur a l'overlay
		this.chartPanel.addOverlay(v);
	}

	/**
	 * supprime le selecteur d'un index donne de l'overlay
	 * 
	 * @param index
	 *            index du selecteur
	 */
	public void remove(Comparable key) {
		// si il n'y a pas de selecteur avec cette cle
		if (!this.listeners.keySet().contains(key)) {
			return;
		}

		ValueSelector v = this.listeners.get(key);
		this.chartPanel.removeOverlay(v);
		this.listeners.remove(v.getKey());
		this.listenersPriority.remove(v);

		List<Area> areasToRemove = new ArrayList<>();
		for (Area area : this.areas) {
			ValueSelector debut = area.start;
			ValueSelector fin = area.end;
			if (v == debut || v == fin) {
				this.remove(area.middle.getKey());
				areasToRemove.add(area);
			}
		}

		for (Area area : areasToRemove) {
			this.areas.remove(area);
		}
		
		XYPlot plot = SelectorListener.this.chartPanel.getChart().getXYPlot();
		plot.clearDomainAxes();
		
		for (Area area : this.areas) {
			area.fillInterval();
		}
	}

	public void addArea(Comparable startKey, Comparable endKey, Color color) {
		ValueSelector start = this.listeners.get(startKey);
		ValueSelector end = this.listeners.get(endKey);
		Area area = new Area(start, end, color);
		area.update();
		this.areas.add(area);
	}

	/**
	 * renvoie tous les selecteurs dans l'ordre de priorite
	 * 
	 * @return liste de selecteurs
	 */
	public List<ValueSelector> getListeners() {
		return this.listenersPriority;
	}

	public HashMap<Comparable, Double> getValues() {
		HashMap<Comparable, Double> hm = new HashMap<>();

		for (Comparable k : this.listeners.keySet()) {
			hm.put(k, this.listeners.get(k).getXValue());
		}

		return hm;
	}

	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}

	private class Area {
		private ValueSelector start, end, middle;
		private Color color;
		private double boundsDistToCenter;

		public Area(ValueSelector start, ValueSelector end, Color color) {
			this.color = color;
			this.start = start;
			this.end = end;

			if (color == null) {
				this.color = new Color(225, 244, 50, 120);
			} else {
				this.color = color;
			}

			middle = new ValueSelector("<>", 2, -1, RectangleAnchor.CENTER);
			Random rng = new Random();
			int key = rng.nextInt();
			SelectorListener.this.add(middle, key);
		}

		public void update() {
			if (middle.isXLocked()) { // si le selecteur du milieu n'est pas selectionne, on le recentre
				middle.setXValue((start.getXValue() + end.getXValue()) / 2);
				this.boundsDistToCenter = Math.abs(start.getXValue() - middle.getXValue());
			} else { // sinon on bouge les deux autres selecteurs
				start.setXValue(middle.getXValue() - this.boundsDistToCenter);
				end.setXValue(middle.getXValue() + this.boundsDistToCenter);
			}
			fillInterval();
		}

		private void fillInterval() {
			Double start = this.start.getXValue();
			Double end = this.end.getXValue();
			double debut = Math.min(start, end);
			double fin = Math.max(start, end);

			XYPlot plot = SelectorListener.this.chartPanel.getChart().getXYPlot();
			Marker bst = new IntervalMarker(debut, fin, this.color, new BasicStroke(2.0f), null, null, 1.0f);

			bst.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			bst.setLabelOffset(new RectangleInsets(15, 0, 0, 5));
			bst.setLabelFont(new Font("SansSerif", 0, 12));
			bst.setLabelTextAnchor(TextAnchor.BASELINE_RIGHT);
			plot.addDomainMarker(bst);
		}
	}
}