package org.petctviewer.scintigraphy.gastric_refactored.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class FitCellRenderer extends JLabel implements ListCellRenderer<Fit> {
	private static final long serialVersionUID = 1L;
	
	public FitCellRenderer() {
		this.setOpaque(true);
		this.setHorizontalAlignment(CENTER);
		this.setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Fit value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if(isSelected) {
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		} else {
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}
		
		Icon icon = value.getIcon();
		if(icon != null) {
			this.setIcon(value.getIcon());
			this.setText(value.getName());
		} else {
			this.setText(value.getName() + " (no image available)");
		}
		
		return this;
	}

}
