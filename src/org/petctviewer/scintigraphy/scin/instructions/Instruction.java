package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;

/**
 * Represents an instruction in the workflow.
 *
 * @author Titouan QUÉMA
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
	 * At this stage, the controller has not yet started its work.
	 */
	void prepareAsNext();

	/**
	 * This method is called when this instruction is displayed in response to a
	 * 'Previous' click. <br>
	 * At this stage, the controller has not yet started its work.
	 */
	void prepareAsPrevious();

	/**
	 * Gets the message instruction displayed to the user.<br>
	 * <i>Ignored if {@link #isExpectingUserInput()} is FALSE.</i><br>
	 * If {@link #isExpectingUserInput()} is TRUE, this method <b>cannot</b> return null.
	 *
	 * @return message displayed for the user
	 */
	String getMessage();

	/**
	 * This method should return the name of the ROI that will be stored in the RoiManager.<br>
	 * For example, if this Instruction delimit the brain, then this method should
	 * return "Brain". <p>
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i><br>
	 * If {@link #saveRoi()} is TRUE, this method <b>cannot</b> return null.
	 * </p>
	 *
	 * @return name of the ROI to store in the RoiManager
	 */
	String getRoiName();

	/**
	 * If an instruction expects an input from the user, then it will remain on this instruction util the user clicks
	 * on the 'Next' button.<br>
	 * If an instruction is not displayed to the user, then it will automatically go to the next
	 * instruction without the user intervention.
	 * <p>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 * </p>
	 *
	 * @return TRUE if this instruction should wait for an input and FALSE if this instruction should not be displayed
	 * to the user
	 */
	boolean isExpectingUserInput();

	/**
	 * If an instruction saves a ROI, it must:
	 * <ul>
	 * <li>ask the user to draw a ROI</li>
	 * or
	 * <li>automatically draw a ROI in the {@link #prepareAsNext()} method</li>
	 * </ul>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 *
	 * @return TRUE if this instruction needs to save a ROI or FALSE otherwise
	 */
	boolean saveRoi();

	/**
	 * If an instruction is cancelled, then the 'Next' button is not executed and the workflow stays on the same
	 * instruction.<br>
	 * When returning TRUE, please be careful to always leave a way out for the user.
	 *
	 * @return TRUE if this instruction is cancelled and FALSE otherwise
	 */
	boolean isCancelled();

	/**
	 * The ROI of an instruction will only be visible if:
	 * <ul>
	 * <li>the ROI exists</li>
	 * <li>the current ROI index is greater than this ROI index</li>
	 * <li>the current image is the same as this workflow's image</li>
	 * <li>the current orientation is the same as this instruction's image state's
	 * orientation</li>
	 * <li>this method returns TRUE</li>
	 * </ul>
	 * If one of this conditions is FALSE, then the ROI will not be drawn.<br>
	 * <br>
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i>
	 *
	 * @return TRUE if the ROI saved by this instruction should be visible on the
	 * overlay and FALSE otherwise
	 */
	boolean isRoiVisible();

	/**
	 * State in which the ImagePlus must be on the screen when this instruction is displayed.<br>
	 * If this method returns null, then the image will be in the state of the previous instruction (it is preferable
	 * not to return null).<br>
	 * If this instruction is the first of the workflow, then this method <b>cannot</b> return null!<br>
	 * <i>Ignored if {@link #isCancelled()} is TRUE</i>
	 *
	 * @return state of the ImagePlus
	 */
	ImageState getImageState();

	/**
	 * This method is called after the 'Next' button has been clicked.<br>
	 * At this stage, the controller has done its work. This is the last method called before waiting for another
	 * user input.
	 */
	void afterNext(ControllerWorkflow controller);

	/**
	 * This method is called after the 'Previous' button has been clicked.<br>
	 * At this stage, the controller has done its work. This is the last method called before waiting for another
	 * user input.
	 */
	void afterPrevious(ControllerWorkflow controller);

	/**
	 * Gets the ROI index of this instruction. This method must always be in coherence with the {@link #setRoi(int)}
	 * method.<br>
	 * You cannot assume the ROI index is a constant since the controller of the workflow can evolve.
	 * <i>Ignored if {@link #saveRoi()} is FALSE</i>
	 *
	 * @return index of the ROI to display or <0 if none
	 */
	int getRoiIndex();

	/**
	 * This method is called when the ROI is set for this Instruction.
	 *
	 * @param index ROI set for this Instruction
	 */
	void setRoi(int index);

}
