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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.IJ;
import ij.ImagePlus;


public class FenApplication_FollowUp extends JFrame{

	private static Color[] color = {Color.RED, Color.BLUE, Color.GREEN};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*tab main variable*/
	private JFreeChart leftKidneyGraph ;
	private JFreeChart rightKidneyGraph ;
	
	private String nomPatient;
	private String idPatient;
		
	/*tab details variable*/
	//contient les tableaux de chaque patient
	private HashMap<String, HashMap<String,Object>> allExamens;

	// true : normalized axis
	// false : standard axis
	private boolean axisType;

	private JButton graphButton;
	
	public FenApplication_FollowUp(ArrayList<String> chemins) throws IOException {
		
		new Controleur_FollowUp(this, chemins);
		
		this.setTitle("CVS");
		getContentPane().setLayout(new BorderLayout());
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
	    
		
		
		//title : put to flow
		JLabel titre = new JLabel("<html><h1> Follow-up </h1><html>");
		titre.setHorizontalAlignment(SwingConstants.CENTER);
		JPanel flowTitre = new JPanel();
		flowTitre.add(titre);
				
		//side with all informations
		Box sideBox = Box.createVerticalBox();
		sideBox.add(flowTitre);
		sideBox.add(patientInfoPanel());
		
		// cle : date
		ArrayList<String> cleAllExamens = new ArrayList<>(this.allExamens.keySet());
		
		//tabs with excretion ratio
		Box excretionTabFlow = Box.createVerticalBox();
		//for each date
		for(int i =0; i< cleAllExamens.size(); i++) {
			excretionTabFlow.add(setExcretionRatioTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("excretion")));
		}
		sideBox.add(excretionTabFlow);
	
		
		
		
		this.axisType = false;
		 graphButton = new JButton("standard axis");
		graphButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FenApplication_FollowUp fen = FenApplication_FollowUp.this;
				if(fen.axisType == false) {
					FenApplication_FollowUp.this.graphButton.setText("normalized axis");
					FenApplication_FollowUp.this.setNormalizedAxis(leftKidneyGraph);
					FenApplication_FollowUp.this.setNormalizedAxis(rightKidneyGraph);
					fen.axisType = true;
				}else {
					FenApplication_FollowUp.this.graphButton.setText("standard axis");
					FenApplication_FollowUp.this.setStandardAxis(leftKidneyGraph);
					FenApplication_FollowUp.this.setStandardAxis(rightKidneyGraph);
					fen.axisType = false;
				}
				
			}
		});
		sideBox.add(graphButton);
		sideBox.add(setCaptureButton());
		
		tabMain.add(sideBox,BorderLayout.EAST);
		
		
		//** Tab details **/
		JPanel tabDetails = new JPanel();
		tabDetails.setLayout(new BorderLayout());
		
		Box allResultats = Box.createHorizontalBox();
		//for each date 
		for(int i=0; i<cleAllExamens.size(); i++) {
			
			Box resultats =  Box.createVerticalBox();
			JLabel date = new JLabel(cleAllExamens.get(i));
	        date.setAlignmentX(CENTER_ALIGNMENT);
			
			date.setFont(new Font("Helvetica", Font.PLAIN, 18));
			resultats.add(date);
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));// add space

			resultats.add(setIntegralTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("integral")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setTimingTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("timing")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));
			
			resultats.add(setExcretionRatioTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("excretion")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setRoeTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("roe")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setNoraTab((Double[][])allExamens.get(cleAllExamens.get(i)).get("nora")));
			
			//pour que tableau ne soit pas etalé sur toute la fenetre
			JPanel jp = new JPanel(new FlowLayout());
			jp.add(resultats);
			
			allResultats.add(jp);
		}
		
		JPanel captureButtonFlow = new JPanel(new FlowLayout());
		captureButtonFlow.add(setCaptureButton());

		JPanel patientInfoFlow = new JPanel(new FlowLayout());
		patientInfoFlow.add(patientInfoPanel());
		
		JPanel allResultatsFlow = new JPanel(new FlowLayout());
		allResultatsFlow.add(allResultats);

		
		tabDetails.add(patientInfoFlow, BorderLayout.WEST);
		tabDetails.add(allResultatsFlow, BorderLayout.CENTER);
		tabDetails.add(captureButtonFlow, BorderLayout.EAST);

		
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Main", tabMain);
		tabbedPane.addTab("Details", tabDetails);
	   
		getContentPane().add(tabbedPane);
		this.pack();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}
	
	/**Tab main methods*/
	
	public void createLeftKidneyGraph(XYSeriesCollection collection) {
		this.leftKidneyGraph = ChartFactory.createXYLineChart("Left Kidney","time (s)"," ",collection);	
		XYPlot plot = leftKidneyGraph.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,false);
		plot.setRenderer(renderer);

		for(int i =0;i<collection.getSeriesCount(); i++) {
		    plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(i, color[i]);
		}
	}
	
	public void setNormalizedAxis(JFreeChart graph) {
		XYPlot plot = graph.getXYPlot();
		XYSeriesCollection collection = (XYSeriesCollection) plot.getDataset();
		
		
		// toutes les series sont dans un dataset 
		//donc onva creer un dataset par serie
		for(int i =0; i< collection.getSeriesCount(); i++) {
			plot.setDataset(i,null);

			plot.setDataset(i, new XYSeriesCollection( collection.getSeries(i)));

			
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,false);
			plot.setRenderer(i,renderer);
			
			plot.getRendererForDataset(plot.getDataset(i)).setSeriesPaint(0, color[i]);
			
		
			plot.setRangeAxis(i, new NumberAxis(collection.getSeriesKey(i)+""));
		}

	    plot.setDomainAxis(new NumberAxis("X Axis"));
	    
	  //Map the data to the appropriate axis
	    plot.mapDatasetToRangeAxis(0, 0);
	    plot.mapDatasetToRangeAxis(1, 1);
	    plot.mapDatasetToRangeAxis(2, 2);

	}
	
	public void setStandardAxis(JFreeChart graph) {
		XYPlot plot = graph.getXYPlot();
		XYSeriesCollection collectionMemoire [] = new XYSeriesCollection[plot.getDatasetCount()];
		
		//on memorise les dataset
		for(int i=0; i< collectionMemoire.length; i++) {
			collectionMemoire[i] = (XYSeriesCollection)plot.getDataset(i);
			// on enleve les dataset d'avant
			plot.setDataset(i,null);
		}

		XYSeriesCollection collectionRes = new XYSeriesCollection();

		// on remet toutes les series dans un dataset
		for(int i =0; i<plot.getDatasetCount(); i++ ) {
			collectionRes.addSeries(collectionMemoire[i].getSeries(0));
			plot.getRenderer().setSeriesPaint(i, color[i]);

		}
		
		plot.setDataset(collectionRes);

		
		NumberAxis xAxis = new NumberAxis("");
        xAxis.setAutoRangeIncludesZero(false);
       
		plot.setRangeAxis(1, null);
		plot.setRangeAxis(2, null);
		

		
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
	
	public void setAllExamens(HashMap<String,HashMap<String, Object>> allExamens) {
		this.allExamens = allExamens;
	}

	private JButton setCaptureButton() {
		JButton captureButton = new JButton("Capture");
		captureButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String partie1 = (String)allExamens.get(Collections.max(allExamens.keySet())).get("tags");
				Container root =  Library_Capture_CSV.getRootContainer(captureButton);
				
				captureButton.setVisible(false);
			
				// Capture, nouvelle methode a utiliser sur le reste des programmes
				BufferedImage capture = new BufferedImage(root.getWidth(), root.getHeight(),BufferedImage.TYPE_INT_ARGB);
				root.paint(capture.getGraphics());	
				ImagePlus imp = new ImagePlus("capture", capture);
				String partie2=Library_Capture_CSV.genererDicomTagsPartie2(imp);
				imp.setProperty("Info", partie1+partie2);
				imp.show();
				imp.getWindow().toFront();
				
				captureButton.setVisible(true);
				
				//On propose de sauver la capture en DICOM
				IJ.run("myDicom...");
				
				
			}
		});		
		return captureButton;
	}

	private JPanel patientInfoPanel() {
	    //informations patient panel : put on flow 
	    JPanel patientInfo = new JPanel(new GridLayout(2, 2, 10, 10));
	    patientInfo.add(new JLabel("Patient name: "));
		patientInfo.add(new JLabel(nomPatient));
		patientInfo.add(new JLabel("Patient id: "));
		patientInfo.add(new JLabel(idPatient));
		JPanel flowInfoPatient = new JPanel();
		flowInfoPatient.add(patientInfo);
		return flowInfoPatient;
	}
}

