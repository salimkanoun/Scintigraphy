package org.petctviewer.scintigraphy.dynamic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScin;

public class FenResultat_GeneralDyn extends FenResultatSidePanel {

	private static final long serialVersionUID = -6949646596222162929L;

	public FenResultat_GeneralDyn(VueScin vueScin, BufferedImage capture, ModeleScinDyn modele, String[][] asso,
			String antOrPost) {
		super("Dynamic Quant\n" + antOrPost, vueScin, capture, "_" + antOrPost);

		this.setModele(modele);

		int cols = (modele.getNbRoi() + 1) / 2;
		JPanel grid = new JPanel(new GridLayout(2, cols));

		JLabel lbl_capture = new JLabel();
		lbl_capture.setIcon(new ImageIcon(capture));
		grid.add(lbl_capture);

		ChartPanel[] cPanels = ModeleScin.associateSeries(asso, modele.getSeries());
		for (ChartPanel c : cPanels) {
			c.setPreferredSize(new Dimension(capture.getWidth() + 1 / 3 * capture.getWidth(), capture.getHeight()));
			grid.add(c);
		}

		this.add(grid, BorderLayout.WEST);
		this.finishBuildingWindow();
		this.setVisible(true);
	}

	@Override
	public Component[] getSidePanelContent() {
		return new Component[] { new JLabel() };
	}

}
