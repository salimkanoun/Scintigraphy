package org.petctviewer.scintigraphy.gastric.dynamic;

import org.petctviewer.scintigraphy.scin.controller.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptDialog;

import javax.swing.*;
import java.awt.*;

public class PromptBkgNoise extends PromptDialog {
	private static final long serialVersionUID = 1L;

	private final JRadioButton rbAntre_yes;
	private final JRadioButton rbAntre_no;
	private final JRadioButton rbIntestine_yes;
	private final JRadioButton rbIntestine_no;

	private int indexState;
	private boolean forward;
	private final boolean[][] states;
	private final boolean[] newlySelected;

	public PromptBkgNoise(ControllerWorkflow controller, int nbAppearances) {
		this.states = new boolean[nbAppearances + 1][2];
		this.indexState = 1;
		this.newlySelected = new boolean[2];

		// State 0
		this.states[0] = new boolean[2];
		this.forward = true;

		JPanel panel = new JPanel(new BorderLayout());

		JPanel panCenter = new JPanel(new GridLayout(0, 1));
		JPanel panAntre = new JPanel();
		JLabel lAntre = new JLabel("Is Antre region a background noise?");
		panAntre.add(lAntre);
		ButtonGroup bgAntre = new ButtonGroup();
		this.rbAntre_no = new JRadioButton("No");
		this.rbAntre_no.setSelected(true); // selected by default
		this.rbAntre_yes = new JRadioButton("Yes");
		bgAntre.add(rbAntre_no);
		bgAntre.add(rbAntre_yes);
		panAntre.add(this.rbAntre_no);
		panAntre.add(this.rbAntre_yes);
		panCenter.add(panAntre);

		JPanel panIntestine = new JPanel();
		JLabel lIntestine = new JLabel("Is Intestine region a background noise?");
		panIntestine.add(lIntestine);
		ButtonGroup bgIntestine = new ButtonGroup();
		this.rbIntestine_no = new JRadioButton("No");
		this.rbIntestine_no.setSelected(true); // selected by default
		this.rbIntestine_yes = new JRadioButton("Yes");
		bgIntestine.add(rbIntestine_no);
		bgIntestine.add(rbIntestine_yes);
		panIntestine.add(rbIntestine_no);
		panIntestine.add(rbIntestine_yes);
		panCenter.add(panIntestine);

		JButton okBtn = new JButton("Validate");
		okBtn.addActionListener(e -> {
			setVisible(false);
			saveState();
			updateIndex();
		});

		panel.add(panCenter, BorderLayout.CENTER);
		panel.add(okBtn, BorderLayout.SOUTH);
		panel.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")), BorderLayout.WEST);

		this.getContentPane().add(panel);
		this.pack();
		this.setLocationRelativeTo(controller.getVue());
	}

	private void updateIndex() {
		if (this.forward) this.indexState++;
	}

	@Override
	public boolean shouldBeDisplayed() {
		if(this.indexState >= this.states.length)
			return false;
		boolean[] state = this.states[this.indexState];
		return !this.forward || !state[0] || !state[1];

	}

	/**
	 * @return TRUE if the Antre 'yes' button was not selected and is now selected
	 */
	public boolean antreIsNowSelected() {
		return this.newlySelected[0];
	}

	/**
	 * @return TRUE if the Intestine 'yes' button was not selected and is now
	 *         selected
	 */
	public boolean intestineIsNowSelected() {
		return this.newlySelected[1];
	}

	private void restoreState(boolean[] state) {
		this.rbAntre_no.setSelected(!state[0]);
		this.rbAntre_yes.setSelected(state[0]);

		this.rbIntestine_no.setSelected(!state[1]);
		this.rbIntestine_yes.setSelected(state[1]);
	}

	private void saveState() {
		// Save state
		this.states[this.indexState] = this.getResult();
		boolean[] previousState = this.states[this.indexState - 1];
		if (previousState != null) {
			for (int i = 0; i < 2; i++) {
				this.newlySelected[i] = !previousState[i] && this.getResult()[i];
			}
		}

//		this.DEBUG("Saved state");
	}

	@Override
	public boolean[] getResult() {
		return new boolean[] { this.rbAntre_yes.isSelected(), this.rbIntestine_yes.isSelected() };
	}

	@Override
	public boolean isInputValid() {
		return true;
	}

//	private void DEBUG(String s) {
//		System.out.println(Library_Debug.separator(0));
//		System.out.println("== " + s + " ==");
//		System.out.println("Current index = " + this.indexState);
//		System.out.println("Current states:");
//		int i = 0;
//		for (boolean[] b : this.states) {
//			if (i == this.indexState)
//				System.out.println(Library_Debug.title("State #" + i++));
//			else
//				System.out.println(Library_Debug.subtitle("State #" + i++));
//			System.out.println("\t- antre: " + b[0]);
//			System.out.println("\t- intestine: " + b[1]);
//		}
//	}

	@Override
	protected void prepareAsNext() {
		// Restore state
		this.restoreState(this.states[this.indexState-1]);
		
		// Last time this prompt will be displayed
		if (this.indexState == this.states.length - 1) {
			this.rbAntre_no.setEnabled(false);
			this.rbAntre_yes.setSelected(true);

			this.rbIntestine_no.setEnabled(false);
			this.rbIntestine_yes.setSelected(true);
		} else {
			this.rbAntre_no.setEnabled(true);

			this.rbIntestine_no.setEnabled(true);
		}

		// Change direction
		this.forward = true;

//		this.DEBUG("Prepared as next");
	}

	@Override
	protected void prepareAsPrevious() {
		// Change direction
		this.indexState--;
		this.forward = false;

		// Restore state
		this.restoreState(this.states[this.indexState]);
		
		// Last time this prompt will be displayed
		if (this.indexState == this.states.length - 1) {
			this.rbAntre_no.setEnabled(false);
			this.rbAntre_yes.setSelected(true);

			this.rbIntestine_no.setEnabled(false);
			this.rbIntestine_yes.setSelected(true);
		} else {
			this.rbAntre_no.setEnabled(true);

			this.rbIntestine_no.setEnabled(true);
		}

//		this.DEBUG("Prepared as previous");
	}

}
