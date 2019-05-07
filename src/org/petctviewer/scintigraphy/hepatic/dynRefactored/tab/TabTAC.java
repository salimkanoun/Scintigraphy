package org.petctviewer.scintigraphy.hepatic.dynRefactored.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.petctviewer.scintigraphy.hepatic.dynRefactored.SecondExam.ModelSecondMethodHepaticDynamic;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.MontageMaker;

public class TabTAC {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private JPanel result;

	private TabResult tab;

	private ImagePlus montage;

	private String studyName;

	public TabTAC(FenResults parent, TabResult tab) {

		this.title = "TAC";
		this.parent = parent;
		this.result = new JPanel();

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.result, BorderLayout.CENTER);

		this.tab = tab;

		this.studyName = ((TabOtherMethod) this.tab).getFenApplication().getControleur().getModel().getStudyName();

		this.reloadDisplay();

	}

	public JPanel getResultContent() {
		ModelSecondMethodHepaticDynamic modele = (ModelSecondMethodHepaticDynamic) ((TabOtherMethod) this.tab)
				.getFenApplication().getControleur().getModel();

		JPanel grid = new JPanel(new GridLayout(2, 2));

		ImageStack stackCapture = Library_Capture_CSV.captureToStack(new ImagePlus[] { Library_Capture_CSV.captureImage(modele.getCapture(), 512, 0) });
		this.montage = this.montage(stackCapture);

		// BufferedImage capture = fenApplication.getImagePlus().getBufferedImage();

		grid.add(new DynamicImage(this.montage.getBufferedImage()));

		JPanel pnl_center = new JPanel(new GridLayout(2, 2));

		List<XYSeries> series = modele.getSeries();
		ChartPanel chartDuodenom = Library_JFreeChart.associateSeries(new String[] { "Duodenom" }, series);
		JValueSetter setterDuodenom = new JValueSetter(chartDuodenom.getChart());
		setterDuodenom.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterDuodenom);

		ChartPanel chartCBD = Library_JFreeChart.associateSeries(new String[] { "CBD" }, series);
		pnl_center.add(chartCBD);

		ChartPanel chartHilium = Library_JFreeChart.associateSeries(new String[] { "Hilium" }, series);
		JValueSetter setterHilium = new JValueSetter(chartHilium.getChart());
		setterHilium.addSelector(new Selector("start", 10, -1, RectangleAnchor.BOTTOM_RIGHT), "start");
		pnl_center.add(setterHilium);

		chartDuodenom.setPreferredSize(new Dimension(parent.getWidth() / 2, parent.getHeight() / 2));
		grid.add(chartDuodenom);

		chartCBD.setPreferredSize(new Dimension(parent.getWidth() / 2, parent.getHeight() / 2));
		grid.add(chartCBD);

		chartHilium.setPreferredSize(new Dimension(parent.getWidth() / 2, parent.getHeight() / 2));
		grid.add(chartHilium);

		return grid;
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

}
