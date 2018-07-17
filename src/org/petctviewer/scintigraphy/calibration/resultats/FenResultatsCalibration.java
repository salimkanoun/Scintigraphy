package org.petctviewer.scintigraphy.calibration.resultats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import com.itextpdf.text.List;

public class FenResultatsCalibration extends JFrame{
	
	private JPanel east ;
	private ControleurResultatsCalibration crc;
	
	private JFreeChart graph;
	 
	private JLabel aLabel;
	private JLabel bLabel;
	
	
	
	JPanel tabDetails;

	public FenResultatsCalibration(ArrayList<ArrayList<HashMap<String, Object>>> arrayList) {
		

		
		/**tab1 **/
		JPanel tabGraphics = new JPanel();
		tabGraphics.setLayout(new BorderLayout());

		
		//graph center 
	  	graph = ChartFactory.createScatterPlot(
	        "Schaefer calibration", 
	        "X = (mSUV70 - BG) / BG ", "Y = TS / (mSUV70 - BG)", null);
	  		  	
	    //Changes background color
	    XYPlot plot = (XYPlot)graph.getPlot();
	    plot.setBackgroundPaint(new Color(255,228,196));

	    
	    ChartPanel chartPanel = new ChartPanel(graph);
	    tabGraphics.add(chartPanel,BorderLayout.CENTER);
	    

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
		String[] acquiTitle = new String[arrayList.size()];
		for(int i=0;i<arrayList.size(); i++) {
			acquiTitle[i] = "Acqui "+(i+1);
		}
		String[] sphereTitle = new String[arrayList.get(0).size()];// le nombre de points dans la premiere serie
		for(int i =0; i< arrayList.get(0).size();i++) {
		sphereTitle[i] = "Sphere "+(i+1);
		}

		crc = new ControleurResultatsCalibration(this, arrayList);
		JTableCheckBox jtcb = new JTableCheckBox(acquiTitle,sphereTitle,crc);
	    
	    east = new JPanel();
	    east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
	    this.east.add(jtcb);
	    this.east.add(coef);
	    tabGraphics.add(east,BorderLayout.EAST);
		
	    
	    /**tab2**/
	    //tab details
	     tabDetails = new JPanel();
	    String[] title = {"Num Acqui ", "SUVmax","True Sphere Volume (ml)","Measured spahere Volume (ml)","Difference (ml)","Difference (%)","a"};
	    String[][] titled = {{"1","2","3","4","5","6"}};

	     JTable table = new JTable(titled,title);
	    
	    tabDetails.add(new JScrollPane(table));
	   
	    
		/**fenetre**/
		this.setTitle("Résultats Calibration");
		this.setLayout(new BorderLayout());		
			
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Graphics", tabGraphics);
		tabbedPane.addTab("Details", tabDetails);
		tabbedPane.addChangeListener(crc);
	   
		this.add(tabbedPane);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    this.pack();
		System.out.println("fen test");

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
	 
	public void setTableDetails( ArrayList<ArrayList<HashMap<String, Object>>> data) {

		DecimalFormat df = new DecimalFormat("#.##");
		DecimalFormat df2 = new DecimalFormat("#");

		this.tabDetails.removeAll();
		this.tabDetails.revalidate();
		
		this.tabDetails.setLayout(new GridLayout(2, 3,15,5));
		
	    String[] title = {"Acqui ", "SUVmax","True Volume (ml)","Measured Volume (ml)","Diff (ml)","Diff (%)"};
		//each roi
		for(int i = 0 ;i< data.get(0).size(); i++) {
			//each exam
		    ArrayList<Double> moyenneDiffPourcent = new ArrayList<>(); 

			Double[][] table = new Double[data.size()][6];
			for(int j = 0; j < data.size(); j++){
				table[j][0] = 	ModeleScin.round((double)(j+1), 0);

				table[j][1] = ModeleScin.round(((Double)data.get(j).get(i).get("SUVmax")),2);
				table[j][2] = ModeleScin.round((Double)data.get(j).get(i).get("TrueSphereVolume")/1000,2);
				table[j][3] = ModeleScin.round((Double)data.get(j).get(i).get("VolumeCalculated"),2);
				table[j][4] = ModeleScin.round((Double)data.get(j).get(i).get("VolumeCalculated") - ((Double)data.get(j).get(i).get("TrueSphereVolume")/1000),2);
				table[j][5] = ModeleScin.round( (
								table[j][4]		 / 
								((Double)data.get(j).get(i).get("TrueSphereVolume")/1000) ) *100,2);
				moyenneDiffPourcent.add(Math.abs(table[j][5]));
				//table[j][6] = df.format((Double)data.get(j).get(i).get("x"));
			}
			JTable jtable = new JTable(table,title);
			jtable.setBorder(BorderFactory.createLineBorder(Color.black,1));
			JPanel flow = new JPanel(new FlowLayout());
			flow.add(jtable);
			JPanel tab = new JPanel(new BorderLayout());
			tab.add(jtable.getTableHeader(), BorderLayout.NORTH);
			tab.add(jtable, BorderLayout.CENTER);
			System.out.println("taille :" +moyenneDiffPourcent.size());
		System.out.println("moy : "+mean(moyenneDiffPourcent.toArray(new Double[moyenneDiffPourcent.size()] )   ));
			tab.add(new JLabel(  ModeleScin.round(mean(moyenneDiffPourcent.toArray(new Double[moyenneDiffPourcent.size()] )   ),2)+  "") , BorderLayout.SOUTH);
		
			JPanel tabEtTitre = new JPanel();
			tabEtTitre.setLayout(new BoxLayout(tabEtTitre, BoxLayout.Y_AXIS));
			tabEtTitre.add(new JLabel("Sphere"+(i+1)));
			tabEtTitre.add(tab);
			this.tabDetails.add(tabEtTitre);
		}
		this.tabDetails.revalidate();
	}
	
	
	 
	public static double mean(Double[] m) {
	    Double sum = 0.0D;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	        System.out.println("i:"+i+" m : "+m[i]);
	    }
	    return sum / m.length;
	}
//swing worker
	//vue anone otrhanc tools
	 
}
