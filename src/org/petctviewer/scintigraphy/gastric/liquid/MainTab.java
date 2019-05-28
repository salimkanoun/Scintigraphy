package org.petctviewer.scintigraphy.gastric.liquid;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.gastric.ResultRequest;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;

public class MainTab extends TabResult {

	public MainTab(FenResults parent) {
		super(parent, "Liquid Phase");
		this.reloadDisplay();
	}

	private LiquidModel getModel() {
		return (LiquidModel) parent.getModel();
	}

	@Override
	public Component getSidePanelContent() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel panCenter = new JPanel(new GridLayout(0, 2));
		// T1/2
		panCenter.add(new JLabel("T 1/2"));
		panCenter.add(new JLabel(getModel().getResult(new ResultRequest(LiquidModel.RES_T_HALF)).formatValue()));
		panel.add(panCenter, BorderLayout.CENTER);

		return panel;
	}

	@Override
	public Container getResultContent() {
		XYSeries series = getModel().createSeries();
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		return Library_JFreeChart.createGraph("Stomach retention (liquid)", new Color[]{Color.BLUE}, "", dataset,
				series.getMaxY() * 1.1);
	}
}
