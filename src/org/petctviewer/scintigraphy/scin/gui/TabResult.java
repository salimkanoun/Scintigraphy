package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;

public abstract class TabResult {

	private final JSplitPane split;
	private final SidePanel sidePanel;
	protected FenResults parent;
	private String title;
	private Container result;
	private Component[] componentToHide;
	private Component[] componentToShow;
	private String additionalInfo;

	/**
	 * Instantiate a new tab.<br>
	 * <br>
	 * <i><b>Be careful:</b></i> This method do NOT call the
	 * {@link #getResultContent()} and {@link #getSidePanelContent()} methods. You
	 * need to call the {@link #reloadDisplay()} when you are ready to display them.
	 *
	 * @param parent     FenResults where this tab is placed on
	 * @param title      Title of this tab, displayed on the JTabbedPane's title bar
	 * @param captureBtn TRUE to create a default capture button and FALSE to create a tab
	 *                   without a capture button
	 */
	public TabResult(FenResults parent, String title, boolean captureBtn) {
		this(parent, title);
		if (captureBtn) this.createCaptureButton();
	}

	/**
	 * Instantiate a new tab with no capture button. This method is equivalent to
	 * TabResult(parent, title, false).<br>
	 * <br>
	 * <i><b>Be careful:</b></i> This method do NOT call the
	 * {@link #getResultContent()} and {@link #getSidePanelContent()} methods. You
	 * need to call the {@link #reloadDisplay()} when you are ready to display them.
	 *
	 * @param parent FenResults where this tab is placed on
	 * @param title  Title of this tab, displayed on the JTabbedPane's title bar
	 */
	public TabResult(FenResults parent, String title) {
		this.title = title;
		this.parent = parent;
		this.result = new JPanel();
		this.componentToHide = new Component[0];
		this.componentToShow = new Component[0];
		this.additionalInfo = "";

		this.sidePanel = new SidePanel(null, parent.getModel().getStudyName(), parent.getModel().getImagePlus());

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.result, this.sidePanel);
		this.split.setResizeWeight(1.);
	}

	/**
	 * Creates the complement of information displayed in the SidePanel.<br>
	 * This method is called BEFORE {@link #getResultContent()}.
	 *
	 * @return Complement of information for the result
	 */
	public abstract Component getSidePanelContent();

	/**
	 * Creates the result of the analysis.<br>
	 * This method is called AFTER {@link #getSidePanelContent()}.
	 *
	 * @return displayable result
	 */
	public abstract Container getResultContent();

	/**
	 * Title of this tab. This title should be displayed on the JTabbedPane title.
	 *
	 * @return title of this tab
	 */
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return FenResults where this tab is placed on
	 */
	public FenResults getParent() {
		return this.parent;
	}

	/**
	 * Creates the button to take a capture of this tab.
	 */
	public void createCaptureButton() {
		this.sidePanel.createCaptureButton(this);
	}

	/**
	 * Do not use this method to change directly the content of the panel.<br>
	 * If you need to change the content of any panel, please do that in the
	 * abstract methods {@link #getSidePanelContent()} and
	 * {@link #getResultContent()}. <br>
	 * This method is only designed to be used by the capture tool.
	 *
	 * @return Panel containing the result panel and the side panel
	 */
	public Container getPanel() {
		return this.split;
	}

	/**
	 * This method needs to be called each time the result panel and the side panel
	 * content has to be refreshed.<br>
	 * <p>
	 * For instance, if you need to change the model and then display the result
	 * again, you can use this method, it will automatically call the
	 * getSidePanelContent and the getResultContent methods.
	 */
	public void reloadDisplay() {
		this.reloadSidePanelContent();
		this.reloadResultContent();
	}

	/**
	 * This method reloads only the side panel of this tab.
	 *
	 * @see TabResult#reloadDisplay()
	 */
	public void reloadSidePanelContent() {
		Component sidePanelContent = this.getSidePanelContent();
		Component content = sidePanelContent == null ? new JPanel() : sidePanelContent;
		JScrollPane scroll = new JScrollPane(content);
		scroll.setBorder(null);
		this.sidePanel.setSidePanelContent(scroll);
	}

	/**
	 * This method reloads only the result panel of this tab.
	 *
	 * @see TabResult#reloadDisplay()
	 */
	public void reloadResultContent() {
		this.result = this.getResultContent();
		// Prevent from null
		if (this.result == null) this.result = new JPanel();

		// Remove previous result and add new one
		this.split.setLeftComponent(this.result);

		this.split.setMaximumSize(new Dimension(1024, 768));
	}

	/**
	 * Changes the title of the side panel.
	 *
	 * @param sidePanelTitle New title to display
	 */
	public void setSidePanelTitle(String sidePanelTitle) {
		this.sidePanel.setTitle(sidePanelTitle);
	}

	/**
	 * Return the component to hide in the TabResult on the capture
	 *
	 * @see CaptureButton
	 */
	public Component[] getComponentToHide() {
		return this.componentToHide;
	}

	/**
	 * Set the components to hide on the capture
	 *
	 * @see CaptureButton
	 */
	public void setComponentToHide(Component[] componentToHide) {
		this.componentToHide = componentToHide;
	}

	/**
	 * Return the component to show in the TabResult on the capture
	 *
	 * @see CaptureButton
	 */
	public Component[] getComponentToShow() {
		return this.componentToShow;
	}

	/**
	 * Set the components to show on the capture
	 *
	 * @see CaptureButton
	 */
	public void setComponentToShow(Component[] componentToShow) {
		this.componentToShow = componentToShow;
	}

	/**
	 * Get the additionalInfo for the capture
	 *
	 * @see CaptureButton
	 */
	public String getAdditionalInfo() {
		return this.additionalInfo;
	}

	/**
	 * Set the additionalInfo for the capture
	 *
	 * @see CaptureButton
	 */
	public void setadditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	public CaptureButton getCaptureButton() {
		return this.sidePanel.getCaptureButton();
	}

}
