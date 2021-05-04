package org.petctviewer.scintigraphy.hepatic.scintivol;

import ij.ImagePlus;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;

public class FenApplication_Scintivol extends FenApplicationWorkflow {


    private static final long serialVersionUID = 1L;
    private final ScintivolScintigraphy main;
    private boolean dyn;
    private final ImagePlus impProj;
    private final ImageSelection imsProj;

    public FenApplication_Scintivol(ImageSelection ims, String nom, ScintivolScintigraphy main) {
        super(ims, nom);
        // Keep default visualisation
        this.setVisualizationEnable(false);

        this.main = main;
        this.imsProj = ims;


        Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
        this.getImagePlus().setOverlay(overlay);
        Library_Gui.setOverlayDG(this.getImagePlus(), Color.YELLOW);
        // Library_Gui.setOverlayTitle("Ant", this.getImagePlus(), Color.yellow, 1);
        // Library_Gui.setOverlayTitle("Ant", this.getImagePlus(), Color.yellow, 2);
        // Library_Gui.setOverlayTitle("Ant", this.getImagePlus(), Color.yellow, 3);

//		this.getPanel_btns_gauche().setLayout(new GridLayout(1, 4));

        this.getBtn_drawROI().setEnabled(false);

        this.setDefaultSize();


        this.impProj = imp;

        this.pack();

    }

    @Override
    public void setController(ControllerScin ctrl) {
        super.setController(ctrl);
    }

}
