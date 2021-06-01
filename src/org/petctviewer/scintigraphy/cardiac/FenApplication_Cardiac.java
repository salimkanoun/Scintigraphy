package org.petctviewer.scintigraphy.cardiac;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.gui.FenApplicationWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class FenApplication_Cardiac extends FenApplicationWorkflow {

	private static final long serialVersionUID = -8986173550839545500L;

	// boutons mode decontamination
	private final Button btn_newCont;
	private final Button btn_continue;

	public FenApplication_Cardiac(ImageSelection ims, String nom, boolean fullBodyImages, boolean onlyThoraxImage) {
		super(ims, nom);
		Library_Gui.initOverlay(ims.getImagePlus());
	
		this.btn_continue = new Button("End");
		this.btn_newCont = new Button("Next");


		this.getTextfield_instructions().setPreferredSize(
				new Dimension(200, this.getTextfield_instructions().getHeight()));

		this.setPreferredCanvasSize(600);
		this.setLocationRelativeTo(null);

		this.pack();
	}

	/**
	 * Remplace les boutons permettant la decontamination par les boutons utilis�s
	 * pour d�limiter les rois
	 */
	public void stopContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);
		this.getPanel_Instructions_btns_droite().add(this.createPanelInstructionsBtns());
		this.pack();
	}
	
	public void startContaminationMode() {
		this.getPanel_Instructions_btns_droite().remove(1);

		// mise en place des boutons
		Panel btns_instru = new Panel();
		btns_instru.setLayout(new GridLayout(1, 3));
		btns_instru.add(this.getBtn_precedent());
		btns_instru.add(this.getBtn_suivant());
		btns_instru.add(this.btn_continue);
		this.btn_continue.setEnabled(false);
		this.getPanel_Instructions_btns_droite().add(btns_instru);
		this.pack();
	}

	@Override
	public void setController(ControllerScin ctrl) {
		super.setController(ctrl);
		this.btn_continue.addActionListener(ctrl);
		this.btn_newCont.addActionListener(ctrl);
		if (((ControllerWorkflowCardiac) this.getController()).getFullBodyImagesCount() > 0) this.setText_instructions(
				"Delimit a new contamination");
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

		TextRoi Ant = Library_Gui.createTextRoi("Ant", (w / 4.0) - (textWidth / 4), 0 , 1, Color.YELLOW, overlay.getLabelFont());
		double xPosition = (3.0 * w / 4) - (3 * textWidth / 4);
		TextRoi invertedPost = Library_Gui.createTextRoi("Post", xPosition, 0 , 1, Color.YELLOW, overlay.getLabelFont());
		TextRoi AntSlice2 = Library_Gui.createTextRoi("Ant", (w / 4.0) - (textWidth / 4), 0 , 2, Color.YELLOW, overlay.getLabelFont());
		TextRoi invertedPostSlice2 = Library_Gui.createTextRoi("Post", xPosition, 0 , 2, Color.YELLOW, overlay.getLabelFont());


		overlay.add(Ant);
		overlay.add(invertedPost);
		overlay.add(AntSlice2);
		overlay.add(invertedPostSlice2);
	}

	public void setMultipleLateralisation(Color color) {
		ImagePlus imp = this.getImagePlus();
		Overlay overlay = imp.getOverlay();

		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
		Rectangle2D bounds = overlay.getLabelFont().getStringBounds("L  L", frc);
		double textWidth = bounds.getWidth();

		double x = (imp.getWidth()/2.) - (textWidth/2.);
		double y = imp.getHeight()/2.;

		TextRoi slice1 = Library_Gui.createTextRoi("L  L", x, y, 1, color, overlay.getLabelFont());
		TextRoi slice2 = Library_Gui.createTextRoi("L  L", x, y, 2, color, overlay.getLabelFont());


		overlay.add(slice1);
		overlay.add(slice2);

		Library_Gui.setOverlaySides(imp, color, "R", "R", 1);
		Library_Gui.setOverlaySides(imp, color, "R", "R", 2);
	}
}