package org.petctviewer.scintigraphy.CSV;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class FenApplication_CVS extends JFrame{

	public FenApplication_CVS(String chemin) throws IOException {
		this.setTitle("CVS");
		this.setLayout(new BorderLayout());
		this.setSize(300, 300);
		
		JLabel labelChemin = new JLabel("chemin");
		labelChemin.setText(chemin); 
		
		/********Calcul*****/
		File file = new File(chemin);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		 
		List<String> lines = new ArrayList<>();
		
		System.out.println("******** Contenu du fichier**********");
		for(String line = br.readLine(); line != null; line = br.readLine()) {
			lines.add(line);
		System.out.println(line);	
		}
		System.out.println("******** Fin du ontenu du fichier**********");
		
		br.close();
		fr.close();
		
		for(String s : lines) {
			System.out.println("ligne : "+lines.indexOf(s)+ " size :"+ s.length());
		}		
		
		
		//essai d'un graph avec les 2 premier params
		// l 4
		String[] x = lines.get(4).split(",");
		String[] y = lines.get(5).split(",");
		String[] z = lines.get(6).split(",");
		String[] w = lines.get(7).split(",");


		XYSeries serie = new XYSeries(x[0] + " / " + y[0]);
		for(int i = 1; i<x.length;i++) {
			serie.add(Double.parseDouble(x[i]),Double.parseDouble(y[i]));
		}
		

		XYSeries serie2 = new XYSeries(x[0] + " / " + z[0]);
		for(int i = 1; i<x.length;i++) {
			serie2.add(Double.parseDouble(x[i]),Double.parseDouble(z[i]));
		}
		

		XYSeries serie3 = new XYSeries(x[0] + " / " + w[0]);
		for(int i = 1; i<x.length;i++) {
			serie3.add(Double.parseDouble(x[i]),Double.parseDouble(w[i]));
		}
		
		
		
		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(serie);
		data.addSeries(serie2);
		data.addSeries(serie3);	
		
		JFreeChart graph = ChartFactory.createXYLineChart("titre",x[0],y[0],data);
	  
	    
	    //Changes background color
	    XYPlot plot = (XYPlot)graph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));
	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    this.add(chartPanel,BorderLayout.CENTER);


		/******/
		
		this.add(labelChemin,BorderLayout.NORTH);
	}
	
}
