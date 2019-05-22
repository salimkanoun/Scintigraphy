package org.petctviewer.scintigraphy.scin;

public class innerInstruction {
	
	public enum InstructionType {
		
		DRAW_ROI("DrawRoiInstruction"), 
		DRAW_LOOP("DrawLoopInstruction"), 
		DRAW_ROI_BACKGROUND("DrawRoiBackground"), 
		DRAW_SYMMETRICAL("DrawSymmetricalLoopInstruction"), 
		DRAW_SYMMETRICAL_LOOP("DrawSymmetricalRoiInstruction"),
		CHECK_INTERSECTION("CheckIntersectionInstruction");
		
		private String name;
		
		private InstructionType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
	
	}
	
	private InstructionType instructionType;
	
	private int indexRoi;
	
	public innerInstruction(InstructionType instructionType, int indexRoi) {
		this.instructionType = instructionType;
		this.indexRoi = indexRoi;
	}
	
	public InstructionType getInstructionType() {
		return this.instructionType;
	}

	public int getRoi() {
		return this.indexRoi;
	}
}
