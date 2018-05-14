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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.scin.ModeleScinDyn;

public class FenSetValues extends JDialog {

	private static final long serialVersionUID = -2425748481776555583L;

	private ChartPanel chart;
	private SelectorListener selectorListener;

	public FenSetValues(ChartPanel chart) {
		this.chart = chart;

		XYSeriesCollection dataset = ((XYSeriesCollection) chart.getChart().getXYPlot().getDataset());
		//on renomme
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
		btn_ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FenSetValues.this.dispose();
			}
		});
		this.add(btn_ok, BorderLayout.SOUTH);

		// on recupere le plot
		XYPlot plot = chart.getChart().getXYPlot();

		// ajout des selecteurs dans le listener
		this.selectorListener = new SelectorListener(chart);
		selectorListener.add(
				new ValueSelector("TMax L", ModeleScinDyn.getMaxY(plot.getDataset(), 1), 1, RectangleAnchor.TOP_LEFT),
				1);
		selectorListener.add(new ValueSelector("TMax R", ModeleScinDyn.getMaxY(plot.getDataset(), 0), 0,
				RectangleAnchor.BOTTOM_LEFT), 0);

		selectorListener.add(new ValueSelector("Ret OG R", 20, 0, RectangleAnchor.BOTTOM_LEFT), 2);
		selectorListener.add(new ValueSelector("Ret OG L", 20, 1, RectangleAnchor.TOP_LEFT), 3);

		ValueSelector start = new ValueSelector(" ", 1, -1, RectangleAnchor.TOP_LEFT); // debut de l'intervalle
		selectorListener.add(start, 4);
		ValueSelector end = new ValueSelector(" ", 3, -1, RectangleAnchor.BOTTOM_RIGHT); // fin de l'intervalle
		selectorListener.add(end, 5);

		// on rempli l'intervalle entre start et end
		this.fillInterval(start.getXValue(), end.getXValue());
		// listener permettant de redraw l'intervalle quand on bouge les bornes
		chart.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				FenSetValues.this.fillInterval(start.getXValue(), end.getXValue());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				//TODO drag mouse
//				JFreeChart jfc = chart.getChart(); 
//				ChartMouseEvent event = new ChartMouseEvent(jfc, e, chart.getEntityForPoint(e.getX(), e.getY()));
//				selectorListener.chartMouseClicked(event);
//				selectorListener.chartMouseMoved(event);
			}
		});

		// on ajoute le listener sur le chart
		chart.addChartMouseListener(selectorListener);

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

	public Double[] getXValues() {
		return this.selectorListener.getXValues();
	}

	public ChartPanel getChartPanelWithOverlay() {
		// supprimer les marqueurs de retention d'origine
		this.selectorListener.remove(2);
		this.selectorListener.remove(3);

		// on supprime le listener du chartPanel
		this.chart.removeChartMouseListener(selectorListener);

		return this.chart;
	}

}
