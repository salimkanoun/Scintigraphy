package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

public class DrawRoiInstruction implements Instruction {

	private String organToDelimit;
	private String roiName;
	private boolean isAdjusting;
	private ImageState state;
	private DrawRoiInstruction instructionToCopy;
	private int indexRoiToEdit;

	/**
	 * Instantiates a new instruction to draw ROI. With this constructor, you can
	 * specify a ROI to edit and roi name to display.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 * @param instructionToCopy
	 *            Instruction to take a copy of the ROI from
	 * @param roiName
	 *            Name of the Roi (displayed one)
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state, DrawRoiInstruction instructionToCopy,
			String roiName) {
		this.organToDelimit = organToDelimit;
		this.isAdjusting = false;
		this.state = state;
		this.instructionToCopy = instructionToCopy;
		this.indexRoiToEdit = -1;
		this.roiName = roiName == null ? organToDelimit : roiName;
	}

	/**
	 * Instantiates a new instruction to draw ROI. With this constructor, you can
	 * specify a ROI to edit.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 * @param instructionToCopy
	 *            Instruction to take a copy of the ROI from
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state, DrawRoiInstruction instructionToCopy) {
		this(organToDelimit, state, instructionToCopy, null);
	}

	/**
	 * Instantiates a new instruction to draw ROI.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state) {
		this(organToDelimit, state, null, null);
	}

	/**
	 * Instantiates a new instruction to draw ROI. With this constructor, you can
	 * specify a ROI to edit.
	 * 
	 * @param organToDelimit
	 *            Name of the organ to delimit
	 * @param state
	 *            State of the image
	 * @param roiName
	 *            Name of the Roi (displayed one)
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state, String roiName) {
		this(organToDelimit, state, null, roiName);
	}

	@Override
	public String getRoiName() {
		return this.roiName;
	}

	/**
	 * This method return the name of the organ delimited by the ROI. This name
	 * could be different of the RoiName because, for background, you don't want to
	 * display the RoiName, and you have to specify it to "".
	 * 
	 * @return
	 */
	public String getOrganToDelimit() {
		return this.organToDelimit;
	}

	@Override
	public void setRoi(int index) {
		this.indexRoiToEdit = index;
	}

	@Override
	public int roiToDisplay() {
		if (this.indexRoiToEdit != -1)
			return this.indexRoiToEdit;
		else if (this.instructionToCopy != null)
			return this.instructionToCopy.indexRoiToEdit;
		else
			return -1;
	}

	@Override
	public ImageState getImageState() {
		return this.state;
	}

	@Override
	public boolean isExpectingUserInput() {
		return true;
	}

	@Override
	public String getMessage() {
		return (this.isAdjusting ? "Adjust" : "Delimit") + " the " + this.organToDelimit;
	}

	@Override
	public void prepareAsNext() {
		this.isAdjusting = false;
	}

	@Override
	public void prepareAsPrevious() {
		this.isAdjusting = true;
	}

	@Override
	public String toString() {
		return "DrawRoiInstruction [organToDelimit=" + organToDelimit + ", isAdjusting=" + isAdjusting + ", state="
				+ state + ", roiToEdit=" + indexRoiToEdit + ", roiName=" + roiName + "]";
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean saveRoi() {
		return true;
	}

	@Override
	public boolean isRoiVisible() {
		return true;
	}

}
