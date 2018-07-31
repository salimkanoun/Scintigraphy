package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;

public class FenResultats_EsophagealTransit extends JFrame{

	private JFreeChart graph;
	
	private String [] titleRows;
	
	private String [] titleCols = {"Entier","un Tier","deux Tier", "trois Tier"};
	
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList) {
		
		this.setLayout(new BorderLayout());
		
		// tabbed
		
		//graph center 
	  	 graph = ChartFactory.createXYLineChart(
	        "Esophageal Transit", 
	        "s", "Count/s", null);
	  		  	
	    //Changes background color
	    XYPlot plot = (XYPlot)graph.getPlot();
	  //  plot.setBackgroundPaint(new Color(255,228,196));
	    plot.setBackgroundPaint(new Color(255,255,255));

	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    this.add(chartPanel,BorderLayout.CENTER);
		
		Controleur_Resultats_EsophagealTransit cret = new Controleur_Resultats_EsophagealTransit(this, arrayList);

	    JTableCheckBox d = new JTableCheckBox(titleRows, titleCols, cret);
		
		this.add(d, BorderLayout.EAST);
		this.pack();
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
