package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

/**
 * Tab showing the deconvolve of Right liver, using the Blood Pool values.
 *
 */
public class TabDeconvolv {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private JPanel result;

	private TabResult tab;
	private int deconvolve;

	public TabDeconvolv(FenResults parent, TabResult tab) {

		this.title = "Deconvolv";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;
		
		this.deconvolve = 10;

		this.reloadDisplay();

	}

	public JPanel getResultContent() {

		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabOtherMethod) this.tab)
				.getFenApplication().getControleur().getModel();

		// TODO remove start
		List<Double> bp = modele.getData("Blood Pool AVG");
		List<Double> rliver = modele.getData("Right Liver AVG");
//		double[] kernel = ;
		Double[] kernel = {1.0d,2.0d,1.0d};
		Double[] convolvedBP = Library_Quantif.processNConvolv(bp, kernel, 6);
		Double[] convolvedRL = Library_Quantif.processNConvolv(rliver, kernel, 6);

		@SuppressWarnings("deprecation")
		List<Double> deconv = Library_Quantif.deconvolv(convolvedBP,
				convolvedRL,this.deconvolve);

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(modele.createSerie(deconv, "deconv"));
		data.addSeries(modele.getSerie("Blood Pool AVG"));
		data.addSeries(modele.getSerie("Right Liver AVG"));
		JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

		ChartPanel chartpanel = new ChartPanel(chart);

		return chartpanel;

		// return null;
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
	
	public void setDeconvolvFactor(int deconvolv) {
		this.deconvolve = deconvolv;
	}
}
