package org.petctviewer.scintigraphy.CSV;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

public class TabMain extends JPanel{
	private JFreeChart leftKidneyGraph ;
	private JFreeChart rightKidneyGraph ;
	
	private String nomPatient;
	private String idPatient;
	
	private HashMap<String, Double[][]> excretionsRatios;
	
	public TabMain(ArrayList<String> chemins) {
		
		Controleur_FollowUp_TabMain controleurTabMain = new Controleur_FollowUp_TabMain(this, chemins);
		this.setLayout(new BorderLayout());

		
		 //Changes background color
	    XYPlot plot = (XYPlot)this.leftKidneyGraph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));
	    plot = (XYPlot)this.rightKidneyGraph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));
	    
	    //graphics panel
		JPanel charts = new JPanel();
		charts.setLayout(new GridLayout(2,1));
	    charts.add(new ChartPanel(this.leftKidneyGraph),BorderLayout.CENTER);
	    charts.add(new ChartPanel(this.rightKidneyGraph),BorderLayout.CENTER);	
	    this.add(charts);
	    
		
	    //informations patient panel : put on flow 
	    JPanel patientInfo = new JPanel(new GridLayout(2, 2, 10, 10));
	    patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(nomPatient));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(idPatient));
		JPanel flowPatient = new JPanel();
		flowPatient.add(patientInfo);
		
		//title : put to flow
		JLabel titre = new JLabel("<html><h1> Follow-up </h1><html>");
		titre.setHorizontalAlignment(SwingConstants.CENTER);
		JPanel flowTitre = new JPanel();
		flowTitre.add(titre);
				
		//side with all informations
		Box sideBox = Box.createVerticalBox();
		sideBox.add(flowTitre);
		sideBox.add(flowPatient);
		
		// cle : date
		ArrayList<String> cleExcretions = new ArrayList<>(this.excretionsRatios.keySet());
		
		//tabs with excretion ratio
		for(int i =0; i< cleExcretions.size(); i++) {

			JPanel excrTabPanel = new JPanel(new GridLayout(4, 3));
			excrTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
			
			JLabel timeLabel = new JLabel("T");
			timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			excrTabPanel.add(timeLabel);
			
			JLabel leftLabel = new JLabel("L");
			leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
			excrTabPanel.add(leftLabel);
			
			JLabel rightLabel = new JLabel("R");
			rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
			excrTabPanel.add(rightLabel);
			
			Double[][] excr = excretionsRatios.get(cleExcretions.get(i));
			for (int j = 0; j < excr.length; j++) {

				excrTabPanel.add(new JLabel(excr[j][0] + "  min"));

				for (int k = 1; k < excr[j].length; k++) {
					if (excr[j][k] != null) {
						JLabel lbl_g = new JLabel(excr[j][k] + " %");
						lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
						excrTabPanel.add(lbl_g);
					} else {
						JLabel lbl_na = new JLabel("N/A");
						lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
						excrTabPanel.add(lbl_na);
					}
				}
			}
			
			Box excrBox = Box.createVerticalBox();
			excrBox.add(new JLabel("Aquisition date: "+cleExcretions.get(i)));
			excrBox.add(new JLabel(" Excretion ratio"));
			excrBox.add(excrTabPanel);
			
			sideBox.add(excrBox);
		}
		
		this.add(sideBox,BorderLayout.EAST);
	}
	
	public void createLeftKidneyGraph(XYSeriesCollection collection) {
		this.leftKidneyGraph = ChartFactory.createXYLineChart("Left Kidney","time (s)"," ",collection);	
	}
	
	public void createRightKidneyGraph(XYSeriesCollection collection) {	
		this.rightKidneyGraph = ChartFactory.createXYLineChart("Right Kidney","time (s)"," ",collection);
	}

	public void setPatientName(String name) {
		this.nomPatient = name;
	}
	
	public void setIdPatient(String id) {
		this.idPatient = id;
	}
	
	public void setExcretionsRatios(HashMap<String, Double[][]> e) {
		this.excretionsRatios = e;
	}
	
	
}
