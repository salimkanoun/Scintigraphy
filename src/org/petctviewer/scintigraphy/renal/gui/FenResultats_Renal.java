package org.petctviewer.scintigraphy.renal.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.RenalSettings;
import org.petctviewer.scintigraphy.renal.Modele_Renal;
import org.petctviewer.scintigraphy.scin.FenResultatSidePanel;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;
import org.petctviewer.scintigraphy.scin.VueScinDyn;

public class FenResultats_Renal {

	private Container principal, secondaire, kidneys, timedImage, tabCort;

	public FenResultats_Renal(VueScinDyn vue, BufferedImage capture, ChartPanel chartPanel) {
		this.principal = new TabPrincipal(vue, capture, chartPanel).getContentPane();
		
		int w = this.principal.getWidth();
		int h = this.principal.getHeight();

		this.secondaire = new TabOptionalCharts(vue, w, h).getContentPane();
		this.kidneys = new TabROE(vue, w, h).getContentPane();
		this.timedImage = new TabTimedImage(vue, principal.getHeight() / 4, 4, 5).getContentPane();
		this.tabCort = new TabCort(vue, w, h).getContentPane();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void createAndShowGUI() {
		// Create and set up the window.
		final JFrame frame = new JFrame("Results Renal Exam");

		// set grid layout for the frame
		frame.getContentPane().setLayout(new GridLayout(1, 1));

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);

		tabbedPane.addTab("Main", this.principal);
		tabbedPane.addTab("Timed image", this.timedImage);
		tabbedPane.addTab("ROE", this.kidneys);

		//si les corticales sont activees
		if (RenalSettings.getSettings()[1]) {
			tabbedPane.addTab("Corticals/Pelvis", this.tabCort);
		}
		
		tabbedPane.addTab("Other", this.secondaire);

		frame.getContentPane().add(tabbedPane);

		// Display the window
		frame.setPreferredSize(new Dimension(1000, 800));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
