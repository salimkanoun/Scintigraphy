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

public class TabMain extends TabResult {

    private final BufferedImage capture;

    public TabMain(BufferedImage capture, FenResults parent){
        super(parent, "Main", true);
        this.capture = capture;

        this.reloadDisplay();
    }

    @Override
    public Component getSidePanelContent() {
        boolean[] parotides = ((ModelSalivaryGlands) this.parent.getModel()).getParotides();

        JPanel flow_wrap = new JPanel();

        //création du panel d'affichage des pourcentages
        Box panRes = Box.createVerticalBox();

        if (parotides[0] && parotides[1]){
            panRes.add(this.getPanelSep());

        }

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

    private Component getPanelSep() {
        JLabel label_L = new JLabel("L. Parotide");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotide");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel de fonction separee
        Double[] sep = ((ModelSalivaryGlands) this.parent.getModel()).getSeparatedFunction();
        JPanel pnl_sep = new JPanel(new GridLayout(2, 3));
        pnl_sep.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        pnl_sep.add(new JLabel("Relative function"));
        pnl_sep.add(label_L);
        pnl_sep.add(label_R);

        pnl_sep.add(new JLabel("integral"));
        JLabel lbl_d = new JLabel(sep[0] + " %");
        lbl_d.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_sep.add(lbl_d);

        JLabel lbl_g = new JLabel(sep[1] + " %");
        lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_sep.add(lbl_g);



        return pnl_sep;

    }

    private Component getPanelSizeParotid(){
        JLabel label_L = new JLabel("L. Parotid");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotid");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        Double[] size = ((ModelSalivaryGlands) this.parent.getModel()).getSize();
        JPanel panel_sizeParo = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_sizeParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        panel_sizeParo.add(new JLabel(" Parotid Size"));
        panel_sizeParo.add(label_L);
        panel_sizeParo.add(label_R);

        panel_sizeParo.add(new JLabel(""));
        JLabel lbl_heightL = new JLabel(size[0] + " cm");
        lbl_heightL.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeParo.add(lbl_heightL);

        JLabel lbl_heightR = new JLabel(size[1] + " cm");
        lbl_heightR.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeParo.add(lbl_heightR);

        return panel_sizeParo;

    }
    private Component getPanelSizeSubmandibular(){
        JLabel label_L = new JLabel("L. SubMandibular");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. SubMandibular");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        Double[] size = ((ModelSalivaryGlands) this.parent.getModel()).getSize();
        JPanel panel_sizeSub = new JPanel(new GridLayout(2, 3, 0, 3));
        panel_sizeSub.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        panel_sizeSub.add(new JLabel(" SubMandibular Size"));
        panel_sizeSub.add(label_L);
        panel_sizeSub.add(label_R);

        panel_sizeSub.add(new JLabel(""));
        JLabel lbl_heightL = new JLabel(size[0] + " cm");
        lbl_heightL.setHorizontalAlignment(JLabel.CENTER);
        panel_sizeSub.add(lbl_heightL);

        JLabel lbl_heightR = new JLabel(size[1] + " cm");
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
        Double[][] Excr = ((ModelSalivaryGlands) this.parent.getModel()).getExcr();
        JPanel panel_ExcrParo = new JPanel(new GridLayout(4, 3, 0, 3));
        panel_ExcrParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_ExcrParo.add(new JLabel(" Excretion ratio"));
        panel_ExcrParo.add(label_L);
        panel_ExcrParo.add(label_R);
        for (int i = 0; i < 3; i++) {
            // aligne a droite
            panel_ExcrParo.add(new JLabel(Excr[0][i] + "  min"));

            for (int j = 1; j <= 2; j++) {
                if (Excr[j][i] != null) {
                    JLabel lbl_g = new JLabel(Excr[j][i] + " %");
                    lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_ExcrParo.add(lbl_g);
                } else {
                    JLabel lbl_na = new JLabel("N/A");
                    lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_ExcrParo.add(lbl_na);
                }
            }

        }
        return panel_ExcrParo;
    }

    private Component getPanelExcrSubmandibular() {
        JLabel label_L = new JLabel("L. SubMandibular");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. SubMandibular");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Excr
        Double[][] Excr = ((ModelSalivaryGlands) this.parent.getModel()).getExcr();
        JPanel panel_ExcrSubman = new JPanel(new GridLayout(4, 3, 0, 3));
        panel_ExcrSubman.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_ExcrSubman.add(new JLabel(" Excretion Fraction"));
        panel_ExcrSubman.add(label_L);
        panel_ExcrSubman.add(label_R);
        for (int i = 0; i < 3; i++) {
            // aligne a droite
            panel_ExcrSubman.add(new JLabel(Excr[0][i] + "  min"));

            for (int j = 1; j <= 2; j++) {
                if (Excr[j][i] != null) {
                    JLabel lbl_g = new JLabel(Excr[j][i] + " %");
                    lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_ExcrSubman.add(lbl_g);
                } else {
                    JLabel lbl_na = new JLabel("N/A");
                    lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_ExcrSubman.add(lbl_na);
                }
            }

        }
        return panel_ExcrSubman;
    }


    private Component getPanelUptakeParotid() {
        JLabel label_L = new JLabel("L. Parotid");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. Parotid");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Uptake
        Double[][] Uptake = ((ModelSalivaryGlands) this.parent.getModel()).getExcr();
        JPanel panel_UptakeParo = new JPanel(new GridLayout(4, 3, 0, 3));
        panel_UptakeParo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_UptakeParo.add(new JLabel(" Uptake Ratio"));
        panel_UptakeParo.add(label_L);
        panel_UptakeParo.add(label_R);
        for (int i = 0; i < 3; i++) {
            // aligne a droite
            panel_UptakeParo.add(new JLabel(Uptake[0][i] + "  min"));

            for (int j = 1; j <= 2; j++) {
                if (Uptake[j][i] != null) {
                    JLabel lbl_g = new JLabel(Uptake[j][i] + " %");
                    lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_UptakeParo.add(lbl_g);
                } else {
                    JLabel lbl_na = new JLabel("N/A");
                    lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_UptakeParo.add(lbl_na);
                }
            }

        }
        return panel_UptakeParo;
    }

    private Component getPanelUptakeSubmandilar() {
        JLabel label_L = new JLabel("L. SubMandibular");
        label_L.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel label_R = new JLabel("R. SubMandibular");
        label_R.setHorizontalAlignment(SwingConstants.CENTER);

        // panel Uptake
        Double[][] Uptake = ((ModelSalivaryGlands) this.parent.getModel()).getExcr();
        JPanel panel_UptakeSubMan = new JPanel(new GridLayout(4, 3, 0, 3));
        panel_UptakeSubMan.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        panel_UptakeSubMan.add(new JLabel(" Uptake Ratio"));
        panel_UptakeSubMan.add(label_L);
        panel_UptakeSubMan.add(label_R);
        for (int i = 0; i < 3; i++) {
            // aligne a droite
            panel_UptakeSubMan.add(new JLabel(Uptake[0][i] + "  min"));

            for (int j = 1; j <= 2; j++) {
                if (Uptake[j][i] != null) {
                    JLabel lbl_g = new JLabel(Uptake[j][i] + " %");
                    lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_UptakeSubMan.add(lbl_g);
                } else {
                    JLabel lbl_na = new JLabel("N/A");
                    lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
                    panel_UptakeSubMan.add(lbl_na);
                }
            }

        }
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
        JValueSetter chartNephrogram = ((ModelSalivaryGlands) this.parent.getModel()).getNephrogramChart();
        ImagePlus proj = ZProjector.run(parent.getModel().getImagePlus(), "sum", slice1, slice2);
        proj.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
        JPanel grid = new JPanel(new GridLayout(2, 1));

        // creation du panel du haut
        JPanel panel_top = new JPanel(new GridLayout(1, 2));

        // ajout de la capture et du montage
        panel_top.add(new DynamicImage(capture));
        panel_top.add(new DynamicImage(proj.getImage()));

        // on ajoute les panels a la grille principale
        grid.add(panel_top);
        grid.add(chartNephrogram);

        chartNephrogram.removeChartMouseListener(chartNephrogram);

        return grid;

    }
}

