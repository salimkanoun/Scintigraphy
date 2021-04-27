package org.petctviewer.scintigraphy.scin.controller;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.MontageMaker;
import ij.plugin.frame.RoiManager;
import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;
import org.petctviewer.scintigraphy.scin.model.ModelScin;
import org.petctviewer.scintigraphy.scin.preferences.PrefTabMain;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * This class represents the Controller in the MVC pattern.<br> This abstract class is only a provider for functions
 * simplifying the interactions with the RoiManager of Fiji.
 *
 * @author Titouan QUÉMA
 */
public abstract class ControllerScin implements ActionListener {

	/**
	 * View of the MVC pattern
	 */
	protected final FenApplication vue;
	protected final ModelScin model;
	/**
	 * Position in the flow of the controller.<br> Increments when 'Next' button is pressed.<br> Decrements when
	 * 'Previous' button is pressed.<br> The position must always be positive.
	 */
	protected int position;
	private boolean inverted;

	public ControllerScin(FenApplication vue, ModelScin model) {
		this.vue = vue;
		this.position = 0;
		this.model = model;
		this.inverted = false;

		Roi.setColor(Color.RED);
	}

	/**
	 * Checks if the controller has done its work.
	 *
	 * @return TRUE if the controller has finished, FALSE otherwise
	 */
	public abstract boolean isOver();

	/**
	 * @return view of the MVC pattern
	 */
	public FenApplication getVue() {
		return this.vue;
	}

	/**
	 * @return model of the MVC pattern
	 */
	public ModelScin getModel() {
		return this.model;
	}

	/**
	 * The position increments when clicking on the 'Next' button and decrements when clicking on the 'Previous'
	 * button.
	 *
	 * @return actual position
	 */
	public int getPosition() {
		return this.position;
	}

	/**
	 * This method should be called when the controller is over.
	 */
	protected void end() {
		this.vue.getTextfield_instructions().setText("End!");
		this.vue.getBtn_suivant().setEnabled(false);
	}

	/**
	 * This method is called when the FenApplication is closed.
	 */
	public void close() {
		this.model.getRoiManager().close();
	}

	/**
	 * This method is called when the 'Previous' button is pressed. It will decrement the position by 1.
	 */
	public void clickPrevious() {
		// TODO: this might be useless
		this.position--;
		if (this.position == 0) {
			this.vue.getBtn_precedent().setEnabled(false);
		}
		this.vue.getBtn_suivant().setEnabled(true);
	}

	/**
	 * This method is called when the 'Next' button is pressed. It will increment the position by 1.
	 */
	public void clickNext() {
		this.position++;
		this.vue.getBtn_precedent().setEnabled(true);
	}

	/**
	 * Displays the instruction on the view.
	 *
	 * @param instruction Instruction to display
	 */
	public void displayInstruction(String instruction) {
		this.vue.getTextfield_instructions().setText(instruction);
		this.vue.pack();
	}

	/**
	 * Creates an ImagePlus with 4 captures.
	 *
	 * @param captures ImageStack with 4 captures
	 * @return ImagePlus with the 4 captures on 1 slice
	 * @throws IllegalArgumentException if the number of captures is different than 4
	 */
	protected ImagePlus montage(ImageStack captures) throws IllegalArgumentException {
		if (captures.getSize() != 4) throw new IllegalArgumentException(
				"The number of captures (" + captures.getSize() + ") must be 4");

		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Results -" + this.model.getStudyName(), captures);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		return imp;
	}


	/**
	 * Creates an ImagePlus with 2 captures.
	 *
	 * @param captures ImageStack with 2 captures
	 * @return ImagePlus with the 2 captures on 1 slice
	 * @throws IllegalArgumentException if the number of captures is different than 2
	 */
	protected ImagePlus montageForTwo(ImageStack captures) throws IllegalArgumentException {
		if (captures.getSize() != 2) throw new IllegalArgumentException(
				"The number of captures (" + captures.getSize() + ") must be 2");

		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Results -" + this.model.getStudyName(), captures);
		imp = mm.makeMontage2(imp, 2, 1, 0.75, 1, 2, 1, 10, false);
		return imp;
	}

	
	/**
	 * Creates an ImagePlus with 3 captures.
	 *
	 * @param captures ImageStack with 3 captures
	 * @return ImagePlus with the 3 captures on 1 slice
	 * @throws IllegalArgumentException if the number of captures is different than 3
	 */
	protected ImagePlus montageForThree(ImageStack captures) throws IllegalArgumentException {
		if (captures.getSize() != 3) throw new IllegalArgumentException(
				"The number of captures (" + captures.getSize() + ") must be 2");

		MontageMaker mm = new MontageMaker();
		ImagePlus imp = new ImagePlus("Results -" + this.model.getStudyName(), captures);
		imp = mm.makeMontage2(imp, 3, 1, 0.50, 1, 3, 1, 10, false);
		return imp;
	}

	/**
	 * Saves the current ROI of the current ImagePlus in the RoiManager at the specified index. If a ROI with the same
	 * name has already been saved, it will be replaced.
	 *
	 * @param name           Name of the ROI to save
	 * @param indexRoiToSave index of the ROI to save on the RoiManager
	 * @throws NoDataException if no ROI is present on the current ImagePlus
	 */

	public void saveRoiAtIndex(String name, int indexRoiToSave) throws NoDataException {
		Roi roiToSave = this.vue.getImagePlus().getRoi();

		// Check if there is a ROI to save
		if (roiToSave == null) throw new NoDataException("No ROI to save");

		// TODO: maybe allow the user to choose the color for the ROI?
		if (this.inverted)
			roiToSave.setStrokeColor(Color.BLUE);
		else
			roiToSave.setStrokeColor(Color.YELLOW);
		roiToSave.setPosition(0);

		Roi existingRoi = this.getRoiManager().getRoi(indexRoiToSave);

		// Check if there is an existing ROI
		if (existingRoi != null) {
			// Overwrite it
			this.model.getRoiManager().setRoi(roiToSave, indexRoiToSave);
		} else {
			// Add it
			this.model.getRoiManager().addRoi(roiToSave);
		}
		this.vue.getImagePlus().killRoi();

		// Name the ROI
		this.model.getRoiManager().rename(indexRoiToSave, name);
	}

	/**
	 * Displays the ROI with the specified index if existing on the overlay of the current image.
	 *
	 * @param index Index of the ROI to display
	 */
	public void displayRoi(int index) {
		this.displayRois(new int[]{index});
	}

	/**
	 * Displays all existing ROIs with the specified indexes on the overlay of the current image.
	 *
	 * @param indexes Indexes of the ROIs to display
	 */
	public void displayRois(int[] indexes) {
		// Get ROIs to display
		for (int i : indexes) {
			Roi roiToDisplay = this.model.getRoiManager().getRoi(i);
			if (roiToDisplay != null) {
				this.vue.getImagePlus().getOverlay().add(roiToDisplay);
			}
		}

		this.updateROIColor();
	}

	/**
	 * Displays all of the existing ROIs that have an index >= index_start and < index_end.<br>
	 *
	 * @param index_start First ROI index to be displayed
	 * @param index_end   The last ROI index (not displayed)
	 */
	public void displayRois(int index_start, int index_end) {
		int[] array = new int[index_end - index_start];
		for (int i = index_start; i < index_end; i++) {
			array[i - index_start] = i;
		}
		this.displayRois(array);
	}

	/**
	 * Displays all of the existing ROIs that have an index < to the specified index.<br>
	 */
	public void displayRoisUpTo(int index) {
		this.displayRois(0, index);
	}

	/**
	 * Places the correct title and lateralisation according to the specified state.<br>
	 *
	 * @param state The following state parameters must be provided:
	 *              <ul>
	 *              <li>lateralisation</li>
	 *              <li>facingOrientation (cannot be <code>null</code>)</li>
	 *              <li>slice (cannot be less or equals to
	 *              <code>{@link ImageState#SLICE_PREVIOUS}</code>)</li>
	 *              </ul>
	 * @throws IllegalArgumentException if the state doesn't have the required data
	 */
	public void setOverlay(ImageState state) throws IllegalArgumentException {
		if (state == null) throw new IllegalArgumentException("The state cannot be null");
		if (state.getFacingOrientation() == null) throw new IllegalArgumentException(
				"The state misses the required data: -facingOrientation=" + state.getFacingOrientation() + "; " +
						state.getSlice());
		if (state.getSlice() <= ImageState.SLICE_PREVIOUS) throw new IllegalArgumentException("The slice is invalid");

		if (state.isLateralisationRL()) Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
		else Library_Gui.setOverlayGD(this.vue.getImagePlus(), Color.YELLOW);

		Library_Gui.setOverlayTitle(state.title(), this.vue.getImagePlus(), Color.YELLOW, state.getSlice());
	}

	/**
	 * Displays a clone of the ROI with the specified index if existing and make it editable.
	 *
	 * @param index Index of the ROI to clone and edit
	 */
	public void editCopyRoi(int index) {
		Roi roiToEdit = this.model.getRoiManager().getRoi(index);
		if (roiToEdit != null) {
			this.vue.getImagePlus().setRoi((Roi) roiToEdit.clone());
			this.vue.getImagePlus().getRoi().setStrokeColor(Color.RED);
		}
	}

	/**
	 * Displays a clone of the specified ROI and make it editable.<br> If the ROI is null, this method does nothing.
	 *
	 * @param roi ROI to clone and edit
	 */
	public void editCopyRoi(Roi roi) {
		if (roi != null) {
			this.vue.getImagePlus().setRoi((Roi) roi.clone());
			this.vue.getImagePlus().getRoi().setStrokeColor(Color.RED);
		}
	}

	/**
	 * Displays the ROI with the specified index if existing and make it editable.
	 *
	 * @param index Index of the ROI to edit
	 * @return TRUE if the ROI already existed and could be retrieved and FALSE if not
	 */
	public boolean editRoi(int index) {
		Roi roiToEdit = this.model.getRoiManager().getRoi(index);
		if (roiToEdit != null) {
			this.vue.getImagePlus().setRoi(roiToEdit);
			this.vue.getImagePlus().getRoi().setStrokeColor(Color.RED);
			return true;
		}
		return false;
	}

	/**
	 * Displays the ROI with the specified name if existing and make it editable.
	 *
	 * @param name Name of the ROI to edit
	 * @return TRUE if the ROI already existed and could be retrieved and FALSE if not
	 */
	public boolean editRoi(String name) {
		return this.editRoi(this.model.getRoiManager().getRoiIndex(this.getRoi(name)));
	}

	/**
	 * Finds the first ROI matching the specified name.
	 *
	 * @param name Name of the ROI to find
	 * @return first ROI found or null if not
	 */
	protected Roi getRoi(String name) {
		for (Roi r : this.model.getRoiManager().getRoisAsArray())
			if (r.getName().equals(name)) return r;
		return null;
	}

	/**
	 * Creates a rectangle between the two ROIs specified.<br> TODO: move this method in Library_Roi
	 *
	 * @return Rectangle at the center of the ROIs specified
	 */
	protected Rectangle roiBetween(Roi r1, Roi r2) {
		int x = (int) ((r1.getBounds().getLocation().x + r2.getBounds().getLocation().x + r2.getBounds().getWidth()) /
				2);
		int y = (r1.getBounds().getLocation().y + r2.getBounds().getLocation().y) / 2;
		return new Rectangle(x, y, 15, 30);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!(e.getSource() instanceof Button)) return;

		Button b = (Button) e.getSource();

		if (b == this.vue.getBtn_suivant()) {
			this.clickNext();

		} else if (b == this.vue.getBtn_precedent()) {
			this.clickPrevious();

		} else if (b == this.vue.getBtn_drawROI()) {
			Button btn = this.vue.getBtn_drawROI();

			// on change la couleur du bouton
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}

			// on deselectionne le bouton contraste
			this.vue.getBtn_reverse().setBackground(null);

			IJ.setTool(PrefTabMain.toolFromString(Prefs.get(PrefTabMain.PREF_TOOL_ROI, "Polygone")));

		} else if (b == this.vue.getBtn_reverse()) {
			this.inverted = !this.inverted;
			// on change la couleur du bouton
			if (b.getBackground() != Color.LIGHT_GRAY) {
				b.setBackground(Color.LIGHT_GRAY);
			} else {
				b.setBackground(null);
			}

			// on deselectionne le bouton draw roi
			this.vue.getBtn_drawROI().setBackground(null);

			IJ.run("Invert LUT", "stack");
			this.updateROIColor();


		} else if (b == this.vue.getBtn_quitter()) {
			this.vue.close();
		}
	}

	public RoiManager getRoiManager() {
		return this.model.getRoiManager();
	}

	private void updateROIColor() {
		String opts_lbl = "show use";
		String opts_roi;

		if (this.inverted) {
			opts_lbl += " draw";
			opts_roi = "black width=1";
		} else {
			opts_roi = "yellow width=0";
		}

		IJ.run("Labels...", opts_lbl);
		IJ.run("Overlay Options...", "stroke="+ opts_roi +" set apply show");
		//IJ.run("Select None");
	}

}