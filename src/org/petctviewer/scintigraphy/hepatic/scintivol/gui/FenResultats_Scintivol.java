package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultats_Scintivol extends FenResults {

    private static final long serialVersionUID = 1L;

    public FenResultats_Scintivol(BufferedImage capture, ControllerScin controller){
        super(controller);

        Model_Scintivol modele = (Model_Scintivol) controller.getModel();
        this.addTab(new TabPrecoce(capture, this));


        this.setTitle("Results Salivary Glands Exam");
        int height = 800;
        int width = 1000;
        this.setPreferredSize(new Dimension(width, height));
        this.setLocationRelativeTo(controller.getVue());
    }
}
