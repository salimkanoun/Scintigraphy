package org.petctviewer.scintigraphy.scin.instructions.execution;

import java.util.List;

import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

public class ScreenShotInstruction extends ExecutionInstruction {

	private List<ImagePlus> captures;

	private FenApplication fen;

	private int captureHeight, captureWidht;

	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen, int captureWidth, int captureHeight) {
		this.captures = captures;
		this.fen = fen;
		this.captureHeight = captureHeight;
		this.captureWidht = captureWidth;
	}

	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen) {
		this(captures, fen, 512, 0);
	}

	@Override
	public void prepareAsNext() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.fen.getImagePlus(), this.captureWidht,
				this.captureHeight);
		this.captures.add(capture);

	}

}
