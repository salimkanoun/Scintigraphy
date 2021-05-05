package org.petctviewer.scintigraphy.salivaryGlands.gui;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.petctviewer.scintigraphy.salivaryGlands.ModelSalivaryGlands;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.gui.TabContrastModifier;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultats_SalivaryGlands extends FenResults {
    private static final long serialVersionUID = 1L;

    public FenResultats_SalivaryGlands(BufferedImage capture, ControllerScin controller){
        super(controller);
        this.addTab(new TabMain(capture, this));

        ModelSalivaryGlands model = (ModelSalivaryGlands) controller.getModel();
        ImagePlus montage = Library_Capture_CSV.creerMontage(model.getFrameDurations(), model.getImpAnt().getImagePlus(), 200, 4, 4);
        montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);
        this.addTab(new TabContrastModifier(this, "Timed Image", montage));



        this.setTitle("Results Salivary Glands Exam");
        int height = 800;
        int width = 1000;
        this.setPreferredSize(new Dimension(width, height));
        this.setLocationRelativeTo(controller.getVue());

    }
}
