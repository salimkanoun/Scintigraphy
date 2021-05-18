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
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabSalivaryGlands;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class TabTardive extends TabResult {
        private final BufferedImage capture;

        public TabTardive(BufferedImage capture, FenResults parent){
            super(parent, "Late", true);
            this.capture = capture;

            this.reloadDisplay();

        }

        @Override
        public Component getSidePanelContent() {
            JPanel flow_wrap = new JPanel();
            Box panRes = Box.createVerticalBox();




            panRes.add(Box.createVerticalStrut(10));
            panRes.add(this.getPanelLiver());


            flow_wrap.add(panRes);

            return flow_wrap;
        }




        private Component getPanelLiver(){
            Model_Scintivol model = (Model_Scintivol) this.parent.getModel();


            // panel de timing
            double liver_t1 = model.getResults().get("Liver Parenchyma").get("max");
            double liver_t2 = model.getResults().get("Liver Parenchyma").get("end");
            double retention = model.getResults().get("Other").get("Retention rate");


            JPanel pnl_liver = new JPanel(new GridLayout(4, 3, 0, 3));
            pnl_liver.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            pnl_liver.add(new JLabel(" Liver Parenchyma "));
            pnl_liver.add(new JLabel("  "));


            JLabel t1 = new JLabel(" MAX");
            pnl_liver.add(t1);

            JLabel val_t1 = new JLabel(String.valueOf(liver_t1));
            val_t1.setHorizontalAlignment(SwingConstants.CENTER);
            pnl_liver.add(val_t1);

            JLabel t2 = new JLabel(" END");
            pnl_liver.add(t2);

            JLabel val_t2 = new JLabel(String.valueOf(liver_t2));
            val_t2.setHorizontalAlignment(SwingConstants.CENTER);
            pnl_liver.add(val_t2);

            JLabel ret = new JLabel(" Retention rate");
            pnl_liver.add(ret);

            JLabel val_ret = new JLabel(String.valueOf(retention) + " %");
            val_ret.setHorizontalAlignment(SwingConstants.CENTER);
            pnl_liver.add(val_ret);



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
            String [][] asso = new String [][]{{"Liver parenchyma"}};
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
            Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
            //model.getResults().get("Other").get("Retention rate");




            double maxTime = Prefs.get(PrefTabSalivaryGlands.PREF_CITRUS_INJECT_TIME, 107.36);
            double endTime = Prefs.get(PrefTabSalivaryGlands.PREF_CITRUS_INJECT_TIME, 103.58);
            Selector max = new Selector("max", maxTime, -1, RectangleAnchor.BOTTOM_LEFT);
            Selector end = new Selector("end produit", endTime, -1, RectangleAnchor.BOTTOM_LEFT);
            jvs.addSelector(max, "start");
            jvs.addSelector(end, "end");

            // renomme les series du chart pour que l'interface soit plus comprehensible
            XYSeriesCollection dataset = (XYSeriesCollection) chart.getChart().getXYPlot().getDataset();
            dataset.getSeries("Liver parenchyma").setKey("Liver parenchyma");


            return jvs;
        }
    }



