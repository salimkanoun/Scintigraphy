package org.petctviewer.scintigraphy.scin.events;

import org.petctviewer.scintigraphy.scin.gui.FitPanel;
import org.petctviewer.scintigraphy.scin.model.Fit;

import javax.swing.event.ChangeEvent;

public class FitChangeEvent extends ChangeEvent {

	private static final long serialVersionUID = 1L;
	private Fit fit;

	/**
	 * Constructs a FitChangeEvent object.
	 *
	 * @param source the Panel that is the source of the event (typically <code>this</code>)
	 */
	public FitChangeEvent(FitPanel source, Fit fit) {
		super(source);
		this.fit = fit;
	}

	public Fit getChangedFit() {
		return this.fit;
	}
}
