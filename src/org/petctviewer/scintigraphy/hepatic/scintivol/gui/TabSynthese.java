package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import ij.plugin.Grid;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabResult;
import org.petctviewer.scintigraphy.scin.library.Library_Quantif;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class TabSynthese extends TabResult {


    public TabSynthese(FenResults parent){
        super(parent, "Synthesis", true);

        this.reloadDisplay();

    }

    @Override
    public Component getSidePanelContent() {
     return null;
    }

    @Override
    public Container getResultContent() {
        boolean isTomo = ((Model_Scintivol) this.parent.getModel()).getResults().containsKey("Tomo");

        //Panel principal
        JPanel flow_wrap = new JPanel(new GridLayout(0,1));
        //Panel données à collecter
        JPanel clearance = new JPanel(new GridLayout(5,1,0,3));
        JPanel ffr = new JPanel(new GridLayout(2,1,0,3));

        //panel des valeurs intermédiaires
        JPanel intermval = new JPanel(new GridLayout(4,1,0,3));

        //panel des résultats
        JPanel results = new JPanel(new GridLayout(0, 1, 0, 3));
        Box panRes = Box.createVerticalBox();
        flow_wrap.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        clearance.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        ffr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1));
        intermval.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1));
        results.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY,1));


        //données à collecter
        clearance.add(this.getPanelTitleDatas());
        clearance.add(this.getPanelTitleHepaticClearance());
        clearance.add(this.getPanelFov());
        clearance.add(this.getPanelLiver());
        clearance.add(this.getPanelHeart());

        //valeurs intermédiares
        intermval.add(this.getPanelTitleIntermediateValues());
        intermval.add(this.getIntermediateValuesCnormT2());
        intermval.add(this.getIntermediateValuesAUCCnorm());
        intermval.add(this.getIntermediateValuesBPActivity());

        //résultats
        results.add(this.getPanelTitleResults());
        results.add(this.getClearancenonNorm());
        results.add(this.getClearanceNorm());
        results.add(this.getRetention());
        if (isTomo) {
            results.add(this.getFFRnonNorm());
            results.add(this.getFFRNorm());
        }


        panRes.add(clearance);
        panRes.add(intermval);
        panRes.add(results);


        if (isTomo) {
            ffr.add(this.getTitlePanelFFR());
            ffr.add(this.getPanelFFR());
            panRes.add(ffr);
        }

        flow_wrap.add(panRes);

        return flow_wrap;

    }

    public Component getPanelTitleDatas(){
        JPanel title = new JPanel(new GridLayout(1,1));
        JLabel lbl_title = new JLabel("Data to be collected");
        lbl_title.setFont(new Font("Arial", Font.BOLD, 20));
        lbl_title.setHorizontalAlignment(JLabel.CENTER);

        title.add(lbl_title);

        return title;


    }

    public Component getPanelTitleResults(){
        JPanel title = new JPanel(new GridLayout(1,1));
        JLabel lbl_title = new JLabel("Results");
        lbl_title.setFont(new Font("Arial", Font.BOLD, 20));
        lbl_title.setHorizontalAlignment(JLabel.CENTER);

        title.add(lbl_title);

        return title;


    }



    public Component getPanelTitleIntermediateValues(){
        JPanel title = new JPanel(new GridLayout(1,1));
        JLabel lbl_title = new JLabel("Intermediate Values");
        lbl_title.setFont(new Font("Arial", Font.BOLD, 20));
        lbl_title.setHorizontalAlignment(JLabel.CENTER);

        title.add(lbl_title);

        return title;


    }

    public Component getPanelTitleHepaticClearance(){
        JPanel title = new JPanel(new GridLayout(1, 4, 0, 3));
        JLabel lb_title = new JLabel("Hepatic clearance\n");
        JLabel lb_t1 = new JLabel("T1");
        JLabel lb_t2 = new JLabel("T2");
        JLabel lb_auc = new JLabel("AUC");

        lb_title.setFont(new Font("Arial", Font.BOLD, 18));
        lb_t1.setFont(new Font("Arial", Font.BOLD, 18));
        lb_t1.setHorizontalAlignment(SwingConstants.CENTER);

        lb_t2.setFont(new Font("Arial", Font.BOLD, 18));
        lb_t2.setHorizontalAlignment(SwingConstants.CENTER);

        lb_auc.setFont(new Font("Arial", Font.BOLD, 18));
        lb_auc.setHorizontalAlignment(SwingConstants.CENTER);

        title.add(lb_title);
        title.add(lb_t1);
        title.add(lb_t2);
        title.add(lb_auc);


        return title;

    }

    public Component getPanelLiver(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Liver");

        // panel de timing
        double liver_t1 = Library_Quantif.round(results.get("t1"), 2);
        double liver_t2 = Library_Quantif.round(results.get("t2"), 2);


        JPanel pnl_liver = new JPanel(new GridLayout(1, 4, 0, 3));
        JLabel lbl_liver = new JLabel("Liver");
        lbl_liver.setFont(new Font("Arial", Font.BOLD,15));
        pnl_liver.add(lbl_liver);


        JLabel val_t1 = new JLabel(liver_t1 +" counts/min");
        val_t1.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_liver.add(val_t1);


        JLabel val_t2 = new JLabel(liver_t2 +" counts/min");
        val_t2.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_liver.add(val_t2);

        JLabel nothing = new JLabel(" ");
        pnl_liver.add(nothing);

        return pnl_liver;


    }


    public Component getPanelHeart(){
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Heart");

        // panel de timing
        double heart_t1 = Library_Quantif.round(results.get("t1"), 2);
        double heart_t2 = Library_Quantif.round(results.get("t2"), 2);
        double heart_AUC = Library_Quantif.round(results.get("AUC"), 2);

        JPanel pnl_heart = new JPanel(new GridLayout(1, 4, 0, 3));

        JLabel lbl_heart = new JLabel("Heart");
        lbl_heart.setFont(new Font("Arial", Font.BOLD,15));
        pnl_heart.add(lbl_heart);

        JLabel val_t1 = new JLabel(heart_t1 +" counts/min");
        val_t1.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_t1);

        JLabel val_t2 = new JLabel(heart_t2 +" counts/min");
        val_t2.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_t2);

        JLabel val_auc = new JLabel(heart_AUC +" counts");
        val_auc.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_heart.add(val_auc);

        return pnl_heart;
    }

    public Component getPanelFov(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("FOV");

        // panel de timing
        double fov_t1 = Library_Quantif.round(results.get("t1"), 2);
        double fov_t2 = Library_Quantif.round(results.get("t2"), 2);

        JPanel pnl_fov = new JPanel(new GridLayout(1, 4,0,3));

        JLabel lbl_fov = new JLabel("FOV");
        lbl_fov.setFont(new Font("Arial", Font.BOLD,15));
        pnl_fov.add(lbl_fov);

        JLabel val_t1 = new JLabel(fov_t1 +" counts/min");
        val_t1.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_fov.add(val_t1);

        JLabel val_t2 = new JLabel(fov_t2 +" counts/min");
        val_t2.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_fov.add(val_t2);

        JLabel nothing = new JLabel(" ");
        pnl_fov.add(nothing);

        return pnl_fov;


    }

    public Component getTitlePanelFFR(){
        JPanel title = new JPanel(new GridLayout(1, 2, 0, 3));
        JLabel lb_title = new JLabel("Share of FRL\n");
        JLabel lb_ffr = new JLabel("FRL/FL (%)");


        lb_title.setFont(new Font("Arial", Font.BOLD, 18));
        lb_ffr.setFont(new Font("Arial", Font.BOLD, 18));
        lb_ffr.setHorizontalAlignment(SwingConstants.CENTER);

        title.add(lb_title);
        title.add(lb_ffr);
        return title;

    }

    public Component getPanelFFR(){
        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();

        Map<String, Double> results = model.getResults().get("Intermediate values");
        JPanel pnl_ffr = new JPanel(new GridLayout(1, 2,0,3));

        double fl_frl = Library_Quantif.round(results.get("FFR/FT") * 100,2);

        JLabel lbl_ffr = new JLabel("Tomoscintigraphy");
        lbl_ffr.setFont(new Font("Arial", Font.BOLD,15));
        pnl_ffr.add(lbl_ffr);

        JLabel val_tomo = new JLabel(fl_frl +" %");
        val_tomo.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_ffr.add(val_tomo);

        return pnl_ffr;


    }

    public Component getIntermediateValuesCnormT2(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        // panel de timing
        double cnorm = Library_Quantif.round(results.get("Cnorm_t2"),2);


        JPanel pnl_intermValues = new JPanel(new GridLayout(1, 4,0,3));

        JLabel lbl_cnorm = new JLabel("Cnorm");
        lbl_cnorm.setFont(new Font("Arial", Font.BOLD,15));
        pnl_intermValues.add(lbl_cnorm);

        JLabel val_cnorm = new JLabel(String.valueOf(cnorm));
        val_cnorm.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_intermValues.add(val_cnorm);


        JLabel nothing = new JLabel(" ");
        pnl_intermValues.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_intermValues.add(nothing2);

        return pnl_intermValues;



    }

    public Component getIntermediateValuesAUCCnorm(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        double auc_cnorm = Library_Quantif.round(results.get("AUC/Cnorm"),2);
        JPanel pnl_intermValues = new JPanel(new GridLayout(1, 4,0,3));


        JLabel lbl_auccnorm = new JLabel("AUC/Cnorm");
        lbl_auccnorm.setFont(new Font("Arial", Font.BOLD,15));
        pnl_intermValues.add(lbl_auccnorm);

        JLabel val_auccnorm = new JLabel(String.valueOf(auc_cnorm));
        val_auccnorm.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_intermValues.add(val_auccnorm);

        JLabel nothing = new JLabel(" ");
        pnl_intermValues.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_intermValues.add(nothing2);


        return pnl_intermValues;


    }

    public Component getIntermediateValuesBPActivity(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        double bp = Library_Quantif.round(results.get("BP Activity"),2);

        JPanel pnl_intermValues = new JPanel(new GridLayout(1, 4,0,3));


        JLabel lbl_bp = new JLabel("BP Activity");
        lbl_bp.setFont(new Font("Arial", Font.BOLD,15));
        pnl_intermValues.add(lbl_bp);

        JLabel val_bp = new JLabel(String.valueOf(bp));
        val_bp.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_intermValues.add(val_bp);

        JLabel nothing = new JLabel(" ");
        pnl_intermValues.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_intermValues.add(nothing2);

        return pnl_intermValues;

    }


    public Component getClearancenonNorm(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        double nonNormFT = Library_Quantif.round(results.get("Clairance FT"),2);

        JPanel pnl_results = new JPanel(new GridLayout(1, 4,0,3));


        JLabel lbl_bp = new JLabel("Non-standardized Clearance FT ");
        lbl_bp.setFont(new Font("Arial", Font.BOLD,15));
        pnl_results.add(lbl_bp);

        JLabel val_bp = new JLabel(nonNormFT +" %/min");
        val_bp.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_results.add(val_bp);

        JLabel nothing = new JLabel(" ");
        pnl_results.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_results.add(nothing2);

        return pnl_results;

    }
    public Component getClearanceNorm(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        double normFT = Library_Quantif.round(results.get("Norm Clairance FT"),2);

        JPanel pnl_results = new JPanel(new GridLayout(1, 4,0,3));


        JLabel lbl_bp = new JLabel("Normalized Clearance FT ");
        lbl_bp.setFont(new Font("Arial", Font.BOLD,15));
        pnl_results.add(lbl_bp);

        JLabel val_bp = new JLabel(String.valueOf(normFT));
        val_bp.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_results.add(val_bp);

        JLabel nothing = new JLabel(" ");
        pnl_results.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_results.add(nothing2);

        return pnl_results;

    }

    public Component getRetention(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");

        double normFT = Library_Quantif.round(results.get("Retention rate")*100,2);

        JPanel pnl_results = new JPanel(new GridLayout(1, 4,0,3));


        JLabel lbl_bp = new JLabel("Retention ");
        lbl_bp.setFont(new Font("Arial", Font.BOLD,15));
        pnl_results.add(lbl_bp);

        JLabel val_bp = new JLabel(normFT+ " %");
        val_bp.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_results.add(val_bp);

        JLabel nothing = new JLabel(" ");
        pnl_results.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_results.add(nothing2);

        return pnl_results;

    }

    public Component getFFRnonNorm(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");
        JPanel pnl_results = new JPanel(new GridLayout(1, 4,0,3));

        double nonnormFFR = Library_Quantif.round(results.get("Clairance FFR"),2);


        JLabel lbl_frl = new JLabel("Clearance FRL ");
        lbl_frl.setFont(new Font("Arial", Font.BOLD,15));
        pnl_results.add(lbl_frl);

        JLabel val_frl = new JLabel(nonnormFFR +" %/min");
        val_frl.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_results.add(val_frl);

        JLabel nothing = new JLabel(" ");
        pnl_results.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_results.add(nothing2);

        return pnl_results;

    }

    public Component getFFRNorm(){

        Model_Scintivol model = (Model_Scintivol) this.parent.getModel();
        Map<String, Double> results = model.getResults().get("Intermediate values");
        JPanel pnl_results = new JPanel(new GridLayout(1, 4,0,3));


        double normFFR = Library_Quantif.round(results.get("Norm Clairance FFR"),2);



        JLabel lbl_frl = new JLabel("Normalized Clearance FRL ");
        lbl_frl.setFont(new Font("Arial", Font.BOLD,15));
        pnl_results.add(lbl_frl);

        JLabel val_frl = new JLabel(String.valueOf(normFFR));
        val_frl.setHorizontalAlignment(SwingConstants.CENTER);
        pnl_results.add(val_frl);

        JLabel nothing = new JLabel(" ");
        pnl_results.add(nothing);

        JLabel nothing2 = new JLabel(" ");
        pnl_results.add(nothing2);

        return pnl_results;

    }
}
