package org.petctviewer.scintigraphy.gastric;

import java.awt.*;

public class InstructionTooltip extends Window {
	private static final long serialVersionUID = 1L;

	private final Label label;
	
	public InstructionTooltip(Frame parent) throws HeadlessException {
		super(parent);
		
		this.label = new Label();
		this.add(label);
	}
	
	public void setText(String msg) {
		this.label.setText(msg);
	}

	@Override
	public void setBackground(Color bgColor) {
		super.setBackground(bgColor);
		this.label.setBackground(bgColor);
	}

}
