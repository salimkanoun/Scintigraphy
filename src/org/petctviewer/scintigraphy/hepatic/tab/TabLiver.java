package org.petctviewer.scintigraphy.hepatic.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.ModelHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class TabLiver {

	private final String title;
	protected final FenResults parent;

	private final JPanel panel;

	private JPanel result;

	private final TabResult tab;

	public TabLiver(FenResults parent, TabResult tab) {

		this.title = "Liver curves";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.reloadDisplay();
	}

	public JPanel getResultContent() {
		ModelHepaticDynamic modele = (ModelHepaticDynamic) ((TabCurves) this.tab)
				.getFenApplication().getController().getModel();
		List<XYSeries> series = modele.getSeries();
		ChartPanel chart = Library_JFreeChart.associateSeries(new String[] {"Blood pool", "Right Liver", "Left Liver" }, series);
		chart.setPreferredSize(new Dimension(200, 140));
		return chart;
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

	}

}