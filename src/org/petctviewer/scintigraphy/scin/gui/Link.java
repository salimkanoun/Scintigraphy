package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class Link extends JButton {

	public Link(String text, URI uri) {
		super("<html><font color=\"#000099\"><u>" + text + "</u></font></html>");
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorderPainted(false);
		this.setOpaque(false);
		this.setBackground(Color.WHITE);
		this.setToolTipText(uri.toString());
		this.setMargin(new Insets(0, 0, 0, 0));
		this.addActionListener((event) -> {
			if(Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(uri);
				} catch (IOException e) {
					System.err.println("Could not launch browser");
				}
			}
		});
	}

}
