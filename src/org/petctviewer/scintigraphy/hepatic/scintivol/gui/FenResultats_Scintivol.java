package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.petctviewer.scintigraphy.hepatic.scintivol.Model_Scintivol;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultats_Scintivol extends FenResults {

    private static final long serialVersionUID = 1L;

    public FenResultats_Scintivol(BufferedImage capture, ControllerScin controller){
        super(controller);
        this.addTab(new TabPrecoce(capture, this));
        this.addTab(new TabTomo(this, "TomoScintigraphy results"));
        Model_Scintivol model = (Model_Scintivol) controller.getModel();



        this.setTitle("Results Scintivol scintigraphy Exam");
        int height = 800;
        int width = 1000;
        this.setPreferredSize(new Dimension(width, height));
        this.setLocationRelativeTo(controller.getVue());
    }
}
