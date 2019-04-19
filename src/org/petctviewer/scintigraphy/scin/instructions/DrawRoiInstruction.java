package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.Orientation;

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
	 * @param organToDelimit Name of the organ to delimit
	 * @param orientation    Orientation of the image
	 * @param indexRoiToEdit Index of the ROI to display for edition
	 */
	public DrawRoiInstruction(String organToDelimit, Orientation orientation, DrawRoiInstruction instructionToCopy) {
		this.organToDelimit = organToDelimit;
		this.isAdjusting = false;
		this.state = new ImageState();
		this.state.orientation = orientation;
		this.instructionToCopy = instructionToCopy;
		this.indexRoiToEdit = -1;
	}

	/**
	 * Instantiates a new instruction to draw ROI.
	 * 
	 * @param organToDelimit Name of the organ to delimit
	 * @param orientation    Orientation of the image
	 */
	public DrawRoiInstruction(String organToDelimit, Orientation orientation) {
		this(organToDelimit, orientation, null);
	}

	/**
	 * This method is called when the ROI is set for this Instruction. This will
	 * replace the indexRoiToEdit.
	 * 
	 * @param roi ROI set at this Instruction
	 */
	public void setRoi(int index) {
		this.indexRoiToEdit = index;
	}

	/**
	 * @return index of the ROI to display or <0 if none
	 */
	public int roiToDisplay() {
		if (this.indexRoiToEdit != -1)
			return this.indexRoiToEdit;
		else if (this.instructionToCopy != null)
			return this.instructionToCopy.indexRoiToEdit;
		else
			return -1;
	}

	public String getOrganName() {
		return this.organToDelimit;
	}

	@Override
	public ImageState getImageState() {
		return this.state;
	}

	@Override
	public boolean isDisplayable() {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
		// TODO Auto-generated method stub
		
	}

}
