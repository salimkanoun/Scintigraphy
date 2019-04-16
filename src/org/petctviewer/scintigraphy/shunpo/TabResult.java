package org.petctviewer.scintigraphy.shunpo;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public abstract class TabResult extends JPanel {
	private static final long serialVersionUID = 1L;

	private String title;
	protected FenResults parent;

	private JPanel result;
	private SidePanel sidePanel;
	
	public TabResult(FenResults parent, String title, boolean captureBtn) {
		this(parent, title);
		if(captureBtn)
			this.createCaptureButton();
	}

	public TabResult(FenResults parent, String title) {
		this.title = title;
		this.parent = parent;

		this.result = this.getResultContent();
		this.sidePanel = new SidePanel(this.getSidePanelContent(), parent.getModel().getStudyName(),
				parent.getModel().getImagePlus());

		this.setLayout(new BorderLayout());

		this.add(this.result, BorderLayout.CENTER);
		this.add(this.sidePanel, BorderLayout.EAST);
	}

	/**
	 * Creates the complement of information displayed in the SidePanel.
	 * 
	 * @return Complement of information for the result
	 */
	public abstract Component getSidePanelContent();

	/**
	 * Creates the result.
	 * 
	 * @return Result
	 */
	public abstract JPanel getResultContent();

	public String getTitle() {
		return this.title;
	}

	public FenResults getParent() {
		return this.parent;
	}
	
	public void createCaptureButton() {
		this.sidePanel.createCaptureButton(this);
	}

}
