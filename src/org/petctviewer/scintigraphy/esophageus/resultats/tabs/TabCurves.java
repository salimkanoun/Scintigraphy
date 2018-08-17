package org.petctviewer.scintigraphy.esophageus.resultats.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.esophageus.application.Modele_EsophagealTransit;
import org.petctviewer.scintigraphy.esophageus.resultats.Modele_Resultats_EsophagealTransit;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

import ij.IJ;
import ij.ImagePlus;

public class TabCurves extends JPanel{
	
	private JFreeChart graphMain;
	
	public TabCurves(int nbAcquisition, Modele_Resultats_EsophagealTransit modele, Modele_EsophagealTransit modeleApp) {
		
		this.setLayout(new BorderLayout());
		
		//graph  
	  	graphMain = ChartFactory.createXYLineChart( "Esophageal Transit", "s", "Count/s", null);
	  	 
	  	XYLineAndShapeRenderer rendererMain = new XYLineAndShapeRenderer();
 	  	rendererMain.setDefaultShapesVisible(true);
 	  
 	  	/*
 	  	for(int i =0; i< nbAcquisition; i++) {
 	 	  	rendererMain.setSeriesShape(i, ShapeUtils.createDiagonalCross(3, 1));
 	 	  	//System.out.println('i'+i);
 	  	}
 	  	*/
 	  	
 	  	//met la grille en noir
 		graphMain.getXYPlot().setDomainGridlinePaint(Color.black);
 	  	graphMain.getXYPlot().setRangeGridlinePaint(Color.black);
 	  	
 	  	//pour avoir les infos des points quand on passe la souris dessus
 	  	rendererMain.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
 	  	
 	  	graphMain.getXYPlot().setRenderer(rendererMain);
 	  
	    //Changes background color et grid color
	    this.graphMain.getXYPlot().setBackgroundPaint(new Color(255,255,255)); 	    
		
		 
	    
	    ChartPanel chartPanel = new ChartPanel(graphMain);	    
	    this.add(chartPanel,BorderLayout.CENTER);
	    
	    
	    //set les data du graph
	    XYSeries [][] datasetModele = modele.getDataSetMain();
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    String [] titleRows = new String [datasetModele.length];
		
		for(int i =0; i< datasetModele.length; i++) {
			titleRows[i] = "Acqui "+(i+1);
			for(int j =0; j<datasetModele[i].length; j++) {
				//System.out.println("i: "+i+" j: "+j);
				dataset.addSeries(datasetModele[i][j]);
			}
		}
		this.graphMain.getXYPlot().setDataset(dataset);
	    
		String [] titleCols = {"Full","Upper","Middle", "Lower"};
	    
	    // table de checkbox	
	    JTableCheckBox tableCheckbox = new JTableCheckBox(titleRows, titleCols, new ChangeListener() {
		
			@Override
			public void stateChanged(ChangeEvent e) {
				
				TabCurves tab = TabCurves.this;
				JCheckBox selected = (JCheckBox)e.getSource();
				
				tab.setVisibilitySeriesMain(	
						Integer.parseInt(selected.getName().split("\\|")[0]), 
		 				Integer.parseInt(selected.getName().split("\\|")[1]),
		 				selected.isSelected());				
			}
		});
	    
	    // active uniquement la premiere colonne
	    tableCheckbox.setFirstColumn();
		
	    JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
	    sidePanel.add(tableCheckbox);

	    
	    JPanel longeurEsophageResultPanel  = new JPanel();
	    longeurEsophageResultPanel.setLayout(new GridLayout(nbAcquisition+1,1));
	    longeurEsophageResultPanel.add(new JLabel("Esophageal height"));
		double[] longueurEsophage = modele.calculLongeurEsophage();
		for(int i =0 ; i< longueurEsophage.length; i++) {
			longeurEsophageResultPanel.add(new JLabel("Acquisition "+(i+1)+" : "+(ModeleScin.round(longueurEsophage[i],2)) +" cm"));
		}
	  
		JPanel longueurEsophageResultPanelFlow = new JPanel();
		longueurEsophageResultPanelFlow.setLayout(new FlowLayout());
		longueurEsophageResultPanelFlow.add(longeurEsophageResultPanel);
		   
		sidePanel.add(longueurEsophageResultPanelFlow);
	    
		JButton captureButton = new JButton("Capture");
		JLabel lblCredit = new JLabel("Provided by petctviewer.org");
		lblCredit.setVisible(false);
		
		sidePanel.add(captureButton);
		sidePanel.add(lblCredit);
		
		this.add(sidePanel, BorderLayout.EAST);
		

		
		modeleApp.esoPlugIn.setCaptureButton(captureButton, lblCredit , this, modele, "Curves");

	}
	
	public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
		 XYItemRenderer renderer = this.graphMain.getXYPlot().getRenderer();
	     //x+4  4: car on a 4 colonnes
		 renderer.setSeriesVisible((x*4)+y,  visibility);
	}
	
	 
}
