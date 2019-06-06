package org.petctviewer.scintigraphy.hepatic.tab;

import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabCurves) this.tab)
				.getFenApplication().getController().getModel();
		List<XYSeries> series = modele.getSeries();

//		chartVasculaire.setPreferredSize(new Dimension(1000, 650));

		return Library_JFreeChart.associateSeries(new String[] {"Blood pool", "Right Liver", "Left Liver" }, series);
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
