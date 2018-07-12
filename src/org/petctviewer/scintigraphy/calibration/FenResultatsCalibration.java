package org.petctviewer.scintigraphy.calibration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import org.jfree.chart.util.ShapeUtils;
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
	
	private  Doublet[][] dataInitial=null;
	private  Doublet[][] dataCurrent=null;

	private JFreeChart graph;
	 
	private JLabel aLabel;
	private JLabel bLabel;

	public FenResultatsCalibration(Doublet[][] data) {
		 crc = new ControleurResultatsCalibration(this,data);
		
		this.setLayout(new BorderLayout());
	
		//chargement des data
		this.dataInitial = new Doublet[data.length][data[0].length];
	    Doublet[][] d = new Doublet[data.length][data[0].length];
		for(int i =0; i<data.length; i++) {
			for(int j =0; j<data[i].length; j++) {
				this.dataInitial[i][j] = new Doublet(data[i][j].getA(),data[i][j].getB());
			}
		}
	  	this.dataCurrent = data.clone();
		
	  	// east panel
	  	east = new JPanel();
	    east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));

		String[] acquiTitle = new String[this.dataCurrent .length];
		for(int i=0;i<data.length; i++) {
			acquiTitle[i] = "Acqui "+(i+1);
		}
		
		String[] sphereTitle = new String[this.dataCurrent[0].length];// le nombre de points dans la premiere serie
		for(int i =0; i< data[0].length;i++) {
			sphereTitle[i] = "Sphere "+(i+1);
		}
		
	    JTableCheckBox jtcb = new JTableCheckBox(acquiTitle,sphereTitle,crc);
	    east.add(jtcb);
	    
	    JPanel coefGrid = new JPanel();
	    coefGrid.setLayout(new GridLayout(2,1));
	     aLabel = new JLabel("a = ");
	     bLabel = new JLabel("b = ");

	    coefGrid.add(aLabel);
	    coefGrid.add(bLabel);
	   
	    JPanel coef = new JPanel();
	    coef.setLayout(new FlowLayout());
	    coef.add(coefGrid);
	    
	    this.east.add(coef);
	    this.add(east,BorderLayout.EAST);
		
		
		//graph center 
	  	graph = ChartFactory.createScatterPlot(
	        "Schaefer calibration", 
	        "X = (mSUV70 - BG) / BG ", "Y = TS / (mSUV70 - BG)", buildColletionFromDoublet(this.dataCurrent));
	  
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(true, false);
		xylineandshaperenderer.setSeriesPaint(0, Color.YELLOW);
		this.graph.getXYPlot().setRenderer(buildColletionFromDoublet(this.dataCurrent).getSeriesCount(), xylineandshaperenderer);
		
		System.out.println(buildColletionFromDoublet(this.dataCurrent).getSeriesCount());
		
	    //Changes background color
	    XYPlot plot = (XYPlot)graph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));

	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    this.add(chartPanel,BorderLayout.CENTER);
	
	    this.pack();
	}
	 
	//actualise les data depuis la tableau de checkbox
	 public void actualiserDatasetFromCheckbox(int a, int b, boolean visiblity) {
		 if(visiblity) {
			 this.dataCurrent[a][b].setA( (Double)this.dataInitial[a][b].getA() );
			 this.dataCurrent[a][b].setB( (Double)this.dataInitial[a][b].getB() );
		 }else {
			 this.dataCurrent[a][b]=new Doublet(Double.NaN, Double.NaN);
		 }
		 this.graph.getXYPlot().setDataset(buildColletionFromDoublet(dataCurrent));
	 }

	 private XYSeriesCollection buildColletionFromDoublet(Doublet[][] data) {
			XYSeriesCollection collection = new XYSeriesCollection();
		  	for(int i=0;i<data.length;i++) {
		  		XYSeries serie = new XYSeries("Acqui "+(i+1));
		  		for(int j=0; j<data[i].length;j++) {
		  			serie.add(data[i][j].getA(), data[i][j].getB());	
		  		}
		  		collection.addSeries(serie);	
		  	}
		  	collection.addSeries(this.feat(data));
		  	return collection;
	 }
	 
	 private XYSeries feat (Doublet[][] data) {
		 //calcul de a+b*x
		 Doublet[][] m = new Doublet[data.length][data[0].length];
		 for(int i =0; i<data.length; i++) {
			 for(int j =0; j<data[i].length; j++) {
					m[i][j] = new Doublet(data[i][j].getA(),data[i][j].getB()*data[i][j].getA());
			 }
		 }
		 
  		XYSeries serikke = new XYSeries("ff ");
	  	for(int i=0;i<m.length;i++) {
	  		for(int j=0; j<m[i].length;j++) {
				if( !m[i][j].getA().equals(Double.NaN) && !m[i][j].getB().equals(Double.NaN) ) {
					serikke.add(m[i][j].getA(), m[i][j].getB());
				}
	  		}
  		}
	  	
		 //feat
		 double[] resultRegression = Regression.getOLSRegression(new XYSeriesCollection(serikke),0);
		 
		 System.out.println("a : "+resultRegression[0]);
		 System.out.println("b : "+resultRegression[1]);

		 //tracage du feat
		 XYSeries feat = new XYSeries("feat");
		 for(double i=0.1D; i< serikke.getItemCount();i+=0.1D) {
				 feat.add(i,((resultRegression[0]/i)+resultRegression[1]) );
			 //feat.add(i,((0.7D/i)+0.4D) );
			 //feat.add(i,resultRegression[1]*i+resultRegression[0]);
		 }
		 
		 setCoef(resultRegression[0], resultRegression[1]); 
		 return feat;
	 }

	 private void setCoef(Double a, Double b) {
		 DecimalFormat df = new DecimalFormat("#.###");
		   
		 aLabel.setFont(new Font("", Font.PLAIN, 20));
		 bLabel.setFont(new Font("", Font.PLAIN, 20));

		 aLabel.setText("a = "+df.format(a));
		 bLabel.setText("b = "+df.format(b));
	 }
//swing worker
	//vue anone otrhanc tools
	 
}
