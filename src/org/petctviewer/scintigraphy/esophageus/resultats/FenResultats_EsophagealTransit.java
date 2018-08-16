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
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCondense;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabCurves;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabRentention;
import org.petctviewer.scintigraphy.esophageus.resultats.tabs.TabTransitTime;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;

public class FenResultats_EsophagealTransit extends JFrame {
	
	private Modele_Resultats_EsophagealTransit 		modele ;

	/*
	 * un partie main avec graph main et un jtablecheckbox main
	 * un partie transit time avec hraph , jvalue stter, checkbox (1 collonnne pour les acqui entier) et un couple de controleur par acqui
	 */
	public FenResultats_EsophagealTransit(ArrayList<HashMap<String, ArrayList<Double>>> arrayList, ArrayList<Object[]> dicomRoi) {
		
		modele = new Modele_Resultats_EsophagealTransit(arrayList,dicomRoi);
		this.setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		tabbedPane.addTab("Curves", new TabCurves(arrayList.size(), this.modele));	
		tabbedPane.addTab("Transit Time", new TabTransitTime(arrayList.size(), this.modele));	 
		tabbedPane.addTab("Retention", new TabRentention(arrayList.size(), this.modele));
		tabbedPane.addTab("Condensed Dynamic images", new TabCondense(arrayList.size(), this.modele));

		this.add(tabbedPane);
		
		this.pack();
	}

}
