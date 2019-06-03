package org.petctviewer.scintigraphy.hepatic.tab;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TabTAC {

	private final String title;
	protected final FenResults parent;

	private final JPanel panel;

	private JPanel result;

	private final TabResult tab;

	private final String studyName;
	private boolean singleGraph;

	public TabTAC(FenResults parent, TabResult tab) {

		this.title = "TAC";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.studyName = ((TabCurves) this.tab).getFenApplication().getControleur().getModel().getStudyName();

		this.reloadDisplay();

	}

	public JPanel getResultContent() {
		if (!singleGraph) {
			ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabCurves) this.tab)
					.getFenApplication().getControleur().getModel();

			JPanel grid = new JPanel(new GridLayout(2, 2));

			ImageStack stackCapture = Library_Capture_CSV
					.captureToStack(new ImagePlus[] { Library_Capture_CSV.captureImage(modele.getCapture(), 512, 0) });
			ImagePlus montage = this.montage(stackCapture);

			// BufferedImage capture = fenApplication.getImagePlus().getBufferedImage();

			grid.add(new DynamicImage(montage.getBufferedImage()));

			List<XYSeries> series = modele.getSeries();
			ChartPanel chartDuodenom = Library_JFreeChart.associateSeries(new String[] { "Duodenom" }, series);
			JValueSetter setterDuodenom = new JValueSetter(chartDuodenom.getChart());
			setterDuodenom.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");

			ChartPanel chartCBD = Library_JFreeChart.associateSeries(new String[] { "CBD" }, series);

			ChartPanel chartHilium = Library_JFreeChart.associateSeries(new String[] { "Hilium" }, series);
			JValueSetter setterHilium = new JValueSetter(chartHilium.getChart());
			setterHilium.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");

			// chartDuodenom.setPreferredSize(new Dimension(parent.getWidth() / 2,
			// parent.getHeight() / 2));
			grid.add(chartDuodenom);

			// chartCBD.setPreferredSize(new Dimension(parent.getWidth() / 2,
			// parent.getHeight() / 2));
			grid.add(chartCBD);

			// chartHilium.setPreferredSize(new Dimension(parent.getWidth() / 2,
			// parent.getHeight() / 2));
			grid.add(chartHilium);

			// grid.setPreferredSize(new Dimension(1000, 650));

			return grid;
		} else {
			ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabCurves) this.tab)
					.getFenApplication().getControleur().getModel();

			XYSeriesCollection data = new XYSeriesCollection();
			data.addSeries(modele.getSerie("Duodenom"));
			data.addSeries(modele.getSerie("CBD"));
			data.addSeries(modele.getSerie("Hilium"));

			JFreeChart chart = ChartFactory.createXYLineChart("", "min", "counts/sec", data);

			ChartPanel chartpanel = new ChartPanel(chart);

			chartpanel.setPreferredSize(new Dimension(1000, 650));

			return chartpanel;
		}
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

	protected ImagePlus montage(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Results TAC -" + this.studyName + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 1, 1, 0.50, 1, 1, 1, 10, false);
		return imp;
	}

	public void switchGraph(JButton buttonSwitchGraph) {
		this.singleGraph = !singleGraph;
		if (this.singleGraph)
			buttonSwitchGraph.setText("Multiple graphs");
		else
			buttonSwitchGraph.setText("Sinlgle graph");
		this.reloadDisplay();
	}

}
