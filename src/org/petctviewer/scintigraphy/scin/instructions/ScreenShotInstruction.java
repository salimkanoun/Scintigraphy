package org.petctviewer.scintigraphy.scin.instructions;

import java.util.List;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.gui.FenApplication;
import org.petctviewer.scintigraphy.scin.library.Library_Capture_CSV;

import ij.ImagePlus;

public class ScreenShotInstruction implements Instruction {

	private List<ImagePlus> captures;

	private FenApplication fenApp;

	public ScreenShotInstruction(List<ImagePlus> captures, FenApplication fen) {
		this.captures = captures;
		this.fenApp = fen;
		System.out.println("\n\n\n Captures enregistrées \n\n\n");
	}

	@Override
	public void prepareAsNext() {
		ImagePlus capture = Library_Capture_CSV.captureImage(this.fenApp.getImagePlus(), 512, 0);
		System.out.println(capture != null);
		System.out.println(captures != null);
		this.captures.add(capture);

	}

	@Override
	public void prepareAsPrevious() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExpectingUserInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveRoi() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRoiVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageState getImageState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void afterNext(ControllerWorkflow controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterPrevious(ControllerWorkflow controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public int roiToDisplay() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRoi(int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getRoiName() {
		// TODO Auto-generated method stub
		return null;
	}

}
