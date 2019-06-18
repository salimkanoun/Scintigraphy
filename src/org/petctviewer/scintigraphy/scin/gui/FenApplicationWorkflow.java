package org.petctviewer.scintigraphy.scin.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.ScrollbarWithLabel;
import org.petctviewer.scintigraphy.gastric.gui.InstructionTooltip;
import org.petctviewer.scintigraphy.scin.ImageSelection;
import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabMain;

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
 * {@link #setVisualizationEnable(boolean)} and setting it to FALSE.
 *
 * @author Titouan QUÉMA
 */
public class FenApplicationWorkflow extends FenApplication implements MouseMotionListener, MouseListener {
	public static final String BTN_TXT_RESUME = "Resume";
	private static final long serialVersionUID = -6280620624574294247L;
	private final Scrollbar scroll;
	private InstructionTooltip tooltip;
	private ImageSelection imageSelection;

	/**
	 * Instantiates a new application window. This constructor enables by default
	 * the scrollbar visualization of the workflow.
	 *
	 * @param ims
	 *            Image displayed in this application
	 * @param studyName
	 *            Name of the study
	 */
	public FenApplicationWorkflow(ImageSelection ims, String studyName) {
		super(ims.getImagePlus(), studyName);
		this.imageSelection = ims;

		// Scrollbar
		this.scroll = new Scrollbar(Scrollbar.HORIZONTAL);
		this.scroll.setBlockIncrement(1);
		this.scroll.setUnitIncrement(1);
		this.scroll.setValues(1, 1, 1, 1);

		// Enable visualization by default
		this.setVisualizationEnable(true);

		this.getLoadRoisMenuItem().setEnabled(true);

		// Prepare overlay
		IJ.setTool(PrefTabMain.toolFromString(Prefs.get(PrefTabMain.PREF_TOOL_ROI, "Polygone")));
		Library_Gui.initOverlay(ims.getImagePlus());
		Library_Gui.setOverlayDG(ims.getImagePlus());
	}

	private void addControllerListeners() {
		if (this.getController() != null) {
			ControllerWorkflow controller = this.getController();
			this.scroll.addAdjustmentListener(controller);
			if (this.isVisualizationEnabled()) this.addMouseWheelListener(controller);
		}
	}

	/**
	 * Enables or disables the visualization functionality.
	 *
	 * @param state
	 *            if TRUE the scrollbar for the visualization is displayed and if
	 *            set to FALSE, then the scrollbar displayed is the one of IJ
	 */
	public void setVisualizationEnable(boolean state) {
		if (state) {
			// Create tooltip
			this.tooltip = new InstructionTooltip(this);
			this.panelContainer.add(scroll, BorderLayout.NORTH);

			// Add listeners
			this.addControllerListeners();
			this.scroll.addMouseMotionListener(this);
			this.scroll.addMouseListener(this);

			// Remove slider of fiji
			// TODO: change this code when the new version of fiji will allow access to the
			// slider
			if (this.getComponents()[1] instanceof ScrollbarWithLabel) this.getComponents()[1].setVisible(false);

		} else {
			// Destroy tooltip
			this.tooltip = null;
			// TODO: this line resets the alignment of the panel to the left. Why???
			this.panelContainer.remove(scroll);

			// Remove listeners
			if (this.getController() != null) {
				ControllerWorkflow controller = (ControllerWorkflow) this.getController();
				this.scroll.removeAdjustmentListener(controller);
				this.removeMouseWheelListener(controller);
			}
			this.scroll.removeMouseListener(this);
			this.scroll.removeMouseMotionListener(this);

			// Replace slider of fiji
			// TODO: change this code when the new version of fiji will allow access to the
			// slider
			if (this.getComponents()[1] instanceof ScrollbarWithLabel) this.getComponents()[1].setVisible(true);
		}
	}

	/**
	 * @return TRUE if the visualization scroll bar is enabled and FALSE if the
	 *         default IJ scroll is left
	 */
	public boolean isVisualizationEnabled() {
		return this.tooltip != null;
	}

	/**
	 * Sets the number of instructions the scrollbar can go through.
	 *
	 * @param nbInstructions
	 *            Number of instructions
	 */
	public void setNbInstructions(int nbInstructions) {
		this.scroll.setValues(0, 1, 0, nbInstructions);
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
	 * @param value
	 *            Index of the instruction
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
	 * @param image
	 *            New image to display
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

	@Override
	public ControllerWorkflow getController() {
		return (ControllerWorkflow) super.getController();
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
		if (this.isVisualizationEnabled()) {
			// This code comes from IJ, it was paste here to keep only the zoom
			// functionality available
			int rotation = e.getWheelRotation();
			boolean ctrl = (e.getModifiers() & Event.CTRL_MASK) != 0;
			if ((ctrl || IJ.shiftKeyDown()) && ic != null) {
				Point loc = ic.getCursorLoc();
				int x = ic.screenX(loc.x);
				int y = ic.screenY(loc.y);
				if (rotation < 0) ic.zoomIn(x, y);
				else ic.zoomOut(x, y);
			}
		} else {
			super.mouseWheelMoved(e);
		}
	}

}
