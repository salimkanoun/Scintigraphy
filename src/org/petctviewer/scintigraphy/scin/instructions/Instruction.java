package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;

/**
 * Represents an instruction in the workflow.
 * 
 * @author Titouan QUÉMA
 *
 */
public interface Instruction {
	
	
	/**
	 * This enum is use to save and load instructions that draw ROI.
	 *
	 */
	public enum DrawInstructionType {
		
		DRAW_ROI("DrawRoiInstruction"), 
		DRAW_LOOP("DrawLoopInstruction"), 
		DRAW_ROI_BACKGROUND("DrawRoiBackground"), 
		DRAW_SYMMETRICAL("DrawSymmetricalRoiInstruction"), 
		DRAW_SYMMETRICAL_LOOP("DrawSymmetricalLoopInstruction"),
		CHECK_INTERSECTION("CheckIntersectionInstruction");
		
		private String name;
		
		private DrawInstructionType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
	}

	/**
	 * This method is called when this instruction is displayed in response to a
	 * 'Next' click. <br>
	 * At this stage, we are before all of the operations for the next action are
	 * done.
	 */
	public abstract void prepareAsNext();

	/**
	 * This method is called when this instruction is displayed in response to a
	 * 'Previous' click. <br>
	 * At this stage, we are before all of the operations for the previous action
	 * are done.
	 */
	public abstract void prepareAsPrevious();

	/**
	 * <i>Ignored if {@link #isExpectingUserInput()} is FALSE.</i><br>
	 * If {@link #isExpectingUserInput()} is TRUE, this method cannot return null.
	 * 
	 * @return message displayed for the user
	 */
	public abstract String getMessage();

	/**
	 * This method should return the name of the organ delimited by the ROI.<br>
	 * For example, if this Instruction delimit the brain, then this method should
	 * return "Brain". <br>
	 * <br>
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i>
	 * 
	 * @return name of the ROI to store in the RoiManager
	 */
	public abstract String getRoiName();

	/**
	 * If an instruction is not displayed, then it will automatically go to the next
	 * instruction without the user intervention.<br>
	 * <br>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 * 
	 * @return TRUE if this instruction should be displayed on the screen of the
	 *         user and FALSE if this instruction should not be displayed
	 */
	public abstract boolean isExpectingUserInput();

	/**
	 * If an instruction saves a ROI, it must:
	 * <ul>
	 * <li>ask the user to draw a ROI</li><br>
	 * or
	 * <li>automatically draw a ROI in the {@link #prepareAsNext()} or
	 * {@link #prepareAsPrevious()} method</li>
	 * </ul>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 * 
	 * @return TRUE if this instruction needs to save a ROI or FALSE otherwise
	 */
	public abstract boolean saveRoi();

	/**
	 * If an instruction is cancelled, then the 'Next' button is not executed.
	 * 
	 * @return TRUE if this instruction is cancelled and FALSE otherwise
	 */
	public abstract boolean isCancelled();

	/**
	 * The ROI of an instruction will only be visible if:
	 * <ul>
	 * <li>the ROI exists</li>
	 * <li>the current ROI index is greater than this ROI index</li>
	 * <li>the current image is the same as this workflow's image</li>
	 * <li>the current orientation is the same as this instruction's image state's
	 * orientation</li>
	 * </ul>
	 * If one of this conditions is FALSE, then the ROI will not be drawn.<br>
	 * <br>
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i>
	 * 
	 * @return TRUE if the ROI saved by this instruction should be visible on the
	 *         overlay and FALSE otherwise
	 */
	public abstract boolean isRoiVisible();

	/**
	 * State in which the ImagePlus must be when this instruction is displayed.<br>
	 * If this method returns null, then the image will remain constant with the
	 * previous instruction (it is preferable not to return null).<br>
	 * <br>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 * 
	 * @return state of the ImagePlus
	 */
	public abstract ImageState getImageState();

	/**
	 * This method is called after the 'Next' button has been clicked.<br>
	 * At this stage, all of the operations for the next action are done.
	 */
	public abstract void afterNext(ControllerWorkflow controller);

	/**
	 * This method is called after the 'Previous' button has been clicked.<br>
	 * At this stage, all of the operations for the previous action are done.
	 */
	public abstract void afterPrevious(ControllerWorkflow controller);

	/**
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i>
	 * 
	 * @return index of the ROI to display or <0 if none
	 */
	public abstract int roiToDisplay();

	/**
	 * This method is called when the ROI is set for this Instruction. This will
	 * replace the indexRoiToEdit.
	 * 
	 * @param roi ROI set at this Instruction
	 */
	public abstract void setRoi(int index);

}
