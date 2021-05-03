package org.petctviewer.scintigraphy.salivaryGlands.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.*;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import ij.Prefs;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabSalivaryGlands;

public class FenCitrus extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton btn_ok;
	private final JValueSetter jvaluesetter;
	private final ModelSalivaryGlands model;

	public FenCitrus(ChartPanel cp, Component parentComponent, ModelSalivaryGlands model) {
		super();
		this.model = model;

		// creation du panel du bas
		this.btn_ok = new JButton("Ok");
		this.btn_ok.addActionListener(this);
		

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(wrapComponent(btn_ok), BorderLayout.CENTER);
		this.setLayout(new BorderLayout());

		this.setTitle("Please adjust the citrus value");

		// creation du jvaluesetter
		this.jvaluesetter = prepareValueSetter(cp);
		this.add(jvaluesetter, BorderLayout.CENTER);

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.pack();
		this.setLocationRelativeTo(parentComponent);
	}

	/// prepare la fenetre de selection des abscisses
	private JValueSetter prepareValueSetter(ChartPanel chart) {
		chart.getChart().getPlot().setBackgroundPaint(null);
		JValueSetter jvs = new JValueSetter(chart.getChart());

		Selector citrus = new Selector("Citrus", Prefs.get(PrefTabSalivaryGlands.PREF_CITRUS_INJECT_TIME, 10), -1,
				RectangleAnchor.BOTTOM_LEFT);
		Selector start = new Selector(" Start", 9, -1, RectangleAnchor.TOP_LEFT);
		Selector end = new Selector("End ", 24, -1, RectangleAnchor.BOTTOM_RIGHT);


		jvs.addSelector(citrus, "citrus");
		jvs.addSelector(start, "start");
		jvs.addSelector(end, "end");

		jvs.addArea("start", "end", "integral",null);

		// renomme les series du chart pour que l'interface soit plus comprehensible
		XYSeriesCollection dataset = ((XYSeriesCollection) chart.getChart().getXYPlot().getDataset());
		if (model.equals(model.getGlands())){
			dataset.getSeries("Final L. Parotid").setKey("Left Parotid");
			dataset.getSeries("Final R. Parotid").setKey("Right Parotid");
			dataset.getSeries("Final L. Submandible").setKey("Left Sub mandibular");
			dataset.getSeries("Final R. Submandible").setKey("Right Sub mandibular");
		}

		return jvs;
	}

	private JPanel wrapComponent(Component c) {
		JPanel p = new JPanel();
		p.add(c);
		return p;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JButton b = (JButton) arg0.getSource();
		if (b == this.btn_ok) {
			this.dispose();
        }
	}

	public JValueSetter getValueSetter() {
		return this.jvaluesetter;
	}



}