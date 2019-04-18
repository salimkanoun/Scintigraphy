package org.petctviewer.scintigraphy.scin;

import java.awt.Button;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import org.petctviewer.scintigraphy.scin.exceptions.NoDataException;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Gui;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.MontageMaker;
import ij.plugin.frame.RoiManager;

public abstract class ControleurScin implements ActionListener {

	/**
	 * View of the MVC pattern
	 */
	protected FenApplication vue;
	/**
	 * Position in the flow of the controller.<br>
	 * Increments when 'Next' button is pressed.<br>
	 * Decrements when 'Previous' button is pressed.<br>
	 * The position must always be positive.
	 */
	protected int position;
	/**
	 * Map of all the ROI names by they index
	 */
	protected Map<Integer, String> roiNames;

	protected Scintigraphy main;

	protected ModeleScin model;

	public ControleurScin(Scintigraphy main, FenApplication vue, ModeleScin model) {
		this.vue = vue;
		this.position = 0;
		this.roiNames = new HashMap<>();
		this.main = main;
		this.model = model;

		Roi.setColor(Color.RED);
	}

	/**
	 * Checks if the controller has done its work.
	 * 
	 * @return TRUE if the controller has finished, FALSE otherwise
	 */
	public abstract boolean isOver();

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
		this.model.roiManager.close();
	}

	/**
	 * This method is called when the 'Previous' button is pressed. It will
	 * decrement the position by 1.
	 */
	public void clicPrecedent() {
		this.position--;
		if (this.position == 0) {
			this.vue.getBtn_precedent().setEnabled(false);
		}
		this.vue.getBtn_suivant().setEnabled(true);
	}

	/**
	 * This method is called when the 'Next' button is pressed. It will increment
	 * the position by 1.
	 */
	public void clicSuivant() {
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
	 */
	protected ImagePlus montage(ImageStack captures) {
		MontageMaker mm = new MontageMaker();
		// TODO: patient ID
		String patientID = "NO_ID_FOUND";
		ImagePlus imp = new ImagePlus("Resultats ShunPo -" + this.main.getStudyName() + " -" + patientID, captures);
		imp = mm.makeMontage2(imp, 2, 2, 0.50, 1, 4, 1, 10, false);
		return imp;
	}

	/**
	 * Saves the current ROI of the current ImagePlus in the RoiManager. If a ROI
	 * with the same name has already been saved, it will be replaced.
	 * 
	 * @param name Name of the ROI to save
	 * @throws NoDataException if no ROI is present on the current ImagePlus
	 */
	public void saveCurrentRoi(String name) throws NoDataException {
		Roi roiToSave = this.vue.getImagePlus().getRoi();

		// Check if there is a ROI to save
		if (roiToSave == null)
			throw new NoDataException("No ROI to save");

		roiToSave.setStrokeColor(Color.YELLOW);
		roiToSave.setPosition(0);

		Roi existingRoi = this.getRoi(name);
		int posExisting = this.position;

		// Check if there is an existing ROI
		if (existingRoi != null) {
			posExisting = this.model.getRoiManager().getRoiIndex(existingRoi);
			// Overwrite it
			this.model.getRoiManager().setRoi(roiToSave, posExisting);
		} else {
			// Add it
			this.model.getRoiManager().addRoi(roiToSave);
		}
		this.vue.getImagePlus().killRoi();

		// Name the ROI
		this.model.getRoiManager().rename(posExisting, name);
		this.roiNames.put(posExisting, name);

	}

	/**
	 * @return index of the last ROI saved
	 */
	public int getIndexLastRoi() {
		return this.roiNames.size() - 1;
	}

	/**
	 * Displays the ROI with the specified index if existing on the overlay of the
	 * current image.
	 * 
	 * @param index Index of the ROI to display
	 */
	public void displayRoi(int index) {
		int[] array = { index };
		this.displayRois(array);
	}

	/**
	 * Displays all existing ROIs with the specified indexes on the overlay of the
	 * current image.
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
	}

	/**
	 * Displays all of the existing ROIs that have an index >= index_start and <
	 * index_end.<br>
	 * <i>Careful</i>: this method resets the image overlay.
	 * 
	 * @param index_start First ROI index to be displayed
	 * @param index_end   The last ROI index (not displayed)
	 */
	public void displayRois(int index_start, int index_end) {
		// Clear overlay
		this.resetOverlay();

		int[] array = new int[index_end - index_start];
		for (int i = index_start; i < index_end; i++) {
			array[i - index_start] = i;
		}
		this.displayRois(array);
	}

	/**
	 * Displays all of the existing ROIs that have an index < to the specified
	 * index.<br>
	 * <i>Careful</i>: this method resets the image overlay.
	 * 
	 * @param index
	 */
	public void displayRoisUpTo(int index) {
		this.displayRois(0, index);
	}

	/**
	 * Clears the current ImagePlus' overlay and replace the DG overlay and the
	 * title.<br>
	 * <b><i>Be careful</b></i>: this method assumes the current ImagePlus is in
	 * Ant/Post orientation and the Post is in DG. <br>
	 * TODO: Refactor this method to remove this assumption
	 */
	public void resetOverlay() {
		this.vue.getOverlay().clear();
		Library_Gui.setOverlayDG(this.vue.getImagePlus(), Color.YELLOW);
		Library_Gui.setOverlayTitle("Ant", this.vue.getImagePlus(), Color.YELLOW, 1);
		Library_Gui.setOverlayTitle("Post", this.vue.getImagePlus(), Color.YELLOW, 2);
	}

	/**
	 * Displays a clone of the ROI with the specified index if existing and make it
	 * editable.
	 * 
	 * @param index Index of the ROI to clone and edit
	 * @return TRUE if the ROI already existed and could be retrieved and FALSE if
	 *         not
	 */
	public boolean editCopyRoi(int index) {
		Roi roiToEdit = this.model.getRoiManager().getRoi(index);
		if (roiToEdit != null) {
			this.vue.getImagePlus().setRoi((Roi) roiToEdit.clone());
			this.vue.getImagePlus().getRoi().setStrokeColor(Color.RED);
			return true;
		}
		return false;
	}

	/**
	 * Displays the ROI with the specified index if existing and make it editable.
	 * 
	 * @param index Index of the ROI to edit
	 * @return TRUE if the ROI already existed and could be retrieved and FALSE if
	 *         not
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
	 * @return TRUE if the ROI already existed and could be retrieved and FALSE if
	 *         not
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
			if (r.getName().equals(name))
				return r;
		return null;
	}

	/**
	 * Creates a rectangle between the two ROIs specified. TODO: move this method in
	 * Library_Roi
	 * 
	 * @param r1
	 * @param r2
	 * @return Rectangle at the center of the ROIs specified
	 */
	protected Rectangle roiBetween(Roi r1, Roi r2) {
		int x = (int) ((r1.getBounds().getLocation().x + r2.getBounds().getLocation().x + r2.getBounds().getWidth())
				/ 2);
		int y = (r1.getBounds().getLocation().y + r2.getBounds().getLocation().y) / 2;
		return new Rectangle(x, y, 15, 30);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Button b = (Button) e.getSource();

		if (b == this.vue.getBtn_suivant()) {
			this.clicSuivant();

		} else if (b == this.vue.getBtn_precedent()) {
			this.clicPrecedent();

		} else if (b == this.vue.getBtn_drawROI()) {
			Button btn = this.vue.getBtn_drawROI();

			// on change la couleur du bouton
			if (btn.getBackground() != Color.LIGHT_GRAY) {
				btn.setBackground(Color.LIGHT_GRAY);
			} else {
				btn.setBackground(null);
			}

			// on deselectionne le bouton contraste
			this.vue.getBtn_contrast().setBackground(null);

			IJ.setTool(Toolbar.POLYGON);

		} else if (b == this.vue.getBtn_contrast()) {
			// on change la couleur du bouton
			if (b.getBackground() != Color.LIGHT_GRAY) {
				b.setBackground(Color.LIGHT_GRAY);
			} else {
				b.setBackground(null);
			}

			// on deselectionne le bouton draw roi
			this.vue.getBtn_drawROI().setBackground(null);

			IJ.run("Window Level Tool");

		} else if (b == this.vue.getBtn_quitter()) {
			this.vue.close();
			return;
		}
	}

	public RoiManager getRoiManager() {
		return this.model.getRoiManager();
	}

}
