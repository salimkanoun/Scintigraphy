package org.petctviewer.scintigraphy.scin.gui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.petctviewer.scintigraphy.scin.controller.ControllerScin;
import org.petctviewer.scintigraphy.scin.model.ModelScin;

/**
 * Class that represent a result window for a study.
 * 
 * @author Titouan QUÉMA
 *
 */
public class FenResults extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JTabbedPane tabPane;
	private final List<TabResult> tabsResult;

	private final ControllerScin controller;

	public FenResults(ControllerScin controller) {
		this.setTitle("Result for " + controller.getModel().getStudyName());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.controller = controller;

		this.tabPane = new JTabbedPane();
		this.tabsResult = new ArrayList<>();

		this.add(tabPane);
	}

	public ModelScin getModel() {
		return this.controller.getModel();
	}

	public ControllerScin getController() {
		return this.controller;
	}

	/**
	 * Adds a new tab to be displayed.
	 * 
	 * @param tab Tab to add
	 */
	public void addTab(TabResult tab) {
		this.tabPane.addTab(tab.getTitle(), tab.getPanel());
		this.tabsResult.add(tab);
		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * @param index Index of the tab to get
	 * @return TabResult at the specified index
	 */
	public TabResult getTab(int index) {
		if (index < 0 || index >= this.tabsResult.size())
			return null;
		return this.tabsResult.get(index);
	}

	/**
	 * The main tab is the first tab displayed.
	 * 
	 * @return first tab
	 */
	public TabResult getMainTab() {
		if (this.tabsResult.size() == 0)
			return null;
		return this.tabsResult.get(0);
	}

	/**
	 * The main tab is the first tab displayed. This method replaces the main tab
	 * with the specified tab.
	 * 
	 * @param tab Tab to be displayed first
	 */
	public void setMainTab(TabResult tab) {
		if (this.tabsResult.size() == 0)
			this.tabsResult.add(tab);
		else {
			this.tabsResult.set(0, tab);
			this.tabPane.removeTabAt(0);
		}
		this.tabPane.insertTab(tab.getTitle(), null, tab.getPanel(), null, 0);
		this.pack();
		this.setLocationRelativeTo(null);
	}

	/**
	 * Removes the specified tab. If the tab doesn't exist, nothing happen.
	 * 
	 * @param tabToRemove Tab to remove
	 */
	public void removeTab(TabResult tabToRemove) {
		int indexToRemove = this.tabsResult.indexOf(tabToRemove);
		if (indexToRemove >= 0) {
			this.tabsResult.remove(indexToRemove);
			this.tabPane.remove(indexToRemove);
		}
	}

	/**
	 * Removes all the tabs.
	 */
	public void clearTabs() {
		this.tabsResult.clear();
		this.tabPane.removeAll();
	}

	/**
	 * Reloads side content and result content of all tabs.
	 */
	public void reloadAllTabs() {
		for (TabResult tab : this.tabsResult)
			tab.reloadDisplay();
	}
	
	public void setNewControllerForCaptureButton(ControllerScin controller) {
		for(TabResult tab : this.tabsResult) {
			for( ActionListener al : tab.getCaptureButton().getActionListeners() )
				tab.getCaptureButton().removeActionListener( al );
		    
			tab.getCaptureButton().addActionListener(controller);
		}
	}

}
