  package org.petctviewer.scintigraphy.dynamic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.Scintigraphy;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public class FenResultat_GeneralDyn extends JFrame {

	private static final long serialVersionUID = -6949646596222162929L;

	public FenResultat_GeneralDyn(Scintigraphy scin, BufferedImage capture, ModeleScinDyn modele, String[][] asso,
			String antOrPost) {
		SidePanel side = new SidePanel(null, "Dynamic Quant\n" + antOrPost, scin.getImp());
		side.addCaptureBtn(scin, "_" + antOrPost);
		
		int cols = (modele.getNbRoi() + 1) / 2;
		JPanel grid = new JPanel(new GridLayout(2, cols));

		grid.add(new DynamicImage(capture));

		ChartPanel[] cPanels = ModeleScinDyn.associateSeries(asso, modele.getSeries());
		for (ChartPanel c : cPanels) {
			c.setPreferredSize(new Dimension(capture.getWidth() + 1 / 3 * capture.getWidth(), capture.getHeight()));
			grid.add(c);
		}

		this.add(grid, BorderLayout.CENTER);
		this.add(side, BorderLayout.EAST);
		
		this.pack();
		this.setMinimumSize(side.getSize());
		this.setVisible(true);
	}
}
