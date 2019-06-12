package org.petctviewer.scintigraphy.cardiac;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.gui.Toolbar;

public class FenApplication_Cardiac extends FenApplicationWorkflow {

	private static final long serialVersionUID = -8986173550839545500L;

	// boutons mode decontamination
	private final Button btn_newCont;
	private final Button btn_continue;

	public FenApplication_Cardiac(ImageSelection ims, String nom, boolean fullBodyImages, boolean onlyThoraxImage) {
		super(ims, nom);
	
		this.btn_continue = new Button("End");
		this.btn_newCont = new Button("Next");

		if(fullBodyImages) {
			this.getPanel_Instructions_btns_droite().remove(1);
	
			// mise en place des boutons
			Panel btns_instru = new Panel();
			btns_instru.setLayout(new GridLayout(1, 3));
			btns_instru.add(this.getBtn_precedent());
			btns_instru.add(this.getBtn_suivant());
			btns_instru.add(this.btn_continue);
			this.getPanel_Instructions_btns_droite().add(btns_instru);
		}
		
		this.getTextfield_instructions().setPreferredSize(new Dimension(200,this.getTextfield_instructions().getHeight()));

		this.setPreferredCanvasSize(600);
		this.setLocationRelativeTo(null);
		IJ.setTool(Toolbar.POLYGON);

		this.pack();
	}

	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilis�s
	 * pour d�limiter les rois
	 */
	public void stopContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);
		this.getPanel_Instructions_btns_droite().add(this.createPanelInstructionsBtns());
		IJ.setTool(Toolbar.POLYGON);
		this.pack();
	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
		this.setText_instructions("Delimit a new contamination");
	}

	public Button getBtn_newCont() {
		return this.btn_newCont;
	}

	public Button getBtn_continue() {
		return this.btn_continue;
	}
	
	public void setMultipleTitle(Color color, int slice) {
		Overlay overlay = this.getImagePlus().getOverlay();
		
		int w = this.getImagePlus().getWidth();
	
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
	
		Rectangle2D bounds = overlay.getLabelFont().getStringBounds("Ant", frc);
		//double textHeight = bounds.getHeight();
		double textWidth = bounds.getWidth();
		
		TextRoi Ant = Library_Gui.createTextRoi("Ant", (w / 4) - (textWidth / 4), 0 , 1, Color.YELLOW, overlay.getLabelFont());
		TextRoi invertedPost = Library_Gui.createTextRoi("Inverted Post", (3*w / 4) - (3*textWidth / 4), 0 , 1, Color.YELLOW, overlay.getLabelFont());
		TextRoi AntSlice2 = Library_Gui.createTextRoi("Ant", (w / 4) - (textWidth / 4), 0 , 2, Color.YELLOW, overlay.getLabelFont());
		TextRoi invertedPostSlice2 = Library_Gui.createTextRoi("Inverted Post", (3*w / 4) - (3*textWidth / 4), 0 , 2, Color.YELLOW, overlay.getLabelFont());
		
		
		overlay.add(Ant);
		overlay.add(invertedPost);
		overlay.add(AntSlice2);
		overlay.add(invertedPostSlice2);
	}
	
	

}
