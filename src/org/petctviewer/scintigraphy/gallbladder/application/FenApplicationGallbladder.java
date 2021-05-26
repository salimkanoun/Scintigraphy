package org.petctviewer.scintigraphy.gallbladder.application;

import ij.gui.Overlay;
import ij.util.DicomTools;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.awt.event.ActionEvent;

public class FenApplicationGallbladder extends FenApplicationWorkflow {

    private static final long serialVersionUID = 1L;
    private final GallbladderScintigraphy main;
    private final Button btn_start;

    public FenApplicationGallbladder(ImageSelection ims, String nom, GallbladderScintigraphy main) {
        super(ims, nom);
        // Keep default visualisation
        this.setVisualizationEnable(false);

        this.main = main;



        Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
        this.getImagePlus().setOverlay(overlay);
        Library_Gui.setOverlayDG(this.getImagePlus(), Color.YELLOW);

        btn_start = new Button("Start");
        btn_start.addActionListener(this);

        this.getPanel_bttns_droit().removeAll();
        this.getPanel_bttns_droit().add(btn_start);

        this.getBtn_drawROI().setEnabled(false);
        this.getBtn_reverse().setEnabled(false);


        this.setDefaultSize();

        this.pack();

    }

    @Override
    public void setController(ControllerScin ctrl) {
        super.setController(ctrl);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.btn_start) {
            ModelGallbladder model = (ModelGallbladder) this.getController().getModel();


            this.getBtn_reverse().setEnabled(true);
            this.getPanel_bttns_droit().removeAll();
            this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
            this.getBtn_drawROI().setEnabled(true);
            this.setAlwaysOnTop(false);
            this.setImage(this.imp);
            this.updateSliceSelector();
            resizeCanvas();
        }

        super.actionPerformed(e);
    }
}

