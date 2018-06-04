package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.gui.FenPatlak;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

public class JValueSetter extends JPanel {

	private static final long serialVersionUID = -2425748481776555583L;

	private SelectorListener selectorListener;

	public JValueSetter(SelectorListener selectorListener) {
		ChartPanel chart = selectorListener.getChartPanel();
		this.selectorListener = selectorListener;
		this.setLayout(new BorderLayout());
		this.add(chart);

		// on ajoute le listener sur le chart
		chart.addChartMouseListener(this.selectorListener);
	}

	public SelectorListener getSelectorListener() {
		return this.selectorListener;
	}

}
