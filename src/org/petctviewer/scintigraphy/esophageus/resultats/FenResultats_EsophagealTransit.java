package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public class FenResultats_EsophagealTransit extends JFrame implements  ChartMouseListener,ActionListener,ChangeListener{

	private JFreeChart graphMain;
	private JFreeChart graphTransitTime;
	private JFreeChart graphRetention;
	
	private String [] titleRows;
	private String [] titleCols = {"Full","Upper","Middle", "Lower"};
	
	private JPanel tabMain,tabTransitTime, tabRetention;
	private Selector startSelector, endSelector ;
	private	JLabel surfaceLabel;
	
	private Modele_Resultats_EsophagealTransit 		modele ;

	 JRadioButton[] radioButtonTransitTime;
	 
	 double[][] selectors;
	 
	 private static int numSeriesSelctors= 0;
	 JLabel [] labelsMesureTempsSelectorTransit;
	 JValueSetter valueSetterTransit;
	 
	 
	 /* Retention */
	 private JRadioButton [] radioButtonRetention;
	 
	 /*Condensé*/
	 //Spinners
	 private JSpinner spinnerRight;
	 private JSpinner spinnerLeft;
	 JPanel tabCondenseDynamique;
	 DynamicImage imageCondensePanel;
	 
	 int rightRognageValue[];
	 int leftRognageValue[];
	 DynamicImage imageProjeterEtRoiPanel;
	 
	 JRadioButton [] radioButtonCondense;
	 
	 private static int numAcquisitionCondense = 0;
	 
	/*
	 * un partie main avec graph main et un jtablecheckbox main
	 * un partie transit time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier) et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList, ArrayList<Object[]> dicomRoi) {
		
		modele = new Modele_Resultats_EsophagealTransit(arrayList,dicomRoi);
		this.setLayout(new BorderLayout());
		
		/********************************************************** tab main ************/
		tabMain = new JPanel();
		this.tabMain.setLayout(new BorderLayout());
		
		//graph  
	  	graphMain = ChartFactory.createXYLineChart( "Esophageal Transit", "s", "Count/s", null);
	  	 
	  	XYLineAndShapeRenderer rendererMain = new XYLineAndShapeRenderer();
 	  	rendererMain.setDefaultShapesVisible(true);
 	  	for(int i =0; i< arrayList.size(); i++) {
 	 	  	rendererMain.setSeriesShape(i, ShapeUtils.createDiagonalCross(3, 1));
 	 	  	//System.out.println('i'+i);
 	  	}
 		graphMain.getXYPlot().setDomainGridlinePaint(Color.black);
 	  	graphMain.getXYPlot().setRangeGridlinePaint(Color.black);
 	  	rendererMain.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
 	  	graphMain.getXYPlot().setRenderer(rendererMain);
 	  
	    //Changes background color et grid color
	    this.graphMain.getXYPlot().setBackgroundPaint(new Color(255,255,255)); 	    
		
		 
	    
	    ChartPanel chartPanel = new ChartPanel(graphMain);	    
	    this.tabMain.add(chartPanel,BorderLayout.CENTER);
	    
	    this.setMainGraphDataset(this.modele.getDataSetMain());
	    
	    // table de checkbox	
	    JTableCheckBox tableCheckboxMain = new JTableCheckBox(titleRows, titleCols, this);
	    
	    tableCheckboxMain.setFirstColumn();
		
		JPanel sideMain = new JPanel();
		sideMain.setLayout(new BoxLayout(sideMain, BoxLayout.Y_AXIS));
	    sideMain.add(tableCheckboxMain);
		
		this.tabMain.add(sideMain, BorderLayout.EAST);


		/******************************************************** tab transit time **********/
	    
		tabTransitTime = new JPanel();
		tabTransitTime.setLayout(new BorderLayout());
		
		//graph  
		graphTransitTime = ChartFactory.createXYLineChart( "Transit Time", "s", "Count/s", null);
		 //Changes background color et grid color
	    this.graphTransitTime.getXYPlot().setBackgroundPaint(new Color(255,255,255)); 	    
		this.graphTransitTime.getXYPlot().setRangeGridlinePaint(Color.black);
		this.graphTransitTime.getXYPlot().setDomainGridlinePaint(Color.black);
		
	    ChartPanel chartTransitPanel = new ChartPanel(graphTransitTime);	    
	    this.tabTransitTime.add(chartTransitPanel,BorderLayout.CENTER);
		
		this.setTransitTimeDataset(modele.getDataSetTransitTime());

		// rend toutes les coubres visible
		for(int i =0 ;i<arrayList.size(); i++) {
				this.setVisibilitySeriesGraph(this.graphTransitTime,i, false);				
		}
	    
		//graph avec les selecteur
		valueSetterTransit = new JValueSetter(graphTransitTime);
		valueSetterTransit.addChartMouseListener(this);
		
		startSelector = new Selector("start", 0, -1, RectangleAnchor.TOP_LEFT);
		valueSetterTransit.addSelector(startSelector, "start");
		endSelector = new Selector("end", 1, -1, RectangleAnchor.TOP_LEFT);
		valueSetterTransit.addSelector(endSelector, "end");
		valueSetterTransit.addArea("start", "end", "area", null);

		// liste contenant les couples de valeurs des selecteurs
		selectors = new double[arrayList.size()][2];
		for(int i = 0 ; i< selectors.length; i++) {
			//couple des valeurs d'un slecteur
			 selectors[i][0] = 0;
			 selectors[i][1] = 2;
		}
		
	    this.tabTransitTime.add(valueSetterTransit,BorderLayout.CENTER);

	    //Panel de selection des acquisitions (side panel)
	    JPanel selectionAcquiTransitPanel = new JPanel();
		selectionAcquiTransitPanel.setLayout(new GridLayout(arrayList.size(), 1));

	    ButtonGroup buttonGroupTransit = new ButtonGroup(); 
	    labelsMesureTempsSelectorTransit = new JLabel[arrayList.size()];
	    radioButtonTransitTime = new JRadioButton[arrayList.size()];
	    
	     
	    for(int i =0; i< arrayList.size(); i++) {
		    //un selecteur pour tous les acqui
	    	JPanel un = new JPanel();
	    	un.setLayout(new FlowLayout());

	    	radioButtonTransitTime[i] = new JRadioButton("Acquisition "+(i+1));
	    	radioButtonTransitTime[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					for(int  i =0; i< FenResultats_EsophagealTransit.this.radioButtonTransitTime.length; i++) {
						if(((JRadioButton)e.getSource()).equals(radioButtonTransitTime[i]) ) {
							//System.out.println("jb i : "+i);
							FenResultats_EsophagealTransit.this.setVisibilitySeriesGraph(FenResultats_EsophagealTransit.this.graphTransitTime,i, true);
							numSeriesSelctors = i;
							FenResultats_EsophagealTransit.this.startSelector.setXValue(selectors[i][0]);
							FenResultats_EsophagealTransit.this.endSelector.setXValue(selectors[i][1]);
							FenResultats_EsophagealTransit.this.valueSetterTransit.updateAreas();
							
			
						}else {
							FenResultats_EsophagealTransit.this.setVisibilitySeriesGraph(FenResultats_EsophagealTransit.this.graphTransitTime,i, false);
						}
					}				
				}
			});
			buttonGroupTransit.add(radioButtonTransitTime[i]);
	    	un.add(radioButtonTransitTime[i]);
	    	
	    	labelsMesureTempsSelectorTransit[i] = new JLabel("measure = ?");
	    	un.add(labelsMesureTempsSelectorTransit[i]);
	    	
	    	selectionAcquiTransitPanel.add(un);
	    }
	  
	    //pour quil soit regroupe
		JPanel selectionAcquiTransitPanelFlow = new JPanel(new FlowLayout());
		selectionAcquiTransitPanelFlow.add(selectionAcquiTransitPanel);			    
			    
		this.tabTransitTime.add(selectionAcquiTransitPanelFlow, BorderLayout.EAST);
		
		/*******************************************************************tab retention ************/
		this.tabRetention = new JPanel();
		this.tabRetention.setLayout(new BorderLayout());
		
		
		//graph center 
		 graphRetention = ChartFactory.createXYLineChart( "Retention", "s", "Count/s", null);
		 XYLineAndShapeRenderer rendererTransit = new XYLineAndShapeRenderer();
		 rendererTransit.setSeriesShapesVisible(0, true);
		 rendererTransit.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		 graphRetention.getXYPlot().setRenderer(rendererTransit);
		 
		 this.graphRetention.getXYPlot().setBackgroundPaint(new Color(255,255,255)); 	    
		 this.graphRetention.getXYPlot().setRangeGridlinePaint(Color.black);
		 this.graphRetention.getXYPlot().setDomainGridlinePaint(Color.black);
	
	 
		 
		 ChartPanel chartRetentionPanel = new ChartPanel(graphRetention);	    
	    this.tabRetention.add(chartRetentionPanel,BorderLayout.CENTER);
		
		graphRetention.getXYPlot().setDataset(this.modele.retention2());


		JPanel radioButtonRetentionPanel = new JPanel();
		radioButtonRetentionPanel.setLayout(new GridLayout(arrayList.size(), 1));
		
	    ButtonGroup buttonGroupRetention = new ButtonGroup();    
	     radioButtonRetention = new JRadioButton[arrayList.size()];
	    for(int i =0; i< arrayList.size(); i++) {
	    	radioButtonRetention[i] = new JRadioButton("Acquisition "+(i+1));
	    	radioButtonRetention[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					for(int i =0; i<FenResultats_EsophagealTransit.this.radioButtonRetention.length; i++) {
						if(((JRadioButton)e.getSource()).equals(radioButtonRetention[i])) {
							FenResultats_EsophagealTransit.this.setVisibilitySeriesGraph(FenResultats_EsophagealTransit.this.graphRetention, i, true);

						}else {
							FenResultats_EsophagealTransit.this.setVisibilitySeriesGraph(FenResultats_EsophagealTransit.this.graphRetention, i, false);
						}
					}	
				}
			});
	    	buttonGroupRetention.add(radioButtonRetention[i]);
	    	radioButtonRetentionPanel.add( radioButtonRetention[i]);
	    }
	    
	    JPanel radioButtonRetentionPanelFlow = new JPanel();
		radioButtonRetentionPanelFlow.setLayout(new FlowLayout());
		radioButtonRetentionPanelFlow.add(radioButtonRetentionPanel);
	    
		Box sideRetentionPanel = Box.createVerticalBox();
	    sideRetentionPanel.add(radioButtonRetentionPanelFlow);
	    
	    
	    JPanel retentionResultPanel  = new JPanel();
	    retentionResultPanel.setLayout(new GridLayout(arrayList.size()+1,1));
	   
	    retentionResultPanel.add(new JLabel("Decrease 10s after peak"));
		double[] retention10s = this.modele.retention();
		for(int i =0 ; i< retention10s.length; i++) {
			retentionResultPanel.add(new JLabel("Acquisition "+(i+1)+" : "+(retention10s[i]*100) +"%"));
		}
	    
		
		JPanel retentionResultPanelFlow = new JPanel();
		retentionResultPanelFlow.setLayout(new FlowLayout());
		retentionResultPanelFlow.add(retentionResultPanel);
		
		sideRetentionPanel.add(retentionResultPanelFlow);
	    
	    
		this.tabRetention.add(sideRetentionPanel, BorderLayout.EAST);
		
				
		/******************************************************** Tab condanse dynamique**/
		
		 this.rightRognageValue = new int[arrayList.size()];
		 this.leftRognageValue = new int[arrayList.size()];
		 
		 tabCondenseDynamique = new JPanel();
		 tabCondenseDynamique.setLayout(new BorderLayout());
		 
		 this.modele.calculAllCondense();
		 imageCondensePanel = new DynamicImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());
		 imageCondensePanel.setLayout( new BorderLayout());

		 tabCondenseDynamique.add(imageCondensePanel);
		
	

		JPanel spinnerPanel = new JPanel();
		spinnerPanel.add(new JLabel("Left side"));
		spinnerLeft = new JSpinner();    
		spinnerLeft.addChangeListener(this);
		spinnerPanel.add(spinnerLeft);
		spinnerPanel.add(new JLabel("Right side"));
		spinnerRight = new JSpinner();
		spinnerRight.addChangeListener(this);
		spinnerPanel.add(spinnerRight);
		
		this.modele.calculAllImagePlusAndRoi();
		imageProjeterEtRoiPanel = new DynamicImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
		imageProjeterEtRoiPanel.setLayout(new BorderLayout());
		
		JPanel imagePlusRognagePanel = new JPanel();
		imagePlusRognagePanel.setLayout(new BorderLayout());
		imagePlusRognagePanel.add(spinnerPanel, BorderLayout.NORTH);
		imagePlusRognagePanel.add(imageProjeterEtRoiPanel, BorderLayout.CENTER);

		JPanel radioButtonCondensePanel = new JPanel();
		radioButtonCondensePanel.setLayout(new GridLayout(arrayList.size(), 1));
		
	    ButtonGroup buttonGroupCondense = new ButtonGroup();    
	    radioButtonCondense = new JRadioButton[arrayList.size()];
	    for(int i =0; i< arrayList.size(); i++) {
	    	radioButtonCondense[i] = new JRadioButton("Acquisition "+(i+1));
	    	radioButtonCondense[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					for(int i =0; i<FenResultats_EsophagealTransit.this.radioButtonCondense.length; i++) {
						if(((JRadioButton)e.getSource()).equals(radioButtonCondense[i])) {
							numAcquisitionCondense = i;
						
							spinnerLeft.removeChangeListener(FenResultats_EsophagealTransit.this);
							
							
							spinnerLeft.setValue(leftRognageValue[numAcquisitionCondense]);
							spinnerRight.setValue(rightRognageValue[numAcquisitionCondense]);
							
							spinnerLeft.addChangeListener(FenResultats_EsophagealTransit.this);
							
							//imageProjeterEtRoiPanel.removeAll();
							imageProjeterEtRoiPanel.setImage(FenResultats_EsophagealTransit.this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

							//imageCondensePanel.removeAll();
							imageCondensePanel.setImage(FenResultats_EsophagealTransit.this.modele.getCondense(numAcquisitionCondense).getBufferedImage());
							
						}
					}
				}
			});
	    	buttonGroupCondense.add(radioButtonCondense[i]);
	    	radioButtonCondensePanel.add( radioButtonCondense[i]);
	    }
	   
	    
	    JPanel radioButtonCondensePanelFlow = new JPanel();
		radioButtonCondensePanelFlow.setLayout(new FlowLayout());
		radioButtonCondensePanelFlow.add(radioButtonCondensePanel);
		
		
		//slider de contraste
		JSlider contrastSlider = new JSlider(SwingConstants.HORIZONTAL,0,20,4);
		JLabel contrastLabel = new JLabel("Contrast");
		
		contrastSlider.addChangeListener(this);
		
	    
		JPanel  sideCondensePanel = new JPanel();
		sideCondensePanel.setLayout(new BorderLayout());
		sideCondensePanel.add(radioButtonCondensePanelFlow, BorderLayout.NORTH);
		sideCondensePanel.add(imagePlusRognagePanel, BorderLayout.CENTER);
		sideCondensePanel.add(contrastSlider,BorderLayout.SOUTH);
		
		tabCondenseDynamique.add(sideCondensePanel, BorderLayout.EAST);
		/****************/
		
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Curves", this.tabMain);	
		tabbedPane.addTab("Transit Time", this.tabTransitTime);	 
		tabbedPane.addTab("Retention", this.tabRetention);
		tabbedPane.addTab("Condensed Dynamic images", tabCondenseDynamique);

		this.add(tabbedPane);
		
		radioButtonTransitTime[0].setSelected(true);
		radioButtonRetention[0].setSelected(true);
		radioButtonCondense[0].setSelected(true);

		this.pack();
	}
	
	/**************** Tab main ************/
	
	public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
		 XYItemRenderer renderer = this.graphMain.getXYPlot().getRenderer();
	     //x+4  4: car on a 4 colonnes
		 renderer.setSeriesVisible((x*4)+y,  visibility);
	}
	
	/*
	 * Permet de mettre a jour les courbes sur le JFreeChartde la tab main
	 */
	public void setMainGraphDataset(XYSeries[][] dataset) {
		XYSeriesCollection d = new XYSeriesCollection();
		titleRows = new String [dataset.length];
		
		for(int i =0; i< dataset.length; i++) {
			titleRows[i] = "Acqui "+(i+1);
			for(int j =0; j<dataset[i].length; j++) {
				//System.out.println("i: "+i+" j: "+j);
				d.addSeries(dataset[i][j]);
			}
		}
		
		this.graphMain.getXYPlot().setDataset(d);
	
		//this.graph.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);	
	}
	 
	/***************** transit time ************/
	
	public void setTransitTimeDataset(XYSeries[][] dataset) {
		XYSeriesCollection m = new XYSeriesCollection();
	//	titleRows = new String [dataset.length];
		
		for(int i =0; i< dataset.length; i++) {
		//	titleRows[i] = "Acqui "+i;
			for(int j =0; j<dataset[i].length; j++) {
				//System.out.println("i: "+i+" j: "+j);
				m.addSeries(dataset[i][j]);
			}
		}
		 //Changes background color
	    XYPlot plott = (XYPlot)graphTransitTime.getPlot();
	    //plot.setBackgroundPaint(new Color(255,228,196));
	    plott.setBackgroundPaint(new Color(255,255,255)); 
	
		this.graphTransitTime.getXYPlot().setDataset(m);
				
	}
	
	/*
	 * retourne la difference de value sur X entre les deux selecteur
	 */
	public double getDelta() {
		return Math.abs(this.startSelector.getXValue() - this.endSelector.getXValue());
	}

	/********************* Listener ************************/
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	/*
	 * au clic sur un selecteur(non-Javadoc)
	 */
	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		//System.out.println("clik"+this.fen.getDelta());
		this.selectors[numSeriesSelctors][0] = this.startSelector.getXValue();
		this.selectors[numSeriesSelctors][1] = this.endSelector.getXValue();
		
		this.labelsMesureTempsSelectorTransit[numSeriesSelctors].setText(ModeleScin.round(this.getDelta(), 2)+" sec");

	}
	
	/*
	 * actualise la valuer a chaque mouvement de la souris sur le graphe meme si on a pas selectionné un selecteur(non-Javadoc)
	 * consomme trop de ressource
	 */
	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
	}

	/*method appelé :
	 * - lors d'un appui sur une checkbox
	* - lors d'un changement de valeur des spinner 
	*/
	@Override
	public void stateChanged(ChangeEvent e) {
		
		System.out.println("statechange");
		if(e.getSource() instanceof JCheckBox) {
			JCheckBox selected = (JCheckBox)e.getSource();
			
			this.setVisibilitySeriesMain(	
					Integer.parseInt(selected.getName().split("\\|")[0]), 
	 				Integer.parseInt(selected.getName().split("\\|")[1]),
	 				selected.isSelected());
		}else if(e.getSource() instanceof JSlider) {
			System.out.println("slider :"+((JSlider)e.getSource()).getValue());

			//changement de contraste
			ContrastEnhancer ce = new ContrastEnhancer();
					
			ce.stretchHistogram(this.modele.getImagePlusAndRoi(numAcquisitionCondense), ((JSlider)e.getSource()).getValue());
			imageProjeterEtRoiPanel.setImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

			ce.stretchHistogram(this.modele.getCondense(numAcquisitionCondense), ((JSlider)e.getSource()).getValue());
			imageCondensePanel.setImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());
		}else 	if( e.getSource() instanceof JSpinner) {
			JSpinner spinner = (JSpinner)e.getSource();
			if(spinner.equals(spinnerRight)) {

				//System.out.println("pinner right "+((int)spinner.getValue()- this.rightRognageValue));
				
				this.modele.rognerDicomCondenseRight((int)spinner.getValue()- this.rightRognageValue[numAcquisitionCondense],numAcquisitionCondense);
				this.rightRognageValue[numAcquisitionCondense] = (int)spinner.getValue();
				
				 this.modele.calculCond(numAcquisitionCondense);
				 imageCondensePanel.setImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());

				 
				 this.modele.calculImagePlusAndRoi(numAcquisitionCondense);
				 imageProjeterEtRoiPanel.setImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());

			}else if(spinner.equals(spinnerLeft)) {
				//System.out.println("pinner left "+spinner.getValue());
				
				this.modele.rognerDicomCondenseLeft((int)spinner.getValue()- this.leftRognageValue[numAcquisitionCondense],numAcquisitionCondense);
				this.leftRognageValue[numAcquisitionCondense] = (int)spinner.getValue();
				 
				this.modele.calculCond(numAcquisitionCondense);
				 imageCondensePanel.setImage(this.modele.getCondense(numAcquisitionCondense).getBufferedImage());

				 this.modele.calculImagePlusAndRoi(numAcquisitionCondense);
				 imageProjeterEtRoiPanel.setImage(this.modele.getImagePlusAndRoi(numAcquisitionCondense).getBufferedImage());
			}
			
		
		}
		
	}

	
	
	/******* Tools *******/
	private void setVisibilitySeriesGraph(JFreeChart graph, int numSerie, boolean visibility) {
	//	System.out.println("visibility nummserie:" + numSerie + " Visi : "+visibility +" title "+ graph.getTitle());
		 XYItemRenderer renderer = graph.getXYPlot().getRenderer();
		 renderer.setSeriesVisible(numSerie,  visibility);
		 renderer.setSeriesPaint(numSerie, Color.red);

	}
	
}
