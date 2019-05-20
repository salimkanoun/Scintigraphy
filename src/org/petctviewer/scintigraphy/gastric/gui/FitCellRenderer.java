package org.petctviewer.scintigraphy.gastric.gui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.petctviewer.scintigraphy.gastric.gui.Fit.FitType;

public class FitCellRenderer extends JLabel implements ListCellRenderer<FitType> {
	private static final long serialVersionUID = 1L;

	public FitCellRenderer() {
		this.setOpaque(true);
		this.setHorizontalAlignment(CENTER);
		this.setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList list, FitType value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if (isSelected) {
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		} else {
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}

		try {
			this.setIcon(new ImageIcon(
					ImageIO.read(new File("src/org/petctviewer/scintigraphy/gastric_refactored/gui/fit_linear.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setText(value.toString());

		return this;
	}

}
