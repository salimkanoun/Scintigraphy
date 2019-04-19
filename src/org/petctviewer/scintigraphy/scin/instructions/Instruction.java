package org.petctviewer.scintigraphy.scin.instructions;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.Orientation;

/**
 * Represents an instruction in the workflow.
 * 
 * @author Titouan QUÉMA
 *
 */
public interface Instruction {

	/**
	 * Represents a state of an ImagePlus.<br>
	 * If a field is set to null, then it will be interpreted as no changes.
	 * 
	 * @author Titouan QUÉMA
	 *
	 */
	public class ImageState {
		/**
		 * Orientation of the image.
		 */
		public Orientation orientation;

		public ImageState() {
		}

		public ImageState(Orientation orientation) {
			this.orientation = orientation;
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
	 * @return message displayed for the user
	 */
	public abstract String getMessage();

	/**
	 * If an instruction is not displayed, then it will automatically go to the next
	 * instruction without the user intervention.<br>
	 * <br>
	 * If an instruction is not displayed, {@link #isDisplayable()} is ignored.
	 * 
	 * @return TRUE if this instruction should be displayed on the screen of the
	 *         user and FALSE if this instruction should not be displayed
	 */
	public abstract boolean isDisplayable();

	/**
	 * State in which the ImagePlus must be when this instruction is displayed.<br>
	 * If this method returns null, then the image will remain constant with the
	 * previous instruction (it is preferable NOT to return null).<br>
	 * <br>
	 * <i>Ignored if {@link #isDisplayable()} is FALSE</i>
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

}
