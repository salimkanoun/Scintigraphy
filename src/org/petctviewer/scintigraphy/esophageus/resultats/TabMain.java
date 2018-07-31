package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

//tab d'affichage des coubres avec selecteur
//
public class TabMain extends JPanel{

	public TabMain() {
		this.setLayout(new BorderLayout());
		
		
	}
	
	
	public void setGraphDataset(XYSeries[][] dataset) {
		
		XYSeriesCollection d = new XYSeriesCollection();
		titleRows = new String [dataset.length];
		
		for(int i =0; i< dataset.length; i++) {
			titleRows[i] = "Acqui "+i;
			for(int j =0; j<dataset[i].length; j++) {
				System.out.println("i: "+i+" j: "+j);
				d.addSeries(dataset[i][j]);
			}
		}
		
		this.graph.getXYPlot().setDataset(d);
		//this.graph.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
	
		d=null;
	}
}
