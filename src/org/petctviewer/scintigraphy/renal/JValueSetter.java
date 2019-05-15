package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class JValueSetter extends ChartPanel implements ChartMouseListener, MouseWheelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// ordre de priorite des selecteurs
	private List<Selector> selectors;
	private Selector current;
	private HashMap<Comparable, Area> areas;

	public JValueSetter(JFreeChart chart) {
		super(chart);
		this.addChartMouseListener(this);
		this.addMouseWheelListener(this);

		this.selectors = new ArrayList<>();
		this.areas = new HashMap<Comparable, Area>();

		// desactive le zoom
		this.setDomainZoomable(false);
		this.setRangeZoomable(false);
	}

	/**
	 * permet de gerer plusieurs selecteurs sur le meme chartPanel en conservant
	 * l'ordre en Z
	 * 
	 * @param chartPanel
	 */
	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// curseur par defaut
		Component c = (Component) event.getTrigger().getSource();

		// si on avait un selecteur selectionne, on le deselectionne
		if (this.current != null) {
			this.current.chartMouseClicked(event);
			this.current = null;
			c.setCursor(null);
			return;
		}

		// on recupere le selecteur clique
		int xMouse = (int) event.getTrigger().getPoint().getX();
		int yMouse = (int) event.getTrigger().getPoint().getY();
		Rectangle2D plotArea = this.getScreenDataArea();
		Selector v = this.getSelector(xMouse, yMouse, plotArea);

		// si il y a un selecteur sous la souris
		if (v != null && this.current == null) {
			// on active son clic et on le selectionne
			v.chartMouseClicked(event);
			this.current = v;

			// s'assure que le dernier marqueur clique est le premier dans la liste afin
			// qu'il prenne le focus
			this.selectors.remove(v);
			this.selectors.add(0, v);
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		Component c = (Component) event.getTrigger().getSource();

		updateAreas();

		// on recupere le selecteur sous la souris
		int xMouse = (int) event.getTrigger().getPoint().getX();
		int yMouse = (int) event.getTrigger().getPoint().getY();
		Rectangle2D plotArea = this.getScreenDataArea();
		Selector v = this.getSelector(xMouse, yMouse, plotArea);

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

	private Selector getSelector(Comparable key) {
		for (Selector s : this.selectors) {
			if (s.getKey() == key) {
				return s;
			}
		}
		return null;
	}

	private Selector getSelector(int xMouse, int yMouse, Rectangle2D plotArea) {
		// marge a gauche et a droite du selecteur permettant le clic
		int marge = 5;
		XYPlot plot = this.getChart().getXYPlot();

		// pour chaque selecteur en respectant l'ordre de priorite
		for (Selector v : this.selectors) {
			// on converti l'abscisse du selecteur sur le tableau en abscisse sur la fenetre
			int xJava2D;
			if(v instanceof YSelector) {
				xJava2D = (int) plot.getRangeAxis().valueToJava2D(v.getXValue(), plotArea, plot.getRangeAxisEdge());
				// si il y a un selecteur la ou on a clique, on le renvoie
				if (xJava2D > yMouse - marge && xJava2D < yMouse + marge) {
					return v;
				}
			}
			else {
				xJava2D = (int) plot.getDomainAxis().valueToJava2D(v.getXValue(), plotArea, plot.getDomainAxisEdge());
				// si il y a un selecteur la ou on a clique, on le renvoie
				if (xJava2D > xMouse - marge && xJava2D < xMouse + marge) {
					return v;
				}
			}
		}
		return null;
	}

	public void updateAreas() {
		XYPlot plot = this.getChart().getXYPlot();
		plot.clearDomainMarkers();
		for (Comparable key : this.areas.keySet()) {
			Area area = this.areas.get(key);
			area.update();
		}
	}

	/**
	 * ajoute un selecteur a un cle donne
	 * 
	 * @param v   le selecteur
	 * @param key cle du selecteur
	 */
	public void addSelector(Selector v, Comparable key) {
		// on passe la cle au selecteur
		v.setJValueSetter(this);
		v.setKey(key);

		// on ajoute le selecteur a la liste utilisee pour conserver l'ordre en Z
		this.selectors.add(v);

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
		this.addOverlay(v);
	}

	/**
	 * supprime le selecteur d'un index donne de l'overlay
	 * 
	 * @param index index du selecteur
	 */
	public void removeSelector(Comparable key) {
		Selector v = this.getSelector(key);

		// si il n'y a pas de selecteur avec cette cle
		if (v == null) {
			return;
		}

		this.removeOverlay(v);
		this.selectors.remove(v);
		// si le selecteur fait partie d'une aire, l'aire est supprim�e
		List<Comparable> areasToRemove = new ArrayList<>();
		for (Comparable k : this.areas.keySet()) {
			Area area = this.areas.get(k);
			Selector debut = area.start;
			Selector fin = area.end;
			Selector middle = area.middle;
			if (v == debut || v == fin) {
				this.removeOverlay(debut);
				this.selectors.remove(debut);

				this.removeOverlay(fin);
				this.selectors.remove(fin);

				this.removeOverlay(middle);
				this.selectors.remove(middle);

				areasToRemove.add(k);
			}
		}

		// supprime les aires
		for (Comparable k : areasToRemove) {
			this.areas.remove(k);
		}
	}

	public void addArea(Comparable startKey, Comparable endKey, Comparable areaKey, Color color) {
		Selector start = this.getSelector(startKey);
		Selector end = this.getSelector(endKey);
		Area area = new Area(start, end, color);
		area.update();
		this.areas.put(areaKey, area);
	}

	/**
	 * renvoie tous les selecteurs dans l'ordre de priorite
	 * 
	 * @return liste de selecteurs
	 */
	public List<Selector> getListeners() {
		return this.selectors;
	}

	public HashMap<Comparable, Double> getValues() {
		HashMap<Comparable, Double> hm = new HashMap<>();

		for (Selector s : selectors) {
			hm.put(s.getKey(), s.getXValue());
		}

		return hm;
	}

	/**
	 * Returns the values of the graph between the 'start' and the 'end' selectors.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public XYDataset retrieveValuesInSpan() {
		return Library_JFreeChart.cropDataset(this.getChart().getXYPlot().getDataset(),
				this.getSelector("start").getXValue(), this.getSelector("end").getXValue());
	}
	
	public Selector getGrabbedSelector() {
		return this.current;
	}

	private class Area {
		private Selector start, end, middle;
		private Color color;
		private double boundsDistToCenter;

		public Area(Selector start, Selector end, Color color) {
			this.color = color;
			this.start = start;
			this.end = end;

			if (color == null) {
				this.color = new Color(225, 244, 50, 120);// jaune
			} else {
				this.color = color;
			}

			middle = new Selector("<>", 2, -1, RectangleAnchor.CENTER);
			addSelector(middle, new Random().nextInt());
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

			XYPlot plot = getChart().getXYPlot();
			Marker bst = new IntervalMarker(debut, fin, this.color, new BasicStroke(2.0f), null, null, 1.0f);

			bst.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			bst.setLabelOffset(new RectangleInsets(15, 0, 0, 5));
			bst.setLabelFont(new Font("SansSerif", 0, 12));
			bst.setLabelTextAnchor(TextAnchor.BASELINE_RIGHT);
			plot.addDomainMarker(bst);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		for(Selector selector : selectors) {
			if(selector instanceof YSelector) {
				YSelector ySelector = (YSelector) selector;
				if (ySelector.getCrossXs().size() > 0) {
					if(ySelector.getCurrentLabelVisible() == -1) {
						for(Crosshair crossX :  ySelector.getCrossXs())
							crossX.setLabelVisible(false);
						if (e.getWheelRotation() < 0)
							ySelector.setCurrentLabelVisible(0);
						else
							ySelector.setCurrentLabelVisible(((YSelector) ySelector).getCrossXs().size() - 1);
					}else {
						int currentLabel = ySelector.getCurrentLabelVisible();
						if (e.getWheelRotation() < 0) {
							ySelector.getCrossXs().get(currentLabel).setLabelVisible(false);
							ySelector.setCurrentLabelVisible((currentLabel + 1) % ySelector.getCrossXs().size());
						}else {
							ySelector.getCrossXs().get(currentLabel).setLabelVisible(false);
							ySelector.setCurrentLabelVisible((currentLabel == 0 ? ySelector.getCrossXs().size() - 1 : currentLabel - 1) % ySelector.getCrossXs().size());
						}
					}
					ySelector.getCrossXs().get(ySelector.getCurrentLabelVisible()).setLabelVisible(true);
				}
			}
		}
	}
}