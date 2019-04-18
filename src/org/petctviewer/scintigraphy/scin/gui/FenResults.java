package org.petctviewer.scintigraphy.scin.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.petctviewer.scintigraphy.scin.ModeleScin;

/**
 * Class that represent a result window for a study.
 * 
 * @author Titouan QUÉMA
 *
 */
public class FenResults extends JFrame {
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabPane;
	private List<TabResult> tabs;

	private ModeleScin model;

	public FenResults(ModeleScin model) {
		this.setLocationRelativeTo(null);
		this.setTitle("Result for " + model.getStudyName());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.model = model;

		this.tabPane = new JTabbedPane();
		this.tabs = new ArrayList<>();

		this.add(tabPane);

		this.setVisible(true);
	}

	public ModeleScin getModel() {
		return this.model;
	}

	/**
	 * Adds a new tab to be displayed.
	 * 
	 * @param tab Tab to add
	 */
	public void addTab(TabResult tab) {
		this.tabPane.addTab(tab.getTitle(), tab.getPanel());
		this.tabs.add(tab);
		this.pack();
	}

	public TabResult getTab(int index) {
		if (index < 0 || index >= this.tabs.size())
			return null;
		return this.tabs.get(index);
	}

	public TabResult getMainTab() {
		if (this.tabs.size() == 0)
			return null;
		return this.tabs.get(0);
	}

	public void setMainTab(TabResult tab) {
		if (this.tabs.size() == 0)
			this.tabs.add(tab);
		else {
			this.tabs.set(0, tab);
			this.tabPane.removeTabAt(0);
		}
		this.tabPane.insertTab(tab.getTitle(), null, tab.getPanel(), null, 0);
	}

	public void removeTab(TabResult tabToRemove) {
		int indexToRemove = this.tabs.indexOf(tabToRemove);
		if (indexToRemove >= 0) {
			this.tabs.remove(indexToRemove);
			this.tabPane.remove(indexToRemove);
		}
	}

}
