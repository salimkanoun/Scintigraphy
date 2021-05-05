package org.petctviewer.scintigraphy.salivaryGlands.gui;

import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TabMain extends TabResult {

    private final BufferedImage capture;

    public TabMain(BufferedImage capture, FenResults parent){
        super(parent, "Main", true);
        this.capture = capture;

        this.reloadDisplay();
    }

    @Override
    public Component getSidePanelContent() {
        //
        JPanel flow_wrap = new JPanel();

        //création du panel d'affichage des pourcentages
        Box panRes = Box.createVerticalBox();

        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelRatios());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelIndexes());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelSizeParotid());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelSizeSubmandibular());

        flow_wrap.add(panRes);

        return flow_wrap;

    }

    private Component getPanelSizeParotid(){
        JLabel label_L = new JLabel("L. Parotid");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotid");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        Map<String, Double> size = ((ModelSalivaryGlands) this.parent.getModel()).getSize();
        JPanel panel_sizeParo = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_sizeParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        panel_sizeParo.add(new JLabel(" Parotid Size"));
        panel_sizeParo.add(label_L);
        panel_sizeParo.add(label_R);

        panel_sizeParo.add(new JLabel(""));
        JLabel lbl_heightL = new JLabel(size.get("L. Parotid") + " cm");
        lbl_heightL.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeParo.add(lbl_heightL);

        JLabel lbl_heightR = new JLabel(size.get("R. Parotid") + " cm");
        lbl_heightR.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeParo.add(lbl_heightR);

        return panel_sizeParo;

    }
    private Component getPanelSizeSubmandibular(){
        JLabel label_L = new JLabel("L. SubMandibular");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. SubMandibular");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        Map<String, Double> size = ((ModelSalivaryGlands) this.parent.getModel()).getSize();
        JPanel panel_sizeSub = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_sizeSub.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        panel_sizeSub.add(new JLabel(" SubMandibular Size"));
        panel_sizeSub.add(label_L);
        panel_sizeSub.add(label_R);

        panel_sizeSub.add(new JLabel(""));
        JLabel lbl_heightL = new JLabel(size.get("L. SubMandib") + " cm");
        lbl_heightL.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeSub.add(lbl_heightL);

        JLabel lbl_heightR = new JLabel(size.get("R. SubMandib") + " cm");
        lbl_heightR.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeSub.add(lbl_heightR);

        return panel_sizeSub;

    }

    private Component getPanelRatios() {
        ModelSalivaryGlands model = (ModelSalivaryGlands) this.parent.getModel();
        JPanel panelRes = new JPanel();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Uptake Ratio", "Excretion Fraction"};
        Map<String, Map<String, Double>> results = model.getResults();
        Object[][] data = new Object[4][3];
        for (int i = 0; i < data.length; i++) {
            String gland = model.getGlands().get(i);
            data[i][0] = gland;
            data[i][1] = results.get(gland).get("Uptake Ratio");
            data[i][2] = results.get(gland).get("Excretion Fraction") + " %";
        }

        table = new JTable(data, title);
        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        panelRes.add(p);

        return panelRes;
    }

    private Component getPanelIndexes() {
        ModelSalivaryGlands model = (ModelSalivaryGlands) this.parent.getModel();
        JPanel panelRes = new JPanel();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "FM/Max", "Max/min", "Max/Lemon", "15min/Lemon"};
        Map<String, Map<String, Double>> results = model.getResults();
        Object[][] data = new Object[4][5];
        for (int i = 0; i < data.length; i++) {
            String gland = model.getGlands().get(i);
            Map<String, Double> res = results.get(gland);
            data[i][0] = gland;
            data[i][1] = Library_Quantif.round(res.get("First Minute") / res.get("Maximum"), 2) + " %";
            data[i][2] = Library_Quantif.round(res.get("Maximum") / res.get("Minimum"), 2) + " %";
            data[i][3] = Library_Quantif.round(res.get("Maximum") / res.get("Lemon"), 2) + " %";
            data[i][4] = Library_Quantif.round(res.get("15 Minutes") / res.get("Lemon"), 2) + " %";
        }

        table = new JTable(data, title);
        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        panelRes.add(p);

        return panelRes;
    }


    @Override
    public JPanel getResultContent() {
        JPanel grid = new JPanel(new GridLayout(2, 1));

        // ajout de la capture et du montage
        JPanel panel_top = new JPanel(new GridLayout(1, 2));
        panel_top.add(new DynamicImage(capture));
        grid.add(panel_top);

        // ajout du graphique
        List<XYSeries> series = ((ModelScinDyn) this.getParent().getModel()).getSeries();
        String[][] asso = new String[][]{{"L. Parotid", "R. Parotid", "L. SubMandib", "R. SubMandib"}};
        ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);
        JValueSetter citrusChart = prepareValueSetter(cp[0]);
        grid.add(citrusChart);
        citrusChart.removeChartMouseListener(citrusChart);

        return grid;
    }

    private static JValueSetter prepareValueSetter(ChartPanel chart) {
        chart.getChart().getPlot().setBackgroundPaint(null);
        JValueSetter jvs = new JValueSetter(chart.getChart());

        double lemonInjectionTime = Prefs.get(PrefTabSalivaryGlands.PREF_CITRUS_INJECT_TIME, 10);
        Selector lemon = new Selector("Lemon juice stimuli", lemonInjectionTime, -1, RectangleAnchor.BOTTOM_LEFT);

        jvs.addSelector(lemon, "lemon");

        // renomme les series du chart pour que l'interface soit plus comprehensible
        XYSeriesCollection dataset = (XYSeriesCollection) chart.getChart().getXYPlot().getDataset();
        dataset.getSeries("L. Parotid").setKey("Left Parotid");
        dataset.getSeries("R. Parotid").setKey("Right Parotid");
        dataset.getSeries("L. SubMandib").setKey("Left SubMandible");
        dataset.getSeries("R. SubMandib").setKey("Right SubMandible");

        return jvs;
    }
}

