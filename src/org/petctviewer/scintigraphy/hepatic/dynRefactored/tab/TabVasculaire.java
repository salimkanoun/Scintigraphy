package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

public class TabVasculaire {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private JPanel result;

	private TabResult tab;

	public TabVasculaire(FenResults parent, TabResult tab) {

		this.title = "Vascular";
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
		List<XYSeries> series = modele.getSeries();
		ChartPanel chartVasculaire = Library_JFreeChart.associateSeries(new String[] { "Blood pool" }, series);

		chartVasculaire.setPreferredSize(new Dimension(1000, 650));

		return chartVasculaire;
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
