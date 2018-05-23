package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScin;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

public class FenSetValues extends JDialog implements ActionListener {

	private static final long serialVersionUID = -2425748481776555583L;

	private ChartPanel chart;
	private SelectorListener selectorListener;

	public FenSetValues(ChartPanel chart) {
		this.chart = chart;

		XYSeriesCollection dataset = ((XYSeriesCollection) chart.getChart().getXYPlot().getDataset());
		// on renomme
		try {
			dataset.getSeries("Final KL").setKey("Left Kidney");
			dataset.getSeries("Final KR").setKey("Right Kidney");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		this.setLayout(new BorderLayout());

		this.setTitle("Adjusting values");
		this.add(chart, BorderLayout.CENTER);
		JButton btn_ok = new JButton("Ok");
		btn_ok.addActionListener(this);

		JPanel wrap = new JPanel();
		wrap.add(btn_ok);
		this.add(wrap, BorderLayout.SOUTH);

		// on recupere le plot
		XYPlot plot = chart.getChart().getXYPlot();

		// ajout des selecteurs dans le listener
		this.selectorListener = new SelectorListener(chart);
		this.selectorListener.add(new ValueSelector("TMax L", ModeleScinDyn.getAbsMaxY(plot.getDataset(), 0), 0,
				RectangleAnchor.BOTTOM_LEFT), 1);
		this.selectorListener.add(new ValueSelector("TMax R", ModeleScinDyn.getAbsMaxY(plot.getDataset(), 1), 1,
				RectangleAnchor.TOP_LEFT), 0);

		// this.selectorListener.add(new ValueSelector("Ret OG R", 20, 0,
		// RectangleAnchor.BOTTOM_LEFT), 2);
		// this.selectorListener.add(new ValueSelector("Ret OG L", 20, 1,
		// RectangleAnchor.TOP_LEFT), 3);

		ValueSelector start = new ValueSelector(" ", 1, -1, RectangleAnchor.TOP_LEFT); // debut de l'intervalle
		this.selectorListener.add(start, 4);
		ValueSelector end = new ValueSelector(" ", 2, -1, RectangleAnchor.BOTTOM_RIGHT); // fin de l'intervalle
		this.selectorListener.add(end, 5);
		ValueSelector middle = new ValueSelector("<->", 2, -1, RectangleAnchor.CENTER);
		this.selectorListener.add(middle, 6);

		this.selectorListener.add(new ValueSelector("Lasilix", 20, -1, RectangleAnchor.BOTTOM_LEFT), 7);

		// on rempli l'intervalle entre start et end
		this.fillInterval(start.getXValue(), end.getXValue());
		// listener permettant de redraw l'intervalle quand on bouge les bornes
		chart.addMouseMotionListener(new MouseMotionListener() {
			private double d;

			@Override
			public void mouseMoved(MouseEvent e) {
				FenSetValues.this.fillInterval(start.getXValue(), end.getXValue());
				if (middle.isXLocked()) { // si le selecteur du milieu n'est pas selectionne, on le recentre
					middle.setXValue((start.getXValue() + end.getXValue()) / 2);
					this.d = Math.abs(start.getXValue() - middle.getXValue());
				} else { // sinon on bouge les deux autres selecteurs
					start.setXValue(middle.getXValue() - this.d);
					end.setXValue(middle.getXValue() + this.d);
				}

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// inutile pour ce programme
			}
		});

		// on ajoute le listener sur le chart
		chart.addChartMouseListener(this.selectorListener);

		this.pack();

	}

	private static JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart("Adjust the values", "X", "Y", dataset);
		return chart;
	}

	private static XYDataset createDataset() {
		XYSeries series = new XYSeries("S1");
		for (int x = 0; x < 30; x++) {
			series.add(x, 5 + Math.random() * 3);
		}
		XYSeriesCollection dataset = new XYSeriesCollection(series);

		XYSeries series2 = new XYSeries("S2");
		for (int x = 0; x < 30; x++) {
			series2.add(x, 3 + Math.random() * 3);
		}

		dataset.addSeries(series2);

		return dataset;
	}

	public static void main(String[] args) {
		ChartPanel c = new ChartPanel(createChart(createDataset()));
		c.getChart().getPlot().setBackgroundPaint(null);
		FenSetValues f = new FenSetValues(c);
		f.setModal(true);
		f.setVisible(true);
	}

	private void fillInterval(double start, double end) {
		double debut = Math.min(start, end);
		double fin = Math.max(start, end);

		XYPlot plot = this.chart.getChart().getXYPlot();
		plot.clearDomainMarkers();
		final Color c = new Color(225, 244, 50, 120);
		final Marker bst = new IntervalMarker(debut, fin, c, new BasicStroke(2.0f), null, null, 1.0f);

		bst.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
		bst.setLabelOffset(new RectangleInsets(15, 0, 0, 5));
		bst.setLabelFont(new Font("SansSerif", 0, 12));
		bst.setLabelTextAnchor(TextAnchor.BASELINE_RIGHT);
		plot.addDomainMarker(bst);
	}

	/**
	 * Renvoie les valeurs en x des selecteurs
	 * 
	 * @return [0] => TMaxD <br>
	 *         [1] => TMaxG <br>
	 *         [2] => Retetion origin D <br>
	 *         [3] => Retetion origin G <br>
	 *         [4] => Borne intervalle 1 <br>
	 *         [5] => Borne intervalle 2 <br>
	 *         [6] => Lasilix
	 */
	public Double[] getXValues() {
		return this.selectorListener.getXValues();
	}

	public ChartPanel getChartPanelWithOverlay() {
		// supprimer les marqueurs de retention d'origine
		this.selectorListener.remove(2);
		this.selectorListener.remove(3);
		this.selectorListener.remove(6);

		// on supprime le listener du chartPanel
		this.chart.removeChartMouseListener(this.selectorListener);

		return this.chart;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		boolean checkOffset = this.checkOffset();
		if (!checkOffset) {
			String message = "Inconsistent differencial function during interval integration. \n Would you like to redefine the interval ?";
			int dialogResult = JOptionPane.showConfirmDialog(this, message, "WARNING", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(dialogResult != JOptionPane.YES_OPTION){
			  this.dispose();
			}
		}
	}

	//returns true if passed
	private boolean checkOffset() {
		XYDataset data = chart.getChart().getXYPlot().getDataset();

		Double[] values = this.getXValues();

		Double debut = Math.min(values[4], values[5]);
		Double fin = Math.max(values[4], values[5]);

		XYDataset dataCropped = Modele_Renal.cropDataset(data, debut, fin);

		for (int i = 1; i < dataCropped.getItemCount(0); i++) {
			Double N1 = (dataCropped.getYValue(0, i) / dataCropped.getYValue(1, i));
			Double N = (dataCropped.getYValue(0, i-1) / dataCropped.getYValue(1, i-1));
			Double ecart = Math.abs(1 - N1/N);
			if(ecart > 0.05) {
				return false;
			}
		}
		
		return true;
	}

}
