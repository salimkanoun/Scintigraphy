package org.petctviewer.scintigraphy.generic.dynamic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.shunpo.FenResults;
import org.petctviewer.scintigraphy.shunpo.TabResult;

public class FenResultat_GeneralDyn extends FenResults {

	private static final long serialVersionUID = -6949646596222162929L;

	private String antOrPost;
	private BufferedImage capture;
	private String[][] asso;

	public FenResultat_GeneralDyn(Scintigraphy scin, BufferedImage capture, ModeleScinDyn modele, String[][] asso,
			String antOrPost) {
		super(modele);
		this.capture = capture;
		this.antOrPost = antOrPost;
		this.asso = asso;

		this.addTab(new Tab());

		this.setLocationRelativeTo(modele.getImagePlus().getWindow());
	}

	private class Tab extends TabResult {

		public Tab() {
			super(FenResultat_GeneralDyn.this, "Result");
			this.createCaptureButton("_" + antOrPost);
		}

		@Override
		public Component getSidePanelContent() {
			return null;
		}

		@Override
		public JPanel getResultContent() {
			Modele_GeneralDyn modele = (Modele_GeneralDyn) this.parent.getModel();
			int cols = (modele.getNbRoi() + 1) / 2;
			JPanel grid = new JPanel(new GridLayout(2, cols));

			grid.add(new DynamicImage(capture));

			ChartPanel[] cPanels = Library_JFreeChart.associateSeries(asso, modele.getSeries());
			for (ChartPanel c : cPanels) {
				c.setPreferredSize(new Dimension(capture.getWidth() + 1 / 3 * capture.getWidth(), capture.getHeight()));
				grid.add(c);
			}
			return grid;
		}
	}
}
