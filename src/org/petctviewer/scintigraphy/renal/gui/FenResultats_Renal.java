package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.RenalScintigraphy;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.shunpo.FenResults;

import ij.Prefs;

public class FenResultats_Renal extends FenResults {
	private static final long serialVersionUID = 1L;
	
	private Container principal, zoomed, kidneys, timedImage, tabCort, tabUreter, tabOther, tabPost, tabPatlak;
	private final int width = 1000, height = 800;

	public FenResultats_Renal(RenalScintigraphy vue, BufferedImage capture, ModeleScin model) {
		super(model, "Renal Exam", model.getStudyName());
		this.principal = new TabPrincipal(vue, capture, this);
		this.zoomed = new TabZoomed(vue, this);
		this.kidneys = new TabROE(vue, this);
		this.timedImage = new TabTimedImage(vue, 4, 5, this);
		this.tabCort = new TabCort(vue, this);
		this.tabUreter = new TabUreter(vue, this);
		this.tabOther = new TabOther(vue, this);
		this.tabPost = new TabPostMict(vue, this);
		if (vue.getPatlakChart() != null) {
			this.tabPatlak = new TabPatlak(vue, this);
		}

		showGUI(vue);
	}

	private void showGUI(RenalScintigraphy vue) {
		// Create and set up the window.
		this.setTitle("Results Renal Exam");
		this.getContentPane().setLayout(new GridLayout(1, 1));

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
		
//		this.getContentPane().add(tabbedPane);
		this.setResult(tabbedPane);
		
		// Display the window
		this.setPreferredSize(new Dimension(width, height));
		this.setLocationRelativeTo(vue.getFenApplication());
		this.pack();
//		this.setVisible(true);
//		this.setResizable(true);
//		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
