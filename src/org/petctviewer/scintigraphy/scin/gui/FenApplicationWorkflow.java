package org.petctviewer.scintigraphy.scin.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;

import org.petctviewer.scintigraphy.gastric.InstructionTooltip;
import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.ImageSelection;

import ij.IJ;

public class FenApplicationWorkflow extends FenApplication implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = -6280620624574294247L;

	private Scrollbar scroll;
	private InstructionTooltip tooltip;

	public enum UI_Element {
		BUTTON_NEXT, BUTTON_PREVIOUS, BUTTON_DRAW_ROI, BUTTON_QUIT, BUTTON_CONTRAST;
	}

	private ImageSelection imageSelection;

	public FenApplicationWorkflow(ImageSelection ims, String nom) {
		super(ims.getImagePlus(), nom);
		this.imageSelection = ims;

		this.setResizable(true);

		this.scroll = new Scrollbar(Scrollbar.HORIZONTAL);
		this.panelContainer.add(scroll, BorderLayout.NORTH);

		this.addMouseWheelListener(this);

		// Enable visualisation by default
		this.setVisualisationEnable(true);
	}
	
	private void addControllerListeners() {
		if (this.getControleur() != null) {
			ControllerWorkflow controller = (ControllerWorkflow) this.getControleur();
			this.scroll.addAdjustmentListener(controller);
			this.addMouseWheelListener(controller);
		}
	}

	public void setVisualisationEnable(boolean state) {
		if (state) {
			// Create tooltip
			this.tooltip = new InstructionTooltip(this);
			this.scroll.setVisible(true);

			// Add listeners
			this.addControllerListeners();
			this.scroll.addMouseMotionListener(this);
			this.scroll.addMouseListener(this);

			// Remove listeners
			this.removeMouseWheelListener(this);

			// Remove slider of fiji
			this.getComponents()[1].setVisible(false);
			
		} else {
			
			// Destroy tooltip
			this.tooltip = null;
			this.scroll.setVisible(false);

			// Remove listeners
			if (this.getControleur() != null) {
				ControllerWorkflow controller = (ControllerWorkflow) this.getControleur();
				this.scroll.removeAdjustmentListener(controller);
				this.removeMouseWheelListener(controller);
			}
			this.scroll.removeMouseListener(this);
			this.scroll.removeMouseMotionListener(this);

			// Remove listeners
			this.removeMouseWheelListener(this);

			// Replace slider of fiji
			this.getComponents()[1].setVisible(true);
		}
	}

	public void setNbInstructions(int nbInstructions) {
		this.scroll.setBlockIncrement(1);
		this.scroll.setUnitIncrement(1);
		this.scroll.setValues(1, 1, 1, nbInstructions);
		System.out.println("Nb instructions: " + nbInstructions);
	}

	public int getInstructionDisplayed() {
		return this.scroll.getValue();
	}

	public void currentInstruction(int value) {
		this.scroll.setValue(value);
	}
	
	public int getMaxInstruction() {
		return this.scroll.getMaximum();
	}

	public void displayScrollToolTip(String message, Color color) {
		// Change message
		this.tooltip.setText(message);
		this.tooltip.setBackground(color);
		this.tooltip.pack();
	}

	public void setMessageInstruction(String message) {
		textfield_instructions.setText(message);
		this.pack();
	}

	public void setEnableNext(boolean state) {
		this.btn_suivant.setEnabled(state);
	}

	public void setEnablePrevious(boolean state) {
		this.btn_precedent.setEnabled(state);
	}

	public ImageSelection getImage() {
		return this.imageSelection;
	}

	public void setImage(ImageSelection image) {
		this.imageSelection = image;
		this.setImage(image.getImagePlus());
	}

	public boolean isButtonNext(Button btn) {
		return this.matchComponent(btn, UI_Element.BUTTON_NEXT);
	}

	public boolean isButtonPrevious(Button btn) {
		return this.matchComponent(btn, UI_Element.BUTTON_PREVIOUS);
	}

	public boolean matchComponent(Component component, UI_Element element) {
		Button btn = this.getButton(element);
		if (btn != null)
			return btn == component;
		return false;
	}

	public Button getButton(UI_Element element) {
		switch (element) {
		case BUTTON_CONTRAST:
			return this.btn_contrast;
		case BUTTON_DRAW_ROI:
			return this.btn_drawROI;
		case BUTTON_NEXT:
			return this.btn_suivant;
		case BUTTON_PREVIOUS:
			return this.btn_precedent;
		case BUTTON_QUIT:
			return this.btn_quitter;
		default:
			return null;
		}
	}

	public void setControleur(ControllerWorkflow controller) {
		super.setControleur(controller);
		this.addControllerListeners();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// Size of the tooltip
		Rectangle bounds = this.tooltip.getBounds();

		// Place tooltip
		Point location = MouseInfo.getPointerInfo().getLocation();
		location.x -= bounds.width / 2;
		location.y -= bounds.height + 10;
		this.tooltip.setLocation(location);
		this.tooltip.setVisible(true);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.tooltip.setVisible(true);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.tooltip.setVisible(false);
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int rotation = e.getWheelRotation();
		boolean ctrl = (e.getModifiers() & Event.CTRL_MASK) != 0;
		if ((ctrl || IJ.shiftKeyDown()) && ic != null) {
			Point loc = ic.getCursorLoc();
			int x = ic.screenX(loc.x);
			int y = ic.screenY(loc.y);
			if (rotation < 0)
				ic.zoomIn(x, y);
			else
				ic.zoomOut(x, y);
			return;
		}
	}

}
