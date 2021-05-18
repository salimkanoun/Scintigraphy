package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import ij.Prefs;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabSalivaryGlands;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

class TabPrecoce extends TabResult {
    private final BufferedImage capture;

    public TabPrecoce(BufferedImage capture, FenResults parent){
        super(parent, "Early", true);
        this.capture = capture;

        this.reloadDisplay();

    }

    @Override
    public Component getSidePanelContent() {
        JPanel flow_wrap = new JPanel();
        Box panRes = Box.createVerticalBox();


        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelHeart());

        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelLiver());


        flow_wrap.add(panRes);

        return flow_wrap;
    }


    private Component getPanelHeart(){
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();


        // panel de timing
        double heart_t1 = model.getResults().get("Heart").get("t1");
        double heart_t2 = model.getResults().get("Heart").get("t2");
        double heart_AUC = model.getResults().get("Heart").get("AUC");


        JPanel pnl_heart = new JPanel(new GridLayout(4, 2, 0, 3));
        pnl_heart.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        pnl_heart.add(new JLabel(" Heart "));
        pnl_heart.add(new JLabel("  "));


        JLabel t1 = new JLabel(" T1");
        pnl_heart.add(t1);

        JLabel val_t1 = new JLabel(String.valueOf(heart_t1));
        val_t1.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_t1);

        JLabel t2 = new JLabel(" T2");
        pnl_heart.add(t2);

        JLabel val_t2 = new JLabel(String.valueOf(heart_t2));
        val_t2.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_t2);

        JLabel auc = new JLabel(" AUC");
        pnl_heart.add(auc);

        JLabel val_auc = new JLabel(String.valueOf(heart_AUC));
        val_auc.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_auc);

        return pnl_heart;
    }


    private Component getPanelLiver(){
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();


        // panel de timing
        double liver_t1 = model.getResults().get("Liver").get("t1");
        double liver_t2 = model.getResults().get("Liver").get("t2");


        JPanel pnl_liver = new JPanel(new GridLayout(3, 3, 0, 3));
        pnl_liver.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        pnl_liver.add(new JLabel(" Liver "));
        pnl_liver.add(new JLabel("  "));


        JLabel t1 = new JLabel(" T1");
        pnl_liver.add(t1);

        JLabel val_t1 = new JLabel(String.valueOf(liver_t1));
        val_t1.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_liver.add(val_t1);

        JLabel t2 = new JLabel(" T2");
        pnl_liver.add(t2);

        JLabel val_t2 = new JLabel(String.valueOf(liver_t2));
        val_t2.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_liver.add(val_t2);



        return pnl_liver;
    }


    @Override
    public Container getResultContent() {

        JPanel grid = new JPanel(new GridLayout(2,1));

        //ajout image capture et montage
        JPanel panel_top = new JPanel(new GridLayout(1,2));
        panel_top.add(new DynamicImage(capture));
        grid.add(panel_top);

        //ajout du graphique image precoce
        String [][] asso = new String [][]{{"Liver", "Heart"}};
        List<XYSeries> series = ((ModelScinDyn) this.getParent().getModel()).getSeries();

        ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);
        JValueSetter timeChart = prepareValueSetter(cp[0]);
      //  cp[0].getChart().setTitle("Heart and Liver");
        grid.add(timeChart);
        timeChart.removeChartMouseListener(timeChart);
        return grid;



    }

    private JValueSetter prepareValueSetter(ChartPanel chart) {
        chart.getChart().getPlot().setBackgroundPaint(null);
        JValueSetter jvs = new JValueSetter(chart.getChart());

        double delay = ((Model_Scintivol) this.getParent().getModel()).getTracerDelayTime();
        double startTime = (delay + 150) / 60;
        double endTime = (delay + 350) / 60;
        Selector t1 = new Selector("t1", startTime, -1, RectangleAnchor.BOTTOM_LEFT);
        Selector t2 = new Selector("t2", endTime, -1, RectangleAnchor.BOTTOM_LEFT);
        jvs.addSelector(t1, "t1");
        jvs.addSelector(t2, "t2");

        // renomme les series du chart pour que l'interface soit plus comprehensible
        XYSeriesCollection dataset = (XYSeriesCollection) chart.getChart().getXYPlot().getDataset();
        dataset.getSeries("Heart").setKey("Heart");
        dataset.getSeries("Liver").setKey("Liver");



        return jvs;
    }
}
