package org.petctviewer.scintigraphy.calibration.resultats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.apache.commons.lang.ArrayUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.Regression;
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
import ij.util.ArrayUtil;
import loci.formats.FormatException;
import loci.formats.in.NiftiReader;
import loci.formats.tools.BioFormatsExtensionPrinter;
import loci.plugins.BF;
import loci.plugins.LociImporter;
import loci.plugins.in.ImporterOptions;
import net.imagej.display.SelectWindow;
import ome.xml.model.Mask;

public class FenResultatsCalibration extends JFrame{
	
	private JPanel east ;
	private ControleurResultatsCalibration crc;

	private JFreeChart graph;
	 
	private JLabel aLabel;
	private JLabel bLabel;

	public FenResultatsCalibration(Doublet[][] data) {
		this.setLayout(new BorderLayout());
		
		//graph center 
	  	graph = ChartFactory.createScatterPlot(
	        "Schaefer calibration", 
	        "X = (mSUV70 - BG) / BG ", "Y = TS / (mSUV70 - BG)", null);
	  	
	    //Changes background color
	    XYPlot plot = (XYPlot)graph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));

	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    this.add(chartPanel,BorderLayout.CENTER);
	    

	  	// east panel
		 //coeff a et b
		 JPanel coefGrid = new JPanel();
		 coefGrid.setLayout(new GridLayout(2,1));
		aLabel = new JLabel("a = ");
		bLabel = new JLabel("b = ");
	    coefGrid.add(aLabel);
	    coefGrid.add(bLabel);
	    JPanel coef = new JPanel();
	    coef.setLayout(new FlowLayout());
	    coef.add(coefGrid);
	
	    //graph
		String[] acquiTitle = new String[data.length];
		for(int i=0;i<data.length; i++) {
			acquiTitle[i] = "Acqui "+(i+1);
		}
		String[] sphereTitle = new String[data[0].length];// le nombre de points dans la premiere serie
		for(int i =0; i< data[0].length;i++) {
		sphereTitle[i] = "Sphere "+(i+1);
		}
		crc = new ControleurResultatsCalibration(this,data);
	    JTableCheckBox jtcb = new JTableCheckBox(acquiTitle,sphereTitle,crc);
	    
	    east = new JPanel();
	    east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
	    this.east.add(jtcb);
	    this.east.add(coef);
	    this.add(east,BorderLayout.EAST);

	    this.pack();
	}
	 
	 public void setCoef(Double a, Double b) {
		 DecimalFormat df = new DecimalFormat("#.###");
		   
		 aLabel.setFont(new Font("", Font.PLAIN, 20));
		 bLabel.setFont(new Font("", Font.PLAIN, 20));
		 
		 aLabel.setText("a = "+df.format((Double)a));
		 bLabel.setText("b = "+df.format((Double)b));
	 }

	public JFreeChart getGraph() {
		return this.graph;
	}
	
	public void setGraph(XYSeriesCollection data) {
		graph.getXYPlot().setDataset(data);

		 XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) graph.getXYPlot().getRenderer();
		 
		 renderer.setSeriesPaint(data.getSeriesCount()-1, new Color(246,0,0));
		 renderer.setSeriesLinesVisible(data.getSeriesCount()-1, true);
		 renderer.setSeriesShapesVisible(data.getSeriesCount()-1, false);
	      
	     this.graph.getXYPlot().setRenderer(renderer);
	}
	 
	 
//swing worker
	//vue anone otrhanc tools
	 
}
