package org.petctviewer.scintigraphy.renal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;

public class SelectorListener implements ChartMouseListener {
		
		//ordre de priorite des selecteurs
		private List<ValueSelector> listenersPriority;
		
		//nombre max de selecteurs
		private static int SELECTOR_CAPACITY = 100;
		
		private ChartPanel chartPanel;
		private XYPlot plot;
		private ValueSelector current;		
		private ValueSelector[] listeners;
		
		/**
		 * permet de gerer plusieurs selecteurs sur le meme chartPanel en conservant l'ordre en Z
		 * @param chartPanel
		 */
		public SelectorListener(ChartPanel chartPanel) {
			this.chartPanel = chartPanel;
			this.plot = chartPanel.getChart().getXYPlot();
			this.listenersPriority = new ArrayList<>();
			this.listeners = new ValueSelector[SELECTOR_CAPACITY];
		}

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
			//curseur par defaut
			Component c = (Component) event.getTrigger().getSource();
			c.setCursor(null);
			
			//si on avait un selecteur selectionne, on le deselectionne
			if(this.current != null) {
				this.current = null;
			}
			
			//on recupere le selecteur clique
			int xMouse = (int) event.getTrigger().getPoint().getX();
			Rectangle2D plotArea = this.chartPanel.getScreenDataArea();
			ValueSelector v = this.getSelector(xMouse, plotArea);
			
			//si il y a un selecteur sous la souris
			if(v != null) {
				//on active son clic et on le selectionne
				v.chartMouseClicked(event);
				this.current = v;
				
				//s'assure que le dernier marqueur clique est le premier dans la liste afin qu'il prenne le focus
				this.listenersPriority.remove(v);
				this.listenersPriority.add(0, v);
			}
		}
		
		private ValueSelector getSelector(int xMouse, Rectangle2D plotArea) {
			//marge a gauche et a droite du selecteur permettant le clic
			int marge = 5;
			
			//pour chaque selecteur en respectant l'ordre de priorite
			for(ValueSelector v : this.listenersPriority) {
				//on converti l'abscisse du selecteur sur le tableau en abscisse sur la fenetre
				int xJava2D = (int) this.plot.getDomainAxis().valueToJava2D(v.getXValue(), plotArea, this.plot.getDomainAxisEdge());
				//si il y a un selecteur la ou on a clique, on le renvoie
				if (xJava2D > xMouse - marge && xJava2D < xMouse + marge) {
					return v;
				}
			}
			return null;
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			Component c = (Component) event.getTrigger().getSource();
			
			//on recupere le selecteur sous la souris
			int xMouse = (int) event.getTrigger().getPoint().getX();
			Rectangle2D plotArea = this.chartPanel.getScreenDataArea();
			ValueSelector v = this.getSelector(xMouse, plotArea);
			
			//si la souris est sur un selecteur ou qu'un selecteur est selectionne
			if(v != null || this.current != null) {
				//on change le curseur en main
				c.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}else {
				//sinon on laisse le curseur par defaut
				c.setCursor(null);
			}
			
			//si un selecteur est selectionne, on appelle la methode chartMouseMoved
			if(this.current != null) {
				this.current.chartMouseMoved(event);
			}
		}
		
		/**
		 * ajoute un selecteur a un index donne
		 * @param v le selecteur
		 * @param index index du selecteur
		 */
		public void add(ValueSelector v, int index) {
			//on passe le chartpanel au selecteur
			v.setChartPanel(this.chartPanel);
			
			//on ajoute le selecteur a la liste utilisee pour conserver l'ordre en Z
			this.listenersPriority.add(v);
		
			//on ajoute la valeur au bon emplacement dans la liste
			this.listeners[index] = v;
			
			//on ajoute la couleur selon la courbe
			Color c = null;
			if(v.getSeries() == 1) {
				c = new Color(85, 85, 255);
			}else if(v.getSeries() == 0) {
				c = new Color(255, 85, 85);
			}else {
				c = new Color(85, 85, 85);
			}
			v.setPaint(c);
			
			//on ajoute le selecteur a l'overlay
			this.chartPanel.addOverlay(v);
		}
		
		/**
		 * supprime le selecteur d'un index donne de l'overlay
		 * @param index index du selecteur
		 */
		public void remove(int index) {
			this.chartPanel.removeOverlay(this.listeners[index]);
		}
		
		/**
		 * renvoie tous les selecteurs dans l'ordre de priorite
		 * @return liste de selecteurs
		 */
		public List<ValueSelector> getListeners(){
			return this.listenersPriority;
		}
		
		/**
		 * Renvoie les valeurs en x des selecteurs
		 * @return [0] => TMaxD <br> [1] => TMaxG <br> [2] => Retetion origin D <br> [3] => Retetion origin G <br> [4] => Borne intervalle 1 <br> [5] => Borne intervalle 2
		 */
		public Double[] getXValues() {
			Double[] values = new Double[6];
			for(int i = 0; i < values.length ; i++) {
				values[i] = this.listeners[i].getXValue();
			}
			return values;			
		}
		
	}