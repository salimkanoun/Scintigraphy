
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A demo showing crosshairs that follow the data points on an XYPlot.
 */
public class test extends JFrame {

	private ChartPanel chartPanel;

	public test(String title) {
		super(title);
		setContentPane(createContent());
	}

	private JPanel createContent() {
		JFreeChart chart = createChart(createDataset());
		this.chartPanel = new ChartPanel(chart);
		
		CustomOverlay TMaxD = new CustomOverlay("TMaxD", 0.0, chartPanel, 0);
		CustomOverlay TMaxG = new CustomOverlay("TMaxG", 2.0, chartPanel, 1);
		
		chartPanel.addOverlay(TMaxD);
		chartPanel.addOverlay(TMaxG);
		
		chartPanel.addChartMouseListener(TMaxG);
		chartPanel.addChartMouseListener(TMaxD);
		
		
		return chartPanel;
	}
	
	private class CustomOverlay extends CrosshairOverlay implements ChartMouseListener{
		
		private Crosshair crossX, crossY;
		private boolean xLocked;
		private XYPlot plot;
		private ChartPanel chartPanel;
		private int series;
		
		public CustomOverlay(String nom, double startX, ChartPanel chartPanel, int series) {
			this.series = series;
			this.xLocked = true;
			this.chartPanel = chartPanel;
			this.plot = chartPanel.getChart().getXYPlot();
			
			this.crossX = new Crosshair(startX, Color.GRAY, new BasicStroke(0f));
			this.crossX.setLabelGenerator(new CrosshairLabelGenerator() {				
				@Override
				public String generateLabel(Crosshair crosshair) {
					return nom;
				}
			});
			
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			for (int c = 0; c < plot.getDataset().getSeriesCount(); c++) {
				renderer.setSeriesShapesVisible(c, false);
			}
			plot.setRenderer(series, renderer);
					
			//this.crossX.setPaint();
			this.crossX.setLabelVisible(true);			
			
			this.crossY = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
			this.crossY.setLabelVisible(true);
			
			this.addDomainCrosshair(crossX);
			this.addRangeCrosshair(crossY);
		}

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
			
			int xMouse = (int) event.getTrigger().getPoint().getX();
			
			Rectangle2D plotArea = chartPanel.getScreenDataArea();
			
			int xJava2D = (int) this.plot.getDomainAxis().valueToJava2D(this.crossX.getValue(), plotArea, plot.getDomainAxisEdge());
			
			int marge = 10;
			if(xJava2D > xMouse - marge && xJava2D < xMouse + marge) {
				this.xLocked = !this.xLocked;
				this.crossY.setVisible(!this.xLocked);
			}
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			this.crossX.setPaint(plot.getRenderer(0).getSeriesPaint(series));
			if (!this.xLocked) {
				Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
				JFreeChart chart = event.getChart();
				XYPlot plot = (XYPlot) chart.getPlot();
				ValueAxis xAxis = plot.getDomainAxis();

				double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
						org.jfree.chart.ui.RectangleEdge.BOTTOM);
				double y = DatasetUtils.findYValue(plot.getDataset(), this.series, x);

				this.crossX.setValue(x);
				this.crossY.setValue(y);
			}			
		}
		
		
	}

	private JFreeChart createChart(XYDataset dataset) {
		JFreeChart chart = ChartFactory.createXYLineChart("Crosshair Demo", "X", "Y", dataset);
		return chart;
	}

	private XYDataset createDataset() {
		XYSeries series = new XYSeries("S1");
		for (int x = 0; x < 10; x++) {
			series.add(x, x + Math.random() * 4.0);
		}
		XYSeriesCollection dataset = new XYSeriesCollection(series);
		
		XYSeries series2 = new XYSeries("S2");
		for (int x = 0; x < 10; x++) {
			series2.add(x, x + Math.random() * 4.0);
		}
		
		dataset.addSeries(series2);
		
		return dataset;
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				test app = new test("JFreeChart: CrosshairOverlayDemo1.java");
				app.pack();
				app.setVisible(true);
			}
		});
	}

}