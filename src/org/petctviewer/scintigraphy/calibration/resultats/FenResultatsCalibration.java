package org.petctviewer.scintigraphy.calibration.resultats;

import ij.ImagePlus;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FenResultatsCalibration extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JFreeChart graph;
	 
	private final JLabel aLabel;
	private final JLabel bLabel;
	
	private final JPanel tabDetails;

	public FenResultatsCalibration(ArrayList<ArrayList<HashMap<String, Object>>> arrayList) {
			
		/*tab1 */
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

		ControleurResultatsCalibration crc = new ControleurResultatsCalibration(this, arrayList);
		JTableCheckBox jtcb = new JTableCheckBox(acquiTitle,sphereTitle, crc);

		JPanel east = new JPanel();
	    east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
	    east.add(jtcb);
	    east.add(coef);
	    
	    JFrame frame = this;
	    
	    JButton capture = new JButton("Capture");
	  
	    east.add(capture);
	    tabGraphics.add(east,BorderLayout.EAST);

	    
	    /*tab2*/
	    //tab details
	     tabDetails = new JPanel();
	    String[] title = {"Num Acqui ", "SUVmax","True Sphere Volume (ml)","Measured spahere Volume (ml)","Difference (ml)","Difference (%)","a"};
	    String[][] titled = {{"1","2","3","4","5","6"}};

	     JTable table = new JTable(titled,title);
	    
	    tabDetails.add(new JScrollPane(table));
	   
		/*fenetre*/
		this.setTitle("Résultats Calibration");
		this.setLayout(new BorderLayout());		
			
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Graphics", tabGraphics);
		tabbedPane.addTab("Details", tabDetails);
		tabbedPane.addChangeListener(crc);
	   
		this.add(tabbedPane);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    this.pack();
	    
	    
	    
	    
	    capture.addActionListener(e -> {
		    Point loc = frame.getLocation();
		    Rectangle bounds = frame.getBounds();
		    BufferedImage buff = null;
		    try {
			    Rectangle rec = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
			    buff = new Robot().createScreenCapture(rec);
		    } catch (AWTException e1) {
			    e1.printStackTrace();
		    }

		    ImagePlus[] im = new ImagePlus[2];
		    im[0] = new  ImagePlus("Capture Calibration",buff);

		    tabbedPane.setSelectedIndex(1);

		    Thread t = new Thread(() -> {
			    try {

				    try {
					    Thread.sleep(100);
				    } catch (InterruptedException e12) {
					    e12.printStackTrace();
				    }
				    Rectangle rec = new Rectangle(loc.x, loc.y, bounds.width, bounds.height);
				    BufferedImage buff1 = new Robot().createScreenCapture(rec);

				    im[1] = new ImagePlus("Capture Calibration", buff1);

				    tabbedPane.setSelectedIndex(0);
				    ImagePlus imageplus = new ImagePlus();
				    imageplus.setStack(Library_Capture_CSV.captureToStack(im));
				    imageplus.show();
			    } catch (AWTException e1) {
				    e1.printStackTrace();
			    }
		    });
		    t.start();

	    });
	}
	 
	 public void setCoef(Double a, Double b) {
		 if(a.equals(Double.NaN) && b.equals(Double.NaN)) {
			 aLabel.setText("a = N/A");
			 bLabel.setText("b = N/A");
		 }else {
			 DecimalFormat df = new DecimalFormat("#.###");
			   
			 aLabel.setFont(new Font("", Font.PLAIN, 20));
			 bLabel.setFont(new Font("", Font.PLAIN, 20));
			 
			 aLabel.setText("a = "+df.format(a));
			 bLabel.setText("b = "+df.format(b));
		 }
		 
		
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
	 
	public void setTableDetails( ArrayList<Double[][]> listeTableauFinal, ArrayList<Double> listMoyenneDifferencePourcentage) {
		this.tabDetails.removeAll();
		this.tabDetails.revalidate();
		
		this.tabDetails.setLayout(new GridLayout(2, 3,15,5));
		
		String[] title = {"Acqui ", "SUVmax","True Volume (ml)","Measured Volume (ml)","Diff (ml)","Diff (%)"};
	
		for(int i=0; i< listeTableauFinal.size(); i++) {

			JTable jtable = new JTable(listeTableauFinal.get(i),title);
			jtable.setBorder(BorderFactory.createLineBorder(Color.black,1));
			
			JPanel tab = new JPanel(new BorderLayout());
			tab.add(jtable.getTableHeader(), BorderLayout.NORTH);
			tab.add(jtable, BorderLayout.CENTER);
			
			//affichage de la moyenne: dernier tableau de la liste
			tab.add(new JLabel("Mean Absolute Difference (%) = "+ listMoyenneDifferencePourcentage.get(i) +  "") , BorderLayout.SOUTH);
			
			//panel final avec titre, entete du tableau, contenu du tableau, moyenne de difference de pourcentage
			JPanel tabEtTitre = new JPanel();
			tabEtTitre.setLayout(new BoxLayout(tabEtTitre, BoxLayout.Y_AXIS));
			tabEtTitre.add(new JLabel("Sphere "+(i+1)));
			tabEtTitre.add(tab);
			
			this.tabDetails.add(tabEtTitre);
		}
		this.tabDetails.revalidate();
	}	 

}
