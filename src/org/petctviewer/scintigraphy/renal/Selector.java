package org.petctviewer.scintigraphy.renal;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.general.DatasetUtils;

import java.awt.*;

public class Selector extends CrosshairOverlay implements ChartMouseListener {

	private static final long serialVersionUID = 6794595703667698248L;
	private Crosshair crossX, crossY;

	// si le selecteur n'est pas selectionne
	private boolean xLocked;

	// serie sur laquelle se situe le selecteur
	private int series;

	private Comparable key;
	private JValueSetter jValueSetter;

	/**
	 * Permet de creer un selecteur deplacable sur un courbe
	 * 
	 * @param nom    studyName du selecteur (null accepte)
	 * @param startX position de depart du selecteur
	 * @param series series observee (-1 si aucune)
	 * @param anchor position du label
	 */
	public Selector(String nom, double startX, int series, RectangleAnchor anchor) {
		this.series = series;
		this.xLocked = true;

		// on intialise le selecteur vertical
		this.crossX = new Crosshair(startX, Color.GRAY, new BasicStroke(0f));

		// on place le label a l'endroit demande
		this.crossX.setLabelOutlineVisible(false);

		if (anchor != null) {
			this.crossX.setLabelAnchor(anchor);
		} else {
			this.crossX.setLabelAnchor(RectangleAnchor.BOTTOM);
		}

		// on rend le label invisible si le studyName est null ou si c'est un espace
		this.crossX.setLabelGenerator(new CrosshairLabelGenerator() {
			@Override
			public String generateLabel(Crosshair crosshair) {
				if (nom == null || nom.trim().equals("")) {
					Selector.this.crossX.setLabelVisible(false);
				}
				return nom;
			}
		});

		// le selecteur vertical n'est pour l'instant pas affiche
		this.crossY = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
		this.crossY.setLabelVisible(true);
		this.crossX.setLabelVisible(true);

		// on ajoute les selecteurs horizontaux et verticaux
		this.addDomainCrosshair(this.crossX);
		this.addRangeCrosshair(this.crossY);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// on bloque, ou debloque le selecteur vertical
		this.xLocked = !this.xLocked;

		// on rend visible le selecteur horizontal si le selecteur horizontal est
		// debloque
		this.crossY.setVisible(!this.xLocked);
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		XYPlot plot = (XYPlot) event.getChart().getPlot();

		// si le selecteur vertical n'est pas bloque
		if (!this.xLocked) {
			ValueAxis xAxis = plot.getDomainAxis();

			// on calcule la nouvelle valeur du selecteur vertical
			double x = xAxis.java2DToValue(event.getTrigger().getX(), this.jValueSetter.getScreenDataArea(),
					RectangleEdge.BOTTOM);
			double y = Double.NaN;

			// si on se situe sur une serie
			if (this.series != -1) {
				// on renvoie le y correspondant
				y = DatasetUtils.findYValue(plot.getDataset(), this.series, x);
			}

			// on met a jour les valeurs
			this.crossX.setValue(x);
			this.crossY.setValue(y);
		}
	}

	public double getXValue() {
		return this.crossX.getValue();
	}

	public void setXValue(double x) {
		this.crossX.setValue(x);
	}

	public void setPaint(Paint seriesPaint) {
		this.crossX.setPaint(seriesPaint);
	}

	public int getSeries() {
		return this.series;
	}

	public boolean isXLocked() {
		return this.xLocked;

	}

	public Comparable getKey() {
		return this.key;
	}

	public void setKey(Comparable key) {
		this.key = key;
	}

	public void setJValueSetter(JValueSetter jValueSetter) {
		this.jValueSetter = jValueSetter;
	}
	
	protected Crosshair getCrossX() {
		return this.crossX;
	}

}