package org.petctviewer.scintigraphy.scin.instructions.execution;

import ij.ImagePlus;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import java.util.List;

/**
 * Instruction to take a capture of the image in the FenApplication when this instruction is executed.
 *
 * @author Esteban BAICHOO
 * @author Titouan QUÉMA
 */
public class ScreenShotInstruction extends ExecutionInstruction {

	public static final int DEFAULT_CAPTURE_WIDTH = 512;
	private static final long serialVersionUID = 1L;
	private final transient List<ImagePlus> captures;
	private final int captureIndex;

	private final transient FenApplication fen;

	private final int captureHeight;
	private final int captureWidth;

	/**
	 * If the width or the height is set to 0, then the ratio is kept.<br> If both width and height are set to 0, then
	 * the natural dimensions of the image are used.
	 *
	 * @param captures      Array of images where the capture will be inserted
	 * @param fen           View where the capture will be taken from
	 * @param captureIndex  Index in the capture array where this instruction will store the new capture
	 * @param captureWidth  Width of the capture
	 * @param captureHeight Height of the capture
	 * @see Library_Capture_CSV#captureImage(ImagePlus, int, int)
	 */
	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen, int captureIndex, int captureWidth,
								 int captureHeight) {
		this.captures = captures;
		this.fen = fen;
		this.captureIndex = captureIndex;
		this.captureHeight = captureHeight;
		this.captureWidth = captureWidth;
	}

	/**
	 * The {@link #DEFAULT_CAPTURE_WIDTH} will be taken with this instantiation and the ratio will be kept.
	 *
	 * @param captures     Array of images where the capture will be inserted
	 * @param fen          View where the capture will be taken from
	 * @param captureIndex Index in the capture array where this instruction will store the new capture
	 * @see Library_Capture_CSV#captureImage(ImagePlus, int, int)
	 */
	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen, int captureIndex) {
		this(captures, fen, captureIndex, DEFAULT_CAPTURE_WIDTH, 0);
	}

	@Override
	public void prepareAsNext() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.fen.getImagePlus(), this.captureWidth,
															 this.captureHeight);
		if (this.captures.size() > this.captureIndex) {
			// Replace the previous capture
			this.captures.set(captureIndex, capture);
		} else if (this.captures.size() == this.captureIndex) {
			// Add new capture in the list
			this.captures.add(capture);
		} else {
			// Wrong index
			System.err.println(
					"Error during capture:\nThe index is " + this.captureIndex + " but the list is only at index " +
							this.captures.size() +
							"\nMaybe some instructions between are missing or the index is just wrong!");
		}
	}

}
