package org.petctviewer.scintigraphy.scin.json;

import org.petctviewer.scintigraphy.scin.instructions.Instruction;

/**
 * This class represent an {@link Instruction}, as saved in a Json file.<br/>
 * Contains :<br/>
 * &emsp;&emsp; - InstructionType<br/>
 * &emsp;&emsp; - IndexRoiToEdit<br/>
 * &emsp;&emsp; - NameOfRoi<br/>
 * &emsp;&emsp; - NameOfRoiFile
 *
 */
public class InstructionFromGson {

	private String InstructionType;

	private int IndexRoiToEdit;

	private String NameOfRoi;

	private String NameOfRoiFile;

	public int getIndexRoiToEdit() {
		return this.IndexRoiToEdit;
	}

	public String getInstructionType() {
		return this.InstructionType;
	}

	@SuppressWarnings("unused")
	public String getNameOfRoi() {
		return this.NameOfRoi;
	}

	public String getNameOfRoiFile() {
		return this.NameOfRoiFile;
	}

	/**
	 * This enum is use to save and load instructions that draw ROI.
	 */
	public enum DrawInstructionType {

		DRAW_ROI("DrawRoiInstruction"), DRAW_LOOP("DrawLoopInstruction"), DRAW_ROI_BACKGROUND("DrawRoiBackground"),
		DRAW_SYMMETRICAL("DrawSymmetricalRoiInstruction"), DRAW_SYMMETRICAL_LOOP("DrawSymmetricalLoopInstruction"),
		CHECK_INTERSECTION("CheckIntersectionInstruction");

		private final String name;

		DrawInstructionType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
