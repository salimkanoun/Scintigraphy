package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabThreeAsOne {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private JPanel result;

	private TabResult tab;

	private String studyName;

	public TabThreeAsOne(FenResults parent, TabResult tab) {

		this.title = "ThreeAsOne";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.studyName = ((TabOtherMethod) this.tab).getFenApplication().getControleur().getModel().getStudyName();

		this.reloadDisplay();

	}

	public JPanel getResultContent() {
		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabOtherMethod) this.tab)
				.getFenApplication().getControleur().getModel();

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.getSerie("Duodenom"));
		data.addSeries(modele.getSerie("CBD"));
		data.addSeries(modele.getSerie("Hilium"));

		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

		ChartPanel chartpanel = new ChartPanel(chart);

		return chartpanel;
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
