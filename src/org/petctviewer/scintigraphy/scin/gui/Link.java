package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 * This class represents a link that can be clicked on.
 *
 * @author Titouan QUÉMA
 */
public class Link extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;

	private URI uri;

	public Link() {
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorderPainted(false);
		this.setOpaque(false);
		this.setBackground(Color.WHITE);
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public Link(String text, URI uri) {
		this();
		this.setText(text);
		this.setUri(uri);
	}

	@Override
	public void setText(String text) {
		super.setText("<html><font color=\"#000099\"><u>" + text + "</u></font></html>");
	}

	public void setUri(URI uri) {
		if (uri == null) {
			// Remove previous URI
			this.setToolTipText(null);
			this.removeActionListener(this);
		} else {
			this.uri = uri;
			this.setToolTipText(uri.toString());
			this.addActionListener(this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				System.err.println("Could not launch browser");
			}
		}
	}
}
