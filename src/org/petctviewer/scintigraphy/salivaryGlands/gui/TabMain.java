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
        panRes.add(this.getPanelSizeParotid());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelSizeSubmandibular());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelUptakeParotid());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelUptakeSubmandilar());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelExcrParotid());
        panRes.add(Box.createVerticalStrut(10));
        panRes.add(this.getPanelExcrSubmandibular());
        panRes.add(Box.createVerticalStrut(10));

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
    private Component getPanelExcrParotid() {
        JLabel label_L = new JLabel("L. Parotid");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotid");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Excr
        Map<String, Double> excrP= ((ModelSalivaryGlands) this.parent.getModel()).getExcretionFraction();
        JPanel panel_ExcrParo = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_ExcrParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_ExcrParo.add(new JLabel(" Excretion Fraction"));
        panel_ExcrParo.add(label_L);
        panel_ExcrParo.add(label_R);

        panel_ExcrParo.add(new JLabel(" "));
        JLabel lbl_excrpL = new JLabel(excrP.get("L. Parotid") + " % ");
        lbl_excrpL.setHorizontalAlignment(JLabel.CENTER);
        panel_ExcrParo.add(lbl_excrpL);

        JLabel lbl_excrpR = new JLabel(excrP.get("R. Parotid") + " % ");
        lbl_excrpR.setHorizontalAlignment(JLabel.CENTER);
        panel_ExcrParo.add(lbl_excrpR);

        return panel_ExcrParo;
    }

    private Component getPanelExcrSubmandibular() {
        JLabel label_L = new JLabel("L. Submandib");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Submandib");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Excr
        Map<String, Double> excrS= ((ModelSalivaryGlands) this.parent.getModel()).getExcretionFraction();
        JPanel panel_ExcrSubman = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_ExcrSubman.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_ExcrSubman.add(new JLabel(" Excretion Fraction"));
        panel_ExcrSubman.add(label_L);
        panel_ExcrSubman.add(label_R);

        panel_ExcrSubman.add(new JLabel(""));
        JLabel lbl_excrSL = new JLabel(excrS.get("L. SubMandib") + " % ");
        lbl_excrSL.setHorizontalAlignment(JLabel.CENTER);
        panel_ExcrSubman.add(lbl_excrSL);

        JLabel lbl_excrSR = new JLabel(excrS.get("R. SubMandib") + " % ");
        lbl_excrSR.setHorizontalAlignment(JLabel.CENTER);
        panel_ExcrSubman.add(lbl_excrSR);
        return panel_ExcrSubman;
    }


    private Component getPanelUptakeParotid() {
        JLabel label_L = new JLabel("L. Parotid");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotid");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Uptake
        Map<String, Double> uptake = ((ModelSalivaryGlands) this.parent.getModel()).getUptakeRatio();
        JPanel panel_UptakeParo = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_UptakeParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_UptakeParo.add(new JLabel(" Uptake Ratio"));
        panel_UptakeParo.add(label_L);
        panel_UptakeParo.add(label_R);
        panel_UptakeParo.add(new JLabel(""));

        JLabel lbl_uptakePL = new JLabel(uptake.get("L. Parotid") + "  ");
        lbl_uptakePL.setHorizontalAlignment(JLabel.CENTER);
        panel_UptakeParo.add(lbl_uptakePL);

        JLabel lbl_uptakePR = new JLabel(uptake.get("R. Parotid") + "  ");
        lbl_uptakePR.setHorizontalAlignment(JLabel.CENTER);
        panel_UptakeParo.add(lbl_uptakePR);
        return panel_UptakeParo;
    }

    private Component getPanelUptakeSubmandilar() {
        JLabel label_L = new JLabel("L. SubMandibular");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. SubMandibular");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Uptake
       Map<String, Double> uptake = ((ModelSalivaryGlands) this.parent.getModel()).getUptakeRatio();
        JPanel panel_UptakeSubMan = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_UptakeSubMan.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_UptakeSubMan.add(new JLabel(" Uptake Ratio"));
        panel_UptakeSubMan.add(label_L);
        panel_UptakeSubMan.add(label_R);
        panel_UptakeSubMan.add(new JLabel(""));

        JLabel lbl_uptakeSL = new JLabel(uptake.get("L. SubMandib") + " ");
        lbl_uptakeSL.setHorizontalAlignment(JLabel.CENTER);
        panel_UptakeSubMan.add(lbl_uptakeSL);

        JLabel lbl_uptakeSR = new JLabel(uptake.get("R. SubMandib") + " ");
        lbl_uptakeSR.setHorizontalAlignment(JLabel.CENTER);
        panel_UptakeSubMan.add(lbl_uptakeSR);
        return panel_UptakeSubMan;
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

