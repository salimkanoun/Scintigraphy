package org.petctviewer.scintigraphy.scin.instructions;

import java.util.List;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

public class ScreenShotInstruction implements Instruction {

	private List<ImagePlus> captures;

//	private ImagePlus imp;
	private FenApplication fen;

	private int captureHeight, captureWidht;

//	public ScreenShotInstruction(List<ImagePlus> captures, ImagePlus imp, int captureHeight, int captureWidth) {
//		this.captures = captures;
//		this.imp = imp;
//		this.captureHeight = captureHeight;
//		this.captureWidht = captureWidth;
//	}

	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen, int captureWidth, int captureHeight) {
		this.captures = captures;
		this.fen = fen;
		this.captureHeight = captureHeight;
		this.captureWidht = captureWidth;
	}

//	public ScreenShotInstruction(List<ImagePlus> captures, ImagePlus imp) {
//		this(captures, imp, 512, 0);
//	}

	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen) {
		this(captures, fen, 512, 0);
	}

	@Override
	public void prepareAsNext() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.fen.getImagePlus(), this.captureWidht,
				this.captureHeight);
		System.out.println(capture != null);
		System.out.println(captures != null);
		this.captures.add(capture);

	}

	@Override
	public void prepareAsPrevious() {
	}

	@Override
	public String getMessage() {
		return "Taking screen shot, please wait...";
	}

	@Override
	public boolean isExpectingUserInput() {
		return false;
	}

	@Override
	public boolean saveRoi() {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isRoiVisible() {
		return false;
	}

	@Override
	public ImageState getImageState() {
		return null;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {

	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {

	}

	@Override
	public int roiToDisplay() {
		return -1;
	}

	@Override
	public void setRoi(int index) {

	}

	@Override
	public String getRoiName() {
		return null;
	}

}
