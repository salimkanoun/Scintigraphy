package org.petctviewer.scintigraphy.salivaryGlands.gui;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
import org.petctviewer.scintigraphy.scin.gui.*;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands.*;

class TabMain extends TabResult {

    private final BufferedImage capture;
    private JFreeChart chartMain;

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
        panRes.add(Box.createVerticalStrut(30));
        panRes.add(this.getCheckboxsChart());

        flow_wrap.add(panRes);

        return flow_wrap;

    }


    private Component getPanelRatios() {
        ModelSalivaryGlands model = (ModelSalivaryGlands) this.parent.getModel();
        JPanel panelRes = new JPanel();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Uptake Ratio", "Excretion Fraction"};
        Map<String, Map<Integer, Double>> results = model.getResults();
        Object[][] data = new Object[4][3];
        for (int i = 0; i < data.length; i++) {
            String gland = model.getGlands().get(i);
            data[i][0] = gland;
            data[i][1] = results.get(gland).get(RES_UPTAKE_RATIO.hashCode());
            data[i][2] = results.get(gland).get(RES_EXCRETION_FRACTION.hashCode()) + " %";
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
        Map<String, Map<Integer, Double>> results = model.getResults();
        Object[][] data = new Object[4][5];
        for (int i = 0; i < data.length; i++) {
            String gland = model.getGlands().get(i);
            Map<Integer, Double> res = results.get(gland);
            data[i][0] = gland;
            data[i][1] = Library_Quantif.round(100 * res.get(RES_FIRST_MIN.hashCode()) / res.get(RES_MAX.hashCode()), 2) + " %";
            data[i][2] = Library_Quantif.round(100 * res.get(RES_MAX.hashCode()) / res.get(RES_MIN.hashCode()), 2) + " %";
            data[i][3] = Library_Quantif.round(100 * res.get(RES_MAX.hashCode()) / res.get(RES_LEMON.hashCode()), 2) + " %";
            data[i][4] = Library_Quantif.round(100 * res.get(RES_15_MIN.hashCode()) / res.get(RES_LEMON.hashCode()), 2) + " %";
        }

        table = new JTable(data, title);
        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        panelRes.add(p);

        return panelRes;
    }

    public JPanel getCheckboxsChart() {
        ModelSalivaryGlands model = (ModelSalivaryGlands) this.parent.getModel();
        JPanel res = new JPanel();

        JPanel container = new JPanel(new GridLayout(2, 1));
        String[] titleRows = model.getGlands().toArray(new String[0]);
        JCheckBoxRows tableCheckbox = new JCheckBoxRows(titleRows, e -> {
            JCheckBox selected = (JCheckBox) e.getSource();
            TabMain.this.setVisibilitySeriesMain(Integer.parseInt(selected.getName()), 0, selected.isSelected());
        });
        tableCheckbox.setAllChecked(true);

        container.add(new JLabel("Show curves"));
        container.add(tableCheckbox);

        res.add(container);

        return res;
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
        String[] asso = new String[]{REGION_LEFT_PAROTID, REGION_RIGHT_PAROTID, REGION_LEFT_SUBMANDIB, REGION_RIGHT_SUBMANDIB};
        ChartPanel cp = Library_JFreeChart.associateSeries(asso, series);
        this.chartMain = cp.getChart();
        JValueSetter citrusChart = prepareValueSetter(cp);
        grid.add(citrusChart);
        citrusChart.removeChartMouseListener(citrusChart);

        return grid;
    }

    private JValueSetter prepareValueSetter(ChartPanel chart) {
        chart.getChart().getPlot().setBackgroundPaint(null);
        JValueSetter jvs = new JValueSetter(chart.getChart());

        double lemonInjectionTime = ((ModelSalivaryGlands) this.getParent().getModel()).getLemonInjection();
        Selector lemon = new Selector("Lemon juice stimuli", lemonInjectionTime, -1, RectangleAnchor.BOTTOM_LEFT);

        jvs.addSelector(lemon, "lemon");

        // renomme les series du chart pour que l'interface soit plus comprehensible
        XYSeriesCollection dataset = (XYSeriesCollection) chart.getChart().getXYPlot().getDataset();
        dataset.getSeries(REGION_LEFT_PAROTID).setKey("Left Parotid");
        dataset.getSeries(REGION_RIGHT_PAROTID).setKey("Right Parotid");
        dataset.getSeries(REGION_LEFT_SUBMANDIB).setKey("Left SubMandible");
        dataset.getSeries(REGION_RIGHT_SUBMANDIB).setKey("Right SubMandible");

        return jvs;
    }

    public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
        XYItemRenderer renderer = this.chartMain.getXYPlot().getRenderer();
        // x+4 4: car on a 4 colonnes
        renderer.setSeriesVisible(x + y, visibility);
    }
}

