package org.petctviewer.scintigraphy.renal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;

public class YSelector extends Selector implements ChartMouseListener {
	private static final long serialVersionUID = 6794595703667698248L;
	private Crosshair crossY;
	private List<Crosshair> crossX;

	// si le selecteur n'est pas selectionne
	private boolean yLocked;

	// serie sur laquelle se situe le selecteur
	private int series;

	private Comparable key;
	private JValueSetter jValueSetter;

	/**
	 * Permet de creer un selecteur deplacable sur un courbe
	 * 
	 * @param nom    nom du selecteur (null accepte)
	 * @param startX position de depart du selecteur
	 * @param series series observee (-1 si aucune)
	 * @param anchor position du label
	 */
	public YSelector(String nom, double startY, int series, RectangleAnchor anchor) {
		super(nom, startY, series, anchor);
		this.series = series;
		this.yLocked = true;
		this.crossX = new ArrayList<>();

		// on intialise le selecteur vertical
		this.crossY = new Crosshair(startY, Color.GRAY, new BasicStroke(0f));

		// on place le label a l'endroit demande
		this.crossY.setLabelOutlineVisible(false);

		if (anchor != null) {
			this.crossY.setLabelAnchor(anchor);
		} else {
			this.crossY.setLabelAnchor(RectangleAnchor.BOTTOM);
		}

		// on rend le label invisible si le nom est null ou si c'est un espace
		this.crossY.setLabelGenerator(new CrosshairLabelGenerator() {
			@Override
			public String generateLabel(Crosshair crosshair) {
				if (nom == null || nom.trim().equals("")) {
					YSelector.this.crossY.setLabelVisible(false);
				}
				return nom;
			}
		});

		// le selecteur vertical n'est pour l'instant pas affiche
//		this.crossX.add(new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f)));
		this.crossY.setLabelVisible(true);
//		this.crossX.get(0).setLabelVisible(true);

		// on ajoute les selecteurs horizontaux et verticaux
//		this.addDomainCrosshair(this.crossX.get(0));
		this.addRangeCrosshair(this.crossY);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// on bloque, ou debloque le selecteur vertical
		this.yLocked = !this.yLocked;

		// on rend visible le selecteur horizontal si le selecteur horizontal est
		// debloque
		for(Crosshair ch : this.crossX)
			ch.setVisible(!this.yLocked);
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		XYPlot plot = (XYPlot) event.getChart().getPlot();
		
		// si le selecteur vertical n'est pas bloque
		if (!this.yLocked) {
			ValueAxis yAxis = plot.getRangeAxis();
			
//			ValueAxis xAxis = plot.getDomainAxis();

			// on calcule la nouvelle valeur du selecteur vertical
			double y = yAxis.java2DToValue(event.getTrigger().getY(), this.jValueSetter.getScreenDataArea(),
					RectangleEdge.LEFT);
//			double x = Double.NaN;
			
			for(Crosshair ch : this.crossX)
				this.removeDomainCrosshair(ch);
			this.crossX.clear();
			
			Number xValue;
			double previousValue = (double) plot.getDataset().getY(this.series, 0);
	        for (int itemIndex = 0 ; itemIndex < plot.getDataset().getItemCount(this.series) ; itemIndex ++){
	            Number yValue = (double) plot.getDataset().getY(this.series, itemIndex);
	            if (((double) yValue) == y || y == previousValue || (((double) yValue) <= y && y <= previousValue) || (((double) yValue) >= y && y >= previousValue)){
	            	System.out.println(yValue+" = "+y);
	            	xValue = plot.getDataset().getX(this.series, itemIndex);
	            	xValue = findYValue(plot.getDataset(), this.series, y);
	                this.crossX.add(new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f)));
	                this.crossX.get(this.crossX.size() - 1).setValue(xValue.doubleValue());
	                this.crossX.get(this.crossX.size() - 1).setLabelVisible(true);
	                this.addDomainCrosshair(this.crossX.get(this.crossX.size() - 1));
	                System.out.println("\tValeurs de x pour moi : "+xValue);
	            }
	            previousValue = (double) yValue;
	        }
			// si on se situe sur une serie
//			if (this.series != -1) {
//				// on renvoie le y correspondant
//				x = DatasetUtils.findYValue(plot.getDataset(), this.series, y);
//			}

			// on met a jour les valeurs
//			this.crossX.setValue(x);
			this.crossY.setValue(y);
		}
	}

	public double getXValue() {
		return this.crossY.getValue();
	}

	public void setXValue(double x) {
		this.crossY.setValue(x);
	}

	public void setPaint(Paint seriesPaint) {
		this.crossY.setPaint(seriesPaint);
	}

	public int getSeries() {
		return this.series;
	}

	public boolean isXLocked() {
		return this.yLocked;

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
	
	
	
	
	
	/**
     * Finds the indices of the the items in the dataset that span the 
     * specified y-value.  There are three cases for the return value:
     * <ul>
     * <li>there is an exact match for the y-value at index i 
     * (returns {@code int[] {i, i}});</li>
     * <li>the y-value falls between two (adjacent) items at index i and i+1 
     * (returns {@code int[] {i, i+1}});</li>
     * <li>the y-value falls outside the domain bounds, in which case the 
     *    method returns {@code int[] {-1, -1}}.</li>
     * </ul>
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series index.
     * @param y  the y-value.
     *
     * @return The indices of the two items that span the x-value.
     *
     * @since 1.0.16
     * 
     * @see #findYValue(org.jfree.data.xy.XYDataset, int, double) 
     */
    public static int[] findItemIndicesForY(XYDataset dataset, int series,
            double x) {
        Args.nullNotPermitted(dataset, "dataset");
        int itemCount = dataset.getItemCount(series);
        if (itemCount == 0) {
            return new int[] {-1, -1};
        }
        if (itemCount == 1) {
            if (x == dataset.getYValue(series, 0)) {
                return new int[] {0, 0};
            } else {
                return new int[] {-1, -1};
            }
        }
        if (dataset.getDomainOrder() == DomainOrder.ASCENDING) {
            int low = 0;
            int high = itemCount - 1;
            double lowValue = dataset.getYValue(series, low);
            if (lowValue > x) {
                return new int[] {-1, -1};
            }
            if (lowValue == x) {
                return new int[] {low, low};
            }
            double highValue = dataset.getYValue(series, high);
            if (highValue < x) {
                return new int[] {-1, -1};
            }
            if (highValue == x) {
                return new int[] {high, high};
            }
            int mid = (low + high) / 2;
            while (high - low > 1) {
                double midV = dataset.getYValue(series, mid);
                if (x == midV) {
                    return new int[] {mid, mid};
                }
                if (midV < x) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return new int[] {low, high};
        }
        else if (dataset.getDomainOrder() == DomainOrder.DESCENDING) {
            int high = 0;
            int low = itemCount - 1;
            double lowValue = dataset.getYValue(series, low);
            if (lowValue > x) {
                return new int[] {-1, -1};
            }
            double highValue = dataset.getYValue(series, high);
            if (highValue < x) {
                return new int[] {-1, -1};
            }
            int mid = (low + high) / 2;
            while (high - low > 1) {
                double midV = dataset.getYValue(series, mid);
                if (x == midV) {
                    return new int[] {mid, mid};
                }
                if (midV < x) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return new int[] {low, high};
        }
        else {
            // we don't know anything about the ordering of the x-values,
            // so we iterate until we find the first crossing of x (if any)
            // we know there are at least 2 items in the series at this point
            double prev = dataset.getYValue(series, 0);
            if (x == prev) {
                return new int[] {0, 0}; // exact match on first item
            }
            for (int i = 1; i < itemCount; i++) {
                double next = dataset.getYValue(series, i);
                if (x == next) {
                    return new int[] {i, i}; // exact match
                }
                if ((x > prev && x < next) || (x < prev && x > next)) {
                    return new int[] {i - 1, i}; // spanning match
                }
            }
            return new int[] {-1, -1}; // no crossing of x
        }
    }
    
    public static double findYValue(XYDataset dataset, int series, double x) {
        // delegate null check on dataset
        int[] indices = findItemIndicesForY(dataset, series, x);
        if (indices[0] == -1) {
        	System.out.println("\t\t\tFail dans la trouvail !");
            return Double.NaN;
        }
        if (indices[0] == indices[1]) {
            return dataset.getXValue(series, indices[0]);
        }
        double x0 = dataset.getYValue(series, indices[0]);
        double x1 = dataset.getYValue(series, indices[1]);
        double y0 = dataset.getXValue(series, indices[0]);
        double y1 = dataset.getXValue(series, indices[1]);
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }
	
}
