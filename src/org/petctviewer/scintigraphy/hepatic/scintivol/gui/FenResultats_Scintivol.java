package org.petctviewer.scintigraphy.hepatic.scintivol.gui;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenResults;

import java.awt.*;
import java.util.List;

public class FenResultats_Scintivol extends FenResults {

    private static final long serialVersionUID = 1L;

    public FenResultats_Scintivol(List<ImagePlus> captures, ControllerScin controller){
        super(controller);
        this.addTab(new TabPrecoce(captures.get(0).getBufferedImage(), this));
        this.addTab((new TabTardive(captures.get(1).getBufferedImage(), this)));
        this.addTab(new TabTomo(this, "TomoScintigraphy results"));
        this.addTab(new TabSynthese(this));

        this.getTabPane().addChangeListener(l -> {
            this.reloadAllTabs();
        });



        this.setTitle("Results Scintivol scintigraphy Exam");
        int height = 800;
        int width = 1000;
        this.setPreferredSize(new Dimension(width, height));
        this.setLocationRelativeTo(controller.getVue());
    }
}
