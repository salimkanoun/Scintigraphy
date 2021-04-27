package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.petctviewer.scintigraphy.salivaryGlands.gui.TabMain;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FenResultats_SalivaryGlands extends FenResults {
    private static final long serialVersionUID = 1L;

    public FenResultats_SalivaryGlands(BufferedImage capture, ControllerScin controller){
        super(controller);
        this.addTab(new TabMain(capture, this));

        ModelSalivaryGlands model = (ModelSalivaryGlands) controller.getModel();
        ImagePlus montage = Library_Capture_CSV.creerMontage(model.getFrameDurations(), model.getImpAnt().getImagePlus(), 200, 4, 5);
        montage.getProcessor().setInterpolationMethod(ImageProcessor.BICUBIC);



        this.setTitle("Results Salivary Glands Exam");
        int height = 800;
        int width = 1000;
        this.setPreferredSize(new Dimension(width, height));
        this.setLocationRelativeTo(controller.getVue());

    }
}
