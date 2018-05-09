package org.petctviewer.scintigraphy.renal;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;

public class SelectorListener implements ChartMouseListener {
		
		private List<ValueSelector> listenersPriority;
		private ChartPanel chartPanel;
		private XYPlot plot;
		private ValueSelector current;		
		private ValueSelector[] listeners;
		
		private boolean colored;
		
		public SelectorListener(ChartPanel chartPanel) {
			this.chartPanel = chartPanel;
			this.plot = chartPanel.getChart().getXYPlot();
			this.listenersPriority = new ArrayList<ValueSelector>();
			this.listeners = new ValueSelector[6];
		}

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
			if(this.current != null) {
				this.current = null;
			}
			
			int xMouse = (int) event.getTrigger().getPoint().getX();
			Rectangle2D plotArea = chartPanel.getScreenDataArea();

			ValueSelector v = this.getSelector(xMouse, plotArea);
			
			if(v != null) {
				v.chartMouseClicked(event);
				this.current = v;
				
				//s'assure que le dernier marqueur clique est le premier dans la liste afin qu'il prenne le focus
				this.listenersPriority.remove(v);
				this.listenersPriority.add(0, v);
			}
		}
		
		private ValueSelector getSelector(int xMouse, Rectangle2D plotArea) {
			int marge = 5;
			
			for(ValueSelector v : this.listenersPriority) {
				int xJava2D = (int) this.plot.getDomainAxis().valueToJava2D(v.getXValue(), plotArea,
						plot.getDomainAxisEdge());				
				if (xJava2D > xMouse - marge && xJava2D < xMouse + marge) {
					return v;
				}
			}
			return null;
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			if(current != null) {
				this.current.chartMouseMoved(event);
			}
		}
		
		public void add(ValueSelector v, int index) {
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
			this.chartPanel.addOverlay(v);
		}
		
		public void remove(int index) {
			this.chartPanel.removeOverlay(this.listeners[index]);
		}
		
		public List<ValueSelector> getListeners(){
			return listenersPriority;
		}
		
		/**
		 * [0] => TMaxD <br> [1] => TMaxG <br> [2] => Retetion origin D <br> [3] => Retetion origin G <br> [4] => Borne intervalle 1 <br> [5] => Borne intervalle 2
		 * @return
		 */
		public Double[] getXValues() {
			Double[] values = new Double[6];
			for(int i = 0; i < this.listeners.length ; i++) {
				values[i] = this.listeners[i].getXValue();
			}
			return values;			
		}
		
		public boolean isColored() {
			return this.colored;
		}

		public void setColored(boolean colored) {
			this.colored = colored;
		}
		
	}