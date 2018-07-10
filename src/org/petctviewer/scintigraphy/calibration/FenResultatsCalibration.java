package org.petctviewer.scintigraphy.calibration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.ImageCalculator;
import ij.plugin.Stack_Statistics;
import ij.plugin.Thresholder;
import ij.plugin.filter.ImageMath;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder;
import ij.process.StackStatistics;
import loci.formats.FormatException;
import loci.formats.in.NiftiReader;
import loci.formats.tools.BioFormatsExtensionPrinter;
import loci.plugins.BF;
import loci.plugins.LociImporter;
import loci.plugins.in.ImporterOptions;
import net.imagej.display.SelectWindow;
import ome.xml.model.Mask;

public class FenResultatsCalibration extends JFrame{
	
	private JPanel graphs;
	private JPanel east ;
	
	public FenResultatsCalibration(ArrayList<String[]> examList) {
		ControleurCalibration cc = new ControleurCalibration(examList, this);
		
		this.setLayout(new BorderLayout());
		
		JFreeChart graph = ChartFactory.createScatterPlot(
  		        "Essai", 
  		        "X", "Y", null);
	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    
	    this.graphs = new JPanel();
	    this.graphs.add(chartPanel);
	    this.add(graphs,BorderLayout.CENTER);
	    
	    
	    east = new JPanel();
	    east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));

	    JButton afficher = new JButton("Afficher");
	    afficher.addActionListener(cc);
	    east.add(afficher);

	   

	   this.add(east,BorderLayout.EAST);
	   this.pack();
		//1ml = 10^12dm3
	}
	
	 public void afficherDonnee(XYSeriesCollection data) {
		  	this.graphs.removeAll();
		  	this.graphs.revalidate();
		  	
		  	
XYDataItem f = new XYDataItem(1.0D, 1.0D);
XYSeries m = new XYSeries("k");

			JFreeChart graph = ChartFactory.createScatterPlot(
		        "Essai", 
		        "X", "Y", data);
		  
		    
		    //Changes background color
		    XYPlot plot = (XYPlot)graph.getPlot();
		    plot.setBackgroundPaint(new Color(255,228,196));
		    
		    ChartPanel chartPanel = new ChartPanel(graph);
		    this.graphs.add(chartPanel);
			this.graphs.repaint();
						
			String[] acquiTitle = new String[data.getSeriesCount()];
			for(int i=0;i<data.getSeriesCount(); i++) {
				acquiTitle[i] = "Acqui "+(i+1);
				//data.getItemCount(i); ???
			}
			
			String[] sphereTitle = new String[data.getItemCount(0)];// le nombre de points dans la premiere serie
			for(int i =0; i< data.getItemCount(0);i++) {
				sphereTitle[i] = "Sphere "+(i+1);
			}
			
		    JTableCheckBox jtcb = new JTableCheckBox(acquiTitle,sphereTitle);
		    east.add(jtcb);
		    
		    //ShapeUtils.
	  }
}
