package org.petctviewer.scintigraphy.renal.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.renal.Model_Renal;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

class TabROE extends TabResult implements ActionListener {

	public TabROE(FenResults parent) {
		super(parent, "ROE", true);

		this.reloadDisplay();
	}

	private Component getPanelROE() {
		JLabel lbl_L = new JLabel("L");
		lbl_L.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbl_R = new JLabel("R");
		lbl_R.setHorizontalAlignment(SwingConstants.CENTER);

		Double[] mins = new Double[10];
		for (int i = 0; i < mins.length; i++) {
			mins[i] = Library_Quantif.round(
					(((Model_Renal) this.parent.getModel()).getSerie("Blood Pool").getMaxX() / (mins.length * 1.0)) * i
							+ 1,
					1);
		}

		// panel roe
		JPanel pnl_roe = new JPanel(new GridLayout(mins.length + 1, 3, 0, 3));
		pnl_roe.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		pnl_roe.add(new JLabel(" ROE "));
		pnl_roe.add(lbl_L);
		pnl_roe.add(lbl_R);

		for (Double min : mins) {
			// aligne a droite
			JLabel lbl_min = new JLabel(min + "  min");
			pnl_roe.add(lbl_min);

			if (((Model_Renal) this.parent.getModel()).getKidneys()[0]) {
				JLabel lbl_g = new JLabel(((Model_Renal) this.parent.getModel()).getROE(min, "L") + " %");
				lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_roe.add(lbl_g);
			} else {
				pnl_roe.add(new JLabel("N/A"));
			}

			if (((Model_Renal) this.parent.getModel()).getKidneys()[1]) {
				JLabel lbl_d = new JLabel(((Model_Renal) this.parent.getModel()).getROE(min, "R") + " %");
				lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
				pnl_roe.add(lbl_d);
			} else {
				pnl_roe.add(new JLabel("N/A"));
			}

		}

		return pnl_roe;
	}

	public Component getSidePanelContent() {
		Box box = Box.createVerticalBox();
		box.add(getPanelROE());
		box.add(Box.createVerticalStrut(30));
		JButton btn_bpi = new JButton("Check fit");
		btn_bpi.addActionListener(this);
		JPanel pnl_bpi = new JPanel();
		pnl_bpi.add(btn_bpi);
		box.add(pnl_bpi);
		return box;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFrame frameBPI = new JFrame();
		frameBPI.add(Library_JFreeChart.associateSeries(new String[] { "BPI", "Blood pool fitted" },
				((Model_Renal) this.parent.getModel()).getSeries()));
		frameBPI.setLocationRelativeTo(parent);
		frameBPI.setSize(new Dimension(512, 512));
		frameBPI.setVisible(true);
	}

	@Override
	public JPanel getResultContent() {// on recupere le modele et les series
		Model_Renal modele = (Model_Renal) parent.getModel();
		List<XYSeries> series = modele.getSeries();

		// recuperation des chart panel avec association
		String[][] asso = { { "Blood pool fitted L", "Final KL", "Output KL" },
				{ "Blood pool fitted R", "Final KR", "Output KR" } };

		ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, series);

		BasicStroke stroke = new BasicStroke(5.0F);
		JPanel p = new JPanel(new GridLayout(2, 1));

		if (modele.getKidneys()[1]) {
			// graphique rein droit
			ChartPanel c = cPanels[1];
			FenResultats_Renal.renameSeries(c, "Blood pool fitted R", "Blood Pool");
			FenResultats_Renal.renameSeries(c, "Final KR", "Right Kidney");
			FenResultats_Renal.renameSeries(c, "Output KR", "Output");

			XYItemRenderer ren = c.getChart().getXYPlot().getRenderer();
			ren.setDefaultStroke(stroke);
			ren.setSeriesPaint(0, Color.GREEN);
			ren.setSeriesPaint(1, Color.BLUE);
			ren.setSeriesPaint(2, Color.MAGENTA);

			c.getChart().setTitle("Right Kidney");
			c.getChart().getLegend().setPosition(RectangleEdge.LEFT);
			p.add(c);
		}

		if (modele.getKidneys()[0]) {
			// graphique rein gauche
			ChartPanel c1 = cPanels[0];

			XYItemRenderer ren = c1.getChart().getXYPlot().getRenderer();

			ren.setDefaultStroke(stroke);
			ren.setSeriesPaint(0, Color.GREEN);
			ren.setSeriesPaint(1, Color.RED);
			ren.setSeriesPaint(2, Color.MAGENTA);

			FenResultats_Renal.renameSeries(c1, "Output KL", "Output");
			FenResultats_Renal.renameSeries(c1, "Blood pool fitted L", "Blood Pool");
			FenResultats_Renal.renameSeries(c1, "Final KL", "Left Kidney");

			c1.getChart().setTitle("Left Kidney");
			c1.getChart().getLegend().setPosition(RectangleEdge.LEFT);
			p.add(c1);
		}
		return p;
	}

}
