package org.petctviewer.scintigraphy.scin.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ScrollbarWithLabel;
import org.petctviewer.scintigraphy.gastric.InstructionTooltip;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;

/**
 * This class is the main window of the program and handles all of the inputs
 * from the user. It extends the FenApplication.<br>
 * This class adds a scrollbar under the image to navigate through all of the
 * visible instructions of the workflow.<br>
 * If you do not want to use this functionality but still need to use a
 * workflow, then you can disable it by calling
 * {@link #setVisualizationEnable(boolean)} and setting it to FALSE.<br>
 * If you want this functionality, then you need to call the
 * {@link #setNbInstructions(int)} method to correctly initialize the scrollbar.
 *
 * @author Titouan QUÉMA
 */
public class FenApplicationWorkflow extends FenApplication implements MouseMotionListener, MouseListener {
	public static final String BTN_TXT_RESUME = "Resume";
	private static final long serialVersionUID = -6280620624574294247L;
	private Scrollbar scroll;
	private InstructionTooltip tooltip;
	private ImageSelection imageSelection;

	/**
	 * Instantiates a new application window. This constructor enables by default
	 * the scrollbar visualization of the workflow.
	 *
	 * @param ims       Image displayed in this application
	 * @param studyName Name of the study
	 */
	public FenApplicationWorkflow(ImageSelection ims, String studyName) {
		super(ims.getImagePlus(), studyName);
		this.imageSelection = ims;

		this.setResizable(true);

		// Scrollbar
		this.scroll = new Scrollbar(Scrollbar.HORIZONTAL);
		this.scroll.setBlockIncrement(1);
		this.scroll.setUnitIncrement(1);
		this.scroll.setValues(1, 1, 1, 1);
		this.panelContainer.add(scroll, BorderLayout.NORTH);

		this.addMouseWheelListener(this);

		// Enable visualization by default
		this.setVisualizationEnable(true);
	}

	private void addControllerListeners() {
		if (this.getControleur() != null) {
			ControllerWorkflow controller = (ControllerWorkflow) this.getControleur();
			this.scroll.addAdjustmentListener(controller);
			this.addMouseWheelListener(controller);
		}
	}

	/**
	 * Enables or disables the visualization functionality.
	 *
	 * @param state if TRUE the scrollbar for the visualization is displayed and if
	 *              set to FALSE, then the scrollbar displayed is the one of IJ
	 */
	public void setVisualizationEnable(boolean state) {
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
			if (this.getComponents()[1] instanceof ScrollbarWithLabel)
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
			if (this.getComponents()[1] instanceof ScrollbarWithLabel)
				this.getComponents()[1].setVisible(true);
		}
	}

	/**
	 * @return TRUE if the visualization scroll bar is enabled and FALSE if the default IJ scroll is left
	 */
	public boolean isVisualizationEnabled() {
		return this.tooltip != null;
	}

	/**
	 * Sets the number of instructions the scrollbar can go through.
	 *
	 * @param nbInstructions Number of instructions
	 */
	public void setNbInstructions(int nbInstructions) {
		this.scroll.setValues(1, 1, 1, nbInstructions);
	}

	/**
	 * Gets the current instruction selected by the scrollbar.
	 *
	 * @return index of the instruction
	 */
	public int getInstructionDisplayed() {
		return this.scroll.getValue();
	}

	/**
	 * Sets the current instruction selected by the scrollbar. The value should be
	 * less or equals than the maximum value.
	 *
	 * @param value Index of the instruction
	 */
	public void currentInstruction(int value) {
		this.scroll.setValue(value);
	}

	/**
	 * Gets the maximum of instructions displayed by the scrollbar.
	 *
	 * @return maximum value
	 */
	public int getMaxInstruction() {
		return this.scroll.getMaximum();
	}

	/**
	 * Actualize the message and color of the tool tip displayed when moving the
	 * scroll.
	 */
	public void displayScrollToolTip(String message, Color color) {
		// Change message
		this.tooltip.setText(message);
		this.tooltip.setBackground(color);
		this.tooltip.pack();
	}

	/**
	 * Changes the current instruction message for the user.
	 *
	 * @param message Message for the user
	 */
	public void setMessageInstruction(String message) {
		textfield_instructions.setText(message);
		this.pack();
	}

	/**
	 * Enables or disables the 'Next' button.
	 *
	 * @param state if TRUE the 'Next' button can be clicked on and if set to FALSE,
	 *              then the button cannot be clicked
	 */
	public void setEnableNext(boolean state) {
		this.btn_suivant.setEnabled(state);
	}

	/**
	 * Enables or disables the 'Previous' button.
	 *
	 * @param state if TRUE the 'Previous' button can be clicked on and if set to
	 *              FALSE, then the button cannot be clicked
	 */
	public void setEnablePrevious(boolean state) {
		this.btn_precedent.setEnabled(state);
	}

	/**
	 * Gets the image displayed by this view.
	 *
	 * @return image currently displayed
	 */
	public ImageSelection getImage() {
		return this.imageSelection;
	}

	/**
	 * Sets the current image displayed by this view.
	 *
	 * @param image New image to display
	 */
	public void setImage(ImageSelection image) {
		this.imageSelection = image;
		this.setImage(image.getImagePlus());
	}

	/**
	 * @deprecated Please use {@link #setImage(ImageSelection)} instead
	 */
	@Deprecated
	@Override
	public void setImage(ImagePlus imp) {
		super.setImage(imp);
	}

	/**
	 * Checks if the specified button is the 'Next' button of this view.
	 *
	 * @param btn Button to check
	 * @return TRUE if this is the 'Next' button and FALSE otherwise
	 */
	public boolean isButtonNext(Button btn) {
		return this.matchComponent(btn, UI_Element.BUTTON_NEXT);
	}

	/**
	 * Checks if the specified button is the 'Previous' button of this view.
	 *
	 * @param btn Button to check
	 * @return TRUE if this is the 'Previous' button and FALSE otherwise
	 */
	public boolean isButtonPrevious(Button btn) {
		return this.matchComponent(btn, UI_Element.BUTTON_PREVIOUS);
	}

	/**
	 * Checks if the specified component matches the specified element of this view.
	 *
	 * @param component Component to check
	 * @param element   Element the component should be equal
	 * @return TRUE if the specified component is the element in this view and FALSE
	 * otherwise
	 */
	private boolean matchComponent(Component component, UI_Element element) {
		Button btn = this.getButton(element);
		if (btn != null)
			return btn == component;
		return false;
	}

	/**
	 * Gets the button of this view represented by the specified element.<br>
	 * If the element is not a button, then null is returned.
	 *
	 * @param element Button to get
	 * @return button represented by the element or null if element is not a button
	 */
	private Button getButton(UI_Element element) {
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

	@Override
	public void setController(ControllerScin controller) {
		if (!(controller instanceof ControllerWorkflow)) {
			throw new IllegalArgumentException("The controller must be an instance of ControllerWorkflow");
		}

		super.setController(controller);
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
		// This code comes from IJ, it was paste here to keep the zoom functionality
		// available
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
		}
	}

	public enum UI_Element {
		BUTTON_NEXT, BUTTON_PREVIOUS, BUTTON_DRAW_ROI, BUTTON_QUIT, BUTTON_CONTRAST
	}

}
