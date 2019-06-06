package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * This class represents a link that can be clicked on.
 *
 * @author Titouan QUÉMA
 */
public class Link extends JButton {
	private static final long serialVersionUID = 1L;

	public Link(String text, URI uri) {
		super("<html><font color=\"#000099\"><u>" + text + "</u></font></html>");
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorderPainted(false);
		this.setOpaque(false);
		this.setBackground(Color.WHITE);
		this.setToolTipText(uri.toString());
		this.setMargin(new Insets(0, 0, 0, 0));
		this.addActionListener((event) -> {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(uri);
				} catch (IOException e) {
					System.err.println("Could not launch browser");
				}
			}
		});
	}

}
