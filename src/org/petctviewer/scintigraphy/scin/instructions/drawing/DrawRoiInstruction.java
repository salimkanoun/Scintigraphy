package org.petctviewer.scintigraphy.scin.instructions.drawing;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.ImageState;
import org.petctviewer.scintigraphy.scin.instructions.Instruction;

public class DrawRoiInstruction implements Instruction {

	private String organToDelimit;
	private boolean isAdjusting;
	private ImageState state;
	private DrawRoiInstruction instructionToCopy;
	private int indexRoiToEdit;

	/**
	 * Instantiates a new instruction to draw ROI. With this constructor, you can
	 * specify a ROI to edit.
	 * 
	 * @param organToDelimit    Name of the organ to delimit
	 * @param state             State of the image
	 * @param instructionToCopy Instruction to take a copy of the ROI from
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state, DrawRoiInstruction instructionToCopy) {
		this.organToDelimit = organToDelimit;
		this.isAdjusting = false;
		this.state = state;
		this.instructionToCopy = instructionToCopy;
		this.indexRoiToEdit = -1;
	}

	/**
	 * Instantiates a new instruction to draw ROI.
	 * 
	 * @param organToDelimit Name of the organ to delimit
	 * @param state          State of the image
	 */
	public DrawRoiInstruction(String organToDelimit, ImageState state) {
		this(organToDelimit, state, null);
	}

	@Override
	public String getRoiName() {
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
				+ state + ", roiToEdit=" + indexRoiToEdit + "]";
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