package org.petctviewer.scintigraphy.esophageus.resultats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.Scintigraphy;

public class FenResultats_EsophagealTransit extends JFrame implements  ChartMouseListener,ActionListener,ChangeListener{

	private JFreeChart graphMain;
	private JFreeChart graphTransitTime;

	private String [] titleRows;
	private String [] titleCols = {"Entier","un Tier","deux Tier", "trois Tier"};
	
	private JPanel tabMain,tabTransitTime;
	private Selector startSelec, endSelec ;
	private	JLabel surfaceLabel;
	
	private Modele_Resultats_EsophagealTransit 		modele ;

	 JRadioButton[] jb;
	 
	 double[][] selectors;
	 
	 private static int numSeriesSelctors= 0;
	 JLabel [] n;
	 JValueSetter valueSetter;
	/*
	 * un partie main avec graph main et un jtablecheckbox main
	 * un partie transit time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier) et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList) {
		
		
		modele = new Modele_Resultats_EsophagealTransit(arrayList);
		
		this.setLayout(new BorderLayout());
		
		/*********** tab main ************/
		tabMain = new JPanel();
		this.tabMain.setLayout(new BorderLayout());
		
		//graph center 
	  	 graphMain = ChartFactory.createXYLineChart( "Esophageal Transit", "s", "Count/s", null);
	  	 
	    //Changes background color
	    XYPlot plot = (XYPlot)graphMain.getPlot();
	    //plot.setBackgroundPaint(new Color(255,228,196));
	    plot.setBackgroundPaint(new Color(255,255,255)); 
	    
	    ChartPanel chartPanel = new ChartPanel(graphMain);	    
	    this.tabMain.add(chartPanel,BorderLayout.CENTER);
	    
	    this.setMainGraphDataset(this.modele.getDataSetMain());
	    
	    
			
		
	    JTableCheckBox d = new JTableCheckBox(titleRows, titleCols, this);
		
		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
	    east.add(d);
		
		this.tabMain.add(east, BorderLayout.EAST);


		
		/********** tab transit time **********/
	    
		tabTransitTime = new JPanel();
		tabTransitTime.setLayout(new BorderLayout());
		
		//graph center 
		graphTransitTime = ChartFactory.createXYLineChart( "Transit Time", "s", "Count/s", null);
	       
	    ChartPanel chartPanelt = new ChartPanel(graphTransitTime);	    
	    this.tabTransitTime.add(chartPanelt,BorderLayout.CENTER);
		
	    
		this.setTransitTimeDataset(modele.getDataSetTransitTime());

		for(int i =0 ;i<arrayList.size(); i++) {
				this.setVisibilitySeriesTransitTime(i, false);				
		}
	    
		 valueSetter = new JValueSetter(graphTransitTime);
		valueSetter.addChartMouseListener(this);
		
		startSelec = new Selector("start", 0, -1, RectangleAnchor.TOP_LEFT);
		valueSetter.addSelector(startSelec, "start");
		endSelec = new Selector("end", 1, -1, RectangleAnchor.TOP_LEFT);
		valueSetter.addSelector(endSelec, "end");
		valueSetter.addArea("start", "end", "area", null);
		
		
		
		// liste contenant les couples de valeurs des selecteurs
		selectors = new double[arrayList.size()][2];
		for(int i = 0 ; i< selectors.length; i++) {
			//couple des valeurs d'un slecteur
			 selectors[i][0] = 0;
			 selectors[i][1] = 2;
		}
		
		 
		
		
		
		
		
	    this.tabTransitTime.add(valueSetter,BorderLayout.CENTER);

	    
	    
	    
	    JPanel selection = new JPanel();
		selection.setLayout(new GridLayout(arrayList.size(), 1));

	    surfaceLabel = new JLabel("diff");
		
	    //selection.add(surfaceLabel);
	    
	    
	    ButtonGroup bg = new ButtonGroup();
	    
	     n = new JLabel[arrayList.size()];
	     jb = new JRadioButton[arrayList.size()];
	    
	     
	    for(int i =0; i< arrayList.size(); i++) {
	    	JPanel un = new JPanel();
	    	un.setLayout(new FlowLayout());

	    	jb[i] = new JRadioButton("Acquisition "+i);
	    	jb[i].addActionListener(this);
			bg.add(jb[i]);
	    	un.add(jb[i]);
	    	
	    	n[i] = new JLabel("mesure = ?");
	    	un.add(n[i]);
	    	
	    	
	    	selection.add(un);
	    
	    }
	  
	    
	    //un selecteur pour tous les acqui
	    //
		JPanel flow = new JPanel(new FlowLayout());
		flow.add(selection);			    
			    
		this.tabTransitTime.add(flow, BorderLayout.EAST);
		
		/****************/
		
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Main", this.tabMain);	
		tabbedPane.addTab("Transit Time", this.tabTransitTime);	   

		this.add(tabbedPane);
		
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
			titleRows[i] = "Acqui "+i;
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
		return Math.abs(this.startSelec.getXValue() - this.endSelec.getXValue());
	}



	
	public void setLabelValue(double value) {
		this.surfaceLabel.setText("Difference :"+	ModeleScin.round(value, 2));
	}
	//radio button

	@Override
	public void actionPerformed(ActionEvent e) {
		for(int  i =0; i< this.jb.length; i++) {
			if(e.getSource() instanceof JRadioButton) {
				if(((JRadioButton)e.getSource()).equals(jb[i]) ) {
					//System.out.println("jb i : "+i);
					this.setVisibilitySeriesTransitTime(i, true);
					numSeriesSelctors = i;
					this.startSelec.setXValue(selectors[i][0]);
					this.endSelec.setXValue(selectors[i][1]);
					this.valueSetter.updateAreas();
					
	
				}else {
					this.setVisibilitySeriesTransitTime(i, false);
				}
			}
		}
		
	}
	
	/*
	 * au clic sur un selecteur(non-Javadoc)
	 */
	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		//System.out.println("clik"+this.fen.getDelta());
		this.setLabelValue(this.getDelta());
		this.selectors[numSeriesSelctors][0] = this.startSelec.getXValue();
		this.selectors[numSeriesSelctors][1] = this.endSelec.getXValue();
		
		this.n[numSeriesSelctors].setText(ModeleScin.round(this.getDelta(), 2)+" sec");

	}
	
	
	/*
	 * actualise la valuer a chaque mouvement de la souris sur le graphe meme si on a pas selectionné un selecteur(non-Javadoc)
	 * consomme trop de ressource
	 */
	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		//System.out.println("mov"+this.fen.getDelta());		
	}

	
	
	private void setVisibilitySeriesTransitTime(int x, boolean visibility) {
		 XYItemRenderer renderer = this.graphTransitTime.getXYPlot().getRenderer();
	     //x+4  4: car on a 4 colonnes
		 renderer.setSeriesVisible(x,  visibility);
	}
	//method appelé lors d'un appui sur une checkbox

	@Override
	public void stateChanged(ChangeEvent e) {
		JCheckBox selected = (JCheckBox)e.getSource();
		
		this.setVisibilitySeriesMain(	
				Integer.parseInt(selected.getName().split("\\|")[0]), 
 				Integer.parseInt(selected.getName().split("\\|")[1]),
 				selected.isSelected());
	}

	
}
