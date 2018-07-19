package org.petctviewer.scintigraphy.renal.followup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.FenResultatSidePanel;

import ij.ImagePlus;
import ij.Prefs;

public class FenApplication_FollowUp extends JFrame{

	/*tab main variable*/
	private JFreeChart leftKidneyGraph ;
	private JFreeChart rightKidneyGraph ;
	
	private String nomPatient;
	private String idPatient;
	
	private HashMap<String, Double[][]> excretionsRatios;
	
	/*tab details variable*/
	//contient les tableaux de chaque patient
	private ArrayList<HashMap<String, Double[][]>> tableaux;
	private String[] dateExamen;
	
	public FenApplication_FollowUp(ArrayList<String> chemins) throws IOException {
		
		this.setTitle("CVS");
		this.setLayout(new BorderLayout());
		this.setSize(700,500);
		
		/**Tab Main**/
		JPanel tabMain = new JPanel();
		tabMain.setLayout(new BorderLayout());

		
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
	    tabMain.add(charts);
	    
		
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
		
		JButton captureButton = new JButton("Capture");
		captureButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				captureButton.setVisible(false);
				
				Container root =  Scintigraphy.getRootContainer(captureButton);
				System.out.println(root.toString());
				
				// Capture, nouvelle methode a utiliser sur le reste des programmes
				BufferedImage capture = new BufferedImage(root.getWidth(), root.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				root.paint(capture.getGraphics());
				ImagePlus imp = new ImagePlus("capture", capture);
				imp.show();
				
				captureButton.setVisible(true);
			}
		});		
		
		sideBox.add(captureButton);
		
		tabMain.add(sideBox,BorderLayout.EAST);
		
		
		//** Tab details **/
		
		JPanel tabDetails = new JPanel();
		tabDetails.setLayout(new FlowLayout());
		
		Box allResultats = Box.createHorizontalBox();
		for(int i=0; i<tableaux.size(); i++) {
			
			Box resultats =  Box.createVerticalBox();
			JLabel date = new JLabel(dateExamen[i]);
	        date.setAlignmentX(CENTER_ALIGNMENT);
			
			date.setFont(new Font("Helvetica", Font.PLAIN, 18));
			resultats.add(date);
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));// add space

			resultats.add(setIntegralTab(tableaux.get(i).get("integral")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setTimingTab(tableaux.get(i).get("timing")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));
			
			resultats.add(setExcretionRatioTab(tableaux.get(i).get("excretion")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setRoeTab(tableaux.get(i).get("roe")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setNoraTab(tableaux.get(i).get("nora")));
			
			//pour que tableau ne soit pas etalé sur toute la fenetre
			JPanel jp = new JPanel(new FlowLayout());
			jp.add(resultats);
			
			allResultats.add(jp);
	
		}
		tabDetails.add(allResultats);
		
		
		
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Main", tabMain);
		tabbedPane.addTab("Details", tabDetails);
	   
		this.add(tabbedPane);
		this.pack();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}
	
	/**Tab main methods*/
	
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
	
	
	
	/** Tab details methods**/
	private Box setNoraTab(Double[][] nora) {
		JPanel noraTabPanel = new JPanel(new GridLayout(4,3,10,5));
		noraTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(rightLabel);
		
		for (int i = 0; i < nora.length; i++) {

			noraTabPanel.add(new JLabel(nora[i][0] + "  min"));

			for (int j = 1; j < nora[i].length; j++) {
				if (nora[i][j] != null) {
					JLabel lbl_g = new JLabel(nora[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					noraTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					noraTabPanel.add(lbl_na);
				}
			}
		}
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(noraTabPanel);
		
		Box noraBox = Box.createVerticalBox();
		JLabel title = new JLabel("NORA");
        title.setAlignmentX(CENTER_ALIGNMENT);
		noraBox.add(title);
		noraBox.add(jp);

		return noraBox;
	}
	
	private Box setExcretionRatioTab(Double[][] excr) {
		JPanel excrTabPanel = new JPanel(new GridLayout(4, 3,10,5));
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
		
		for (int i = 0; i < excr.length; i++) {

			excrTabPanel.add(new JLabel(excr[i][0] + "  min"));

			for (int j = 1; j < excr[i].length; j++) {
				if (excr[i][j] != null) {
					JLabel lbl_g = new JLabel(excr[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					excrTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					excrTabPanel.add(lbl_na);
				}
			}
		}		
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(excrTabPanel);
		
		Box excrBox = Box.createVerticalBox();
		JLabel title = new JLabel("Excretion ratio");
        title.setAlignmentX(CENTER_ALIGNMENT);
		excrBox.add(title);
		excrBox.add(jp);
		
		return excrBox;
	}
	
	private Box setTimingTab(Double[][] timing) {
		JPanel timingTabPanel = new JPanel(new GridLayout(3, 3,0,5));
		timingTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(rightLabel);
		
		
		timingTabPanel.add(new JLabel("TMax (min)"));
		
		for (int i = 0; i < timing.length; i++) {

			if(i==1) {
				timingTabPanel.add(new JLabel("T1/2(min)"));
			}
			
			for (int j = 0; j < timing[i].length; j++) {
				if (timing[i][j] != null) {
					JLabel lbl_g = new JLabel(timing[i][j]+"");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					timingTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					timingTabPanel.add(lbl_na);
				}
			}
		}		
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(timingTabPanel);
		
		Box timingBox = Box.createVerticalBox();
		JLabel title = new JLabel("Timing");
        title.setAlignmentX(CENTER_ALIGNMENT);
		timingBox.add(title);
		timingBox.add(jp);
		
		return timingBox;
	}
	
	private Box setRoeTab(Double[][] roe) {
		JPanel roeTabPanel = new JPanel(new GridLayout(4, 3,10,5));
		roeTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(rightLabel);
		

		for (int i = 0; i < roe.length; i++) {

			roeTabPanel.add(new JLabel(roe[i][0] + "  min"));

			for (int j = 1; j < roe[i].length; j++) {
				if (roe[i][j] != null) {
					JLabel lbl_g = new JLabel(roe[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					roeTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					roeTabPanel.add(lbl_na);
				}
			}
		}		
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(roeTabPanel);
		
		Box roeBox = Box.createVerticalBox();
		JLabel title = new JLabel("ROE");
        title.setAlignmentX(CENTER_ALIGNMENT);
		roeBox.add(title);
		roeBox.add(jp);
		
		return roeBox;
	}
	
	private Box setIntegralTab(Double[][] integral) {
		JPanel integralTabPanel = new JPanel(new GridLayout(2, 2,10,5));
		integralTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		integralTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		integralTabPanel.add(rightLabel);
		
		int i =0;

		for (int j = 0; j < integral[i].length; j++) {
			if (integral[i][j] != null) {
				JLabel lbl_g = new JLabel(integral[i][j] + " %");
				lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
				integralTabPanel.add(lbl_g);
			} else {
				JLabel lbl_na = new JLabel("N/A");
				lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
				integralTabPanel.add(lbl_na);
			}
		}
			
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(integralTabPanel);
				
		Box integralBox = Box.createVerticalBox();
		JLabel title = new JLabel("Relative function integral");
        title.setAlignmentX(CENTER_ALIGNMENT);
		integralBox.add(title);
		integralBox.add(jp);
		
		return integralBox;
	}
	
	public void setTableaux(ArrayList<HashMap<String, Double[][]>> tableaux) {
		this.tableaux = tableaux;
	}

	public void setDateExamen(String[] dateExamen) {
		this.dateExamen = dateExamen;
	}


	
}

