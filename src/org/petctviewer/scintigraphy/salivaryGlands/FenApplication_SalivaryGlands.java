package org.petctviewer.scintigraphy.salivaryGlands;

import ij.ImagePlus;
import ij.gui.Overlay;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.awt.event.ActionEvent;

public class FenApplication_SalivaryGlands extends FenApplicationWorkflow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final SalivaryGlandsScintigraphy main;
	private boolean dyn;
	private final ImagePlus impProj;
	private final Button btn_dyn;
	private final Button btn_start;

	public FenApplication_SalivaryGlands(ImageSelection ims, String nom, SalivaryGlandsScintigraphy main) {
		super(ims, nom);
		// Keep default visualisation
		this.setVisualizationEnable(false);
		
		this.main = main;
		

		Overlay overlay = Library_Gui.initOverlay(this.getImagePlus(), 12);
		this.getImagePlus().setOverlay(overlay);
		Library_Gui.setOverlayGD(this.getImagePlus(), Color.YELLOW);
		Library_Gui.setOverlayTitle("Ant", this.getImagePlus(), Color.yellow, 1);
		Library_Gui.setOverlayTitle("2 first min posterior", this.getImagePlus(), Color.YELLOW, 2);
		Library_Gui.setOverlayTitle("MIP", this.getImagePlus(), Color.YELLOW, 3);
		
		
		// Ajout du boutton dynamic au panel de gauche
		btn_dyn = new Button("Dynamic");
		btn_dyn.addActionListener(this);

//		this.getPanel_btns_gauche().setLayout(new GridLayout(1, 4));
		this.getPanel_btns_gauche().add(btn_dyn);

		btn_start = new Button("Start");
		btn_start.addActionListener(this);

		this.getPanel_bttns_droit().removeAll();
		this.getPanel_bttns_droit().add(btn_start);

		this.getBtn_drawROI().setEnabled(false);

		this.setDefaultSize();

		
		this.impProj = imp;
		
		this.pack();

	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.setText_instructions("Click to start the exam");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void actionPerformed(ActionEvent e) {
		// clic sur le bouton dynamique
		if (e.getSource() == btn_dyn) {

			ImagePlus imp;

			if (!this.dyn) {

				imp = main.getImpAnt().getImagePlus();
				Library_Gui.setCustomLut(imp);
				this.getImagePlus().getOverlay().clear();
				Overlay overlay = Library_Gui.initOverlay(imp);
				imp.setOverlay(overlay);
				Library_Gui.setOverlayGD(imp);
				revalidate();
				setImage(imp);
				resizeCanvas();
				updateSliceSelector();
				Library_Gui.setOverlayGD(imp, Color.YELLOW);
				this.btn_dyn.setBackground(Color.LIGHT_GRAY);

			} else {
				imp = this.impProj;
				Library_Gui.setCustomLut(imp);
				this.btn_dyn.setBackground(null);
				
				Library_Gui.setOverlayGD(imp, Color.YELLOW);
				Library_Gui.setOverlayTitle("Post", imp, Color.yellow, 1);
				Library_Gui.setOverlayTitle("2 first min posterior", imp, Color.YELLOW, 2);
				Library_Gui.setOverlayTitle("MIP", imp, Color.YELLOW, 3);
				if (this.main.getImpAnt() != null) {
					Library_Gui.setOverlayTitle("Ant", imp, Color.yellow, 4);
				}

				revalidate();
				setImage(imp);
				updateSliceSelector();
				resizeCanvas();

			}

			/*
			 * //si l'imp est null, on utilise l'image ant ou post if(imp == null) {
			 * if(vue.getImpPost() != null) { imp = vue.getImpPost(); }else
			 * if(vue.getImpAnt() != null) { imp = vue.getImpAnt(); } }
			 */

			// on inverse le boolean pour l'utilisation suivante
			this.dyn = !this.dyn;

			// Mode debut du programme apres visualisation.
		} else if (e.getSource() == btn_start) {	
			btn_dyn.setEnabled(false);
			
			// TODO move elsewhere

			this.getBtn_contrast().setEnabled(true);

			this.getPanel_bttns_droit().removeAll();
			this.getPanel_bttns_droit().add(this.createPanelInstructionsBtns());
			this.getBtn_drawROI().setEnabled(true);
			this.setImage(impProj);
			this.updateSliceSelector();
			resizeCanvas();
		}

	}
}