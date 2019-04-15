package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;

import ij.Prefs;

public class FenResultats_Renal {

	private Container principal, zoomed, kidneys, timedImage, tabCort, tabUreter, tabOther, tabPost, tabPatlak;
	private final int width = 1000, height = 800;

	public FenResultats_Renal(RenalScintigraphy vue, BufferedImage capture, ModeleScin model) {
		this.principal = new TabPrincipal(vue, capture, model);
		this.zoomed = new TabZoomed(vue, model);
		this.kidneys = new TabROE(vue, model);
		this.timedImage = new TabTimedImage(vue, 4, 5, model);
		this.tabCort = new TabCort(vue, model);
		this.tabUreter = new TabUreter(vue, model);
		this.tabOther = new TabOther(vue, model);
		this.tabPost = new TabPostMict(vue, model);
		if (vue.getPatlakChart() != null) {
			this.tabPatlak = new TabPatlak(vue, model);
		}

		showGUI(vue);
	}

	private void showGUI(RenalScintigraphy vue) {

		// Create and set up the window.
		JFrame frame = new JFrame("Results Renal Exam");
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.principal);
		tabbedPane.addTab("Timed image", this.timedImage);
		tabbedPane.addTab("ROE", this.kidneys);

		// si les pelvis sont activees
		if (Prefs.get("renal.pelvis.preferred", true)) {
			tabbedPane.addTab("Corticals/Pelvis", this.tabCort);
		}

		// si les ureteres sont activees
		if (Prefs.get("renal.ureter.preferred", true)) {
			tabbedPane.addTab("Ureters", this.tabUreter);
		}

		tabbedPane.addTab("Vascular phase", this.zoomed);
		tabbedPane.addTab("Other", this.tabOther);

		tabbedPane.addTab("Post-mictional", this.tabPost);
		
		if (this.tabPatlak != null) {
			tabbedPane.addTab("Patlak", this.tabPatlak);
		}
		
		frame.getContentPane().add(tabbedPane);

		// Display the window
		frame.setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(vue.getFenApplication());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	// renomme la serie
	static void renameSeries(ChartPanel chartPanel, String oldKey, String newKey) {
		XYSeriesCollection dataset = ((XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset());
		try {
			dataset.getSeries(oldKey).setKey(newKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
