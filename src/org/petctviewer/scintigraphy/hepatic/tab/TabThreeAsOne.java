package org.petctviewer.scintigraphy.hepatic.tab;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

import javax.swing.*;
import java.awt.*;

public class TabThreeAsOne {

	private final String title;
	protected final FenResults parent;

	private final JPanel panel;

	private JPanel result;

	private final TabResult tab;

	public TabThreeAsOne(FenResults parent, TabResult tab) {

		this.title = "ThreeAsOne";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.reloadDisplay();

	}

	public JPanel getResultContent() {
		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabCurves) this.tab)
				.getFenApplication().getController().getModel();

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.getSerie("Duodenom"));
		data.addSeries(modele.getSerie("CBD"));
		data.addSeries(modele.getSerie("Hilium"));

		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

		// chartpanel.setPreferredSize(new Dimension(1000, 650));

		return new ChartPanel(chart);
	}

	public String getTitle() {
		return this.title;
	}

	public JPanel getPanel() {
		return this.panel;
	}

	public void reloadDisplay() {
		this.reloadResultContent();
	}

	public void reloadResultContent() {
		if (this.result != null)
			this.panel.remove(this.result);
		this.result = this.getResultContent() == null ? new JPanel() : this.getResultContent();
		this.panel.add(this.result, BorderLayout.CENTER);
		this.parent.repaint();
		this.parent.revalidate();
		this.parent.pack();
	}

}
