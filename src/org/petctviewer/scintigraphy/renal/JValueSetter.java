package org.petctviewer.scintigraphy.renal;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

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
