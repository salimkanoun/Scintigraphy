package org.petctviewer.scintigraphy.shunpo;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.petctviewer.scintigraphy.scin.gui.SidePanel;

public abstract class TabResult {

	private String title;
	protected FenResults parent;

	private JPanel panel;

	private SidePanel sidePanel;
	private JPanel result;

	public TabResult(FenResults parent, String title, boolean captureBtn) {
		this(parent, title);
		if (captureBtn)
			this.createCaptureButton();
	}

	public TabResult(FenResults parent, String title) {
		this.title = title;
		this.parent = parent;

		Component content = this.getSidePanelContent() == null ? new JPanel() : this.getSidePanelContent();
		this.result = this.getResultContent() == null ? new JPanel() : this.getResultContent();

		this.sidePanel = new SidePanel(content, parent.getModel().getStudyName(),
				parent.getModel().getImagePlus());

		this.panel = new JPanel(new BorderLayout());
		this.panel.add(this.sidePanel, BorderLayout.EAST);
		this.panel.add(this.result, BorderLayout.CENTER);
	}

	/**
	 * Creates the complement of information displayed in the SidePanel.
	 * 
	 * @return Complement of information for the result
	 */
	public abstract Component getSidePanelContent();

	/**
	 * Creates the result of the analysis.
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

	public JPanel getPanel() {
		return this.panel;
	}

	public void setSidePanelContent(Component content) {
		this.sidePanel.setSidePanelContent(content);
	}
	
	public void reloadDisplay() {
		// Side panel
		Component content = this.getSidePanelContent() == null ? new JPanel() : this.getSidePanelContent();
		this.sidePanel.setSidePanelContent(content);
		
		// Result
		if(this.result != null)
			this.panel.remove(this.result);
		this.result = this.getResultContent() == null ? new JPanel() : this.getResultContent();
		this.panel.add(this.result, BorderLayout.CENTER);
		this.parent.pack();
	}

}
