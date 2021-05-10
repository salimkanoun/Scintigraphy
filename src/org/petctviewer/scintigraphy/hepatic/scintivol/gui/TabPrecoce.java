package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

class TabPrecoce extends TabResult {
    private final BufferedImage capture;

    public TabPrecoce(BufferedImage capture, FenResults parent){
        super(parent, "Précoce", true);
        this.capture = capture;
    }

    @Override
    public Component getSidePanelContent() {
        JPanel grid = new JPanel(new GridLayout(2,1));

        //ajout image capture et montage
        JPanel panel_top = new JPanel(new GridLayout(1,2));
        panel_top.add(new DynamicImage(capture));
        grid.add(panel_top);

        //ajout du graphique image precoce
        List<XYSeries> series = ((ModelScinDyn) this.getParent().getModel()).getSeries();
        String [][] asso = new String [][]{{"Liver", "Heart"}};
        ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);
        JValueSetter precoceChart = prepareValueSetter(cp[0]);
        grid.add(precoceChart);
        precoceChart.removeChartMouseListener(precoceChart);

        return grid;


    }

    private JValueSetter prepareValueSetter(ChartPanel chartPanel) {
        chartPanel.getChart().getPlot().setBackgroundPaint(null);
        JValueSetter jvs = new JValueSetter(chartPanel.getChart());

        // renomme les series du chart pour que l'interface soit plus comprehensible
        XYSeriesCollection dataset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();
        dataset.getSeries("Liver").setKey("Liver");
        dataset.getSeries("Heart").setKey("Heart");

        return jvs;

    }

    @Override
    public Container getResultContent() {
        return null;
    }
}
