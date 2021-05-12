package org.petctviewer.scintigraphy.salivaryGlands.gui;

import ij.Prefs;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.petctviewer.scintigraphy.calibration.resultats.JTableCheckBox;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.renal.Selector;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
import org.petctviewer.scintigraphy.scin.gui.*;
import org.petctviewer.scintigraphy.scin.library.Library_JFreeChart;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabSalivaryGlands;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

class TabMain extends TabResult {

    private final BufferedImage capture;
    private JFreeChart chartMain;
    private JCheckBoxRows tableCheckbox;

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
            data[i][1] = Library_Quantif.round(100 * res.get("First Minute") / res.get("Maximum"), 2) + " %";
            data[i][2] = Library_Quantif.round(100 * res.get("Maximum") / res.get("Minimum"), 2) + " %";
            data[i][3] = Library_Quantif.round(100 * res.get("Maximum") / res.get("Lemon"), 2) + " %";
            data[i][4] = Library_Quantif.round(100 * res.get("15 Minutes") / res.get("Lemon"), 2) + " %";
        }

        table = new JTable(data, title);
        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        panelRes.add(p);

        return panelRes;
    }

    public JPanel getCheckboxsChart() {
        ModelSalivaryGlands model = (ModelSalivaryGlands) this.parent.getModel();
        JPanel res = new JPanel(new GridLayout(2, 1));

        String[] titleRows = model.getGlands().toArray(new String[0]);
        this.tableCheckbox = new JCheckBoxRows(titleRows, e -> {
            JCheckBox selected = (JCheckBox) e.getSource();
            TabMain.this.setVisibilitySeriesMain(Integer.parseInt(selected.getName()), 0, selected.isSelected());
        });
        this.tableCheckbox.setAllChecked(true);

        //res.add(new JLabel("Show curves"));
        res.add(this.tableCheckbox);

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
        String[][] asso = new String[][]{{"L. Parotid", "R. Parotid", "L. SubMandib", "R. SubMandib"}};
        ChartPanel[] cp = Library_JFreeChart.associateSeries(asso, series);
        this.chartMain = cp[0].getChart();
        JValueSetter citrusChart = prepareValueSetter(cp[0]);
        grid.add(citrusChart);
        citrusChart.removeChartMouseListener(citrusChart);

        return grid;
    }

    private JValueSetter prepareValueSetter(ChartPanel chart) {
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

    public void setVisibilitySeriesMain(int x, int y, boolean visibility) {
        XYItemRenderer renderer = this.chartMain.getXYPlot().getRenderer();
        // x+4 4: car on a 4 colonnes
        renderer.setSeriesVisible(x + y, visibility);
    }
}

