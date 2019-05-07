package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;

public class TabAnotherGraph {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private JPanel result;

	private TabResult tab;

	public TabAnotherGraph(FenResults parent, TabResult tab) {

		this.title = "AnotherGraph";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.reloadDisplay();

	}

	public JPanel getResultContent() {

		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabOtherMethod) this.tab)
				.getFenApplication().getControleur().getModel();

		// TODO remove start
		List<Double> bp = modele.getData("Blood pool");
		List<Double> rliver = modele.getData("Right Liver");

		Double[] deconv = new Double[bp.size()];
		for (int i = 0; i < bp.size(); i++) {
			deconv[i] = rliver.get(i) / bp.get(i);
		}

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.createSerie(Arrays.asList(deconv), "deconv"));
		data.addSeries(modele.getSerie("Blood pool"));
		data.addSeries(modele.getSerie("Right Liver"));
		JFreeChart chart = ChartFactory.createXYLineChart("", "x", "y", data);

		ChartPanel chartpanel = new ChartPanel(chart);

		return chartpanel;
		
//		return null;
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
