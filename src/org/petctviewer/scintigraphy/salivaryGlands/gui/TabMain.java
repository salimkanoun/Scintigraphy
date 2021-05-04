package org.petctviewer.scintigraphy.salivaryGlands.gui;

import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import org.petctviewer.scintigraphy.renal.JValueSetter;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
import org.petctviewer.scintigraphy.scin.gui.DynamicImage;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.model.ModelScinDyn;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
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
        JPanel panelEF = new JPanel();
        JTable table;
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());

        String[] title = { "Name", "Uptake Ratio", "Excretion Fraction"};
        HashMap<String, Double> ur = model.getUptakeRatio();
        HashMap<String, Double> ef = model.getExcretionFraction();
        Object[][] data = new Object[4][3];
        for (int i = 0; i < data.length; i++) {
            data[i][0] = model.getGlands().get(i);
            data[i][1] = ur.get(model.getGlands().get(i));
            data[i][2] = ef.get(model.getGlands().get(i)) + " %";
        }
        table = new JTable(data, title);

        p.add(table.getTableHeader(), BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);

        panelEF.add(p);

        return panelEF;
    }


    @Override
    public JPanel getResultContent() {
        @SuppressWarnings("rawtypes")
        HashMap<Comparable, Double> adjusted = ((ModelSalivaryGlands) parent.getModel()).getAdjustedValues();
        Double x1 = adjusted.get("start");
       Double x2 = adjusted.get("end");
       double debut = Math.min(x1, x2);
       double fin = Math.max(x1, x2);

       int slice1 = ModelScinDyn.getSliceIndexByTime(debut * 60 * 1000,
               ((ModelSalivaryGlands) this.parent.getModel()).getFrameDuration());
        int slice2 = ModelScinDyn.getSliceIndexByTime(fin * 60 * 1000,
               ((ModelSalivaryGlands) this.parent.getModel()).getFrameDuration());
       JValueSetter citrusChart = ((ModelSalivaryGlands) this.parent.getModel()).getCitrusChart();
       ImagePlus proj = ZProjector.run(parent.getModel().getImagePlus(), "sum", slice1, slice2);
        proj.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
        JPanel grid = new JPanel(new GridLayout(2, 1));

        // creation du panel du haut
        JPanel panel_top = new JPanel(new GridLayout(1, 2));


        // ajout de la capture et du montage
        panel_top.add(new DynamicImage(capture));
       // panel_top.add(new DynamicImage(proj.getImage()));

        // on ajoute les panels a la grille principale
        grid.add(panel_top);
        grid.add(citrusChart);

        citrusChart.removeChartMouseListener(citrusChart);

        return grid;
    }
}

