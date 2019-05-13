package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.petctviewer.scintigraphy.scin.ControllerWorkflow;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptDialog;

public class PromptBkgNoise extends PromptDialog {
	private static final long serialVersionUID = 1L;

	private JRadioButton rbAntre_yes, rbAntre_no, rbIntestine_yes, rbIntestine_no;

	private JPanel panCenter;
	private JPanel panAntre, panIntestine;

	private int indexState;
	private Map<Integer, boolean[]> states;
	private boolean[] newlySelected;

	public PromptBkgNoise(ControllerWorkflow controller) {
		this.states = new HashMap<>();
		this.indexState = 0;
		this.newlySelected = new boolean[2];

		// State 0
		this.states.put(-1, new boolean[2]);

		JPanel panel = new JPanel(new BorderLayout());

		panCenter = new JPanel(new GridLayout(0, 1));
		panAntre = new JPanel();
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

		panIntestine = new JPanel();
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
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInputValid()) {
					setVisible(false);
					saveState();
				}
			}
		});

		panel.add(panCenter, BorderLayout.CENTER);
		panel.add(okBtn, BorderLayout.SOUTH);
		panel.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")), BorderLayout.WEST);

		this.getContentPane().add(panel);
		this.pack();
		this.setLocationRelativeTo(controller.getVue());
	}

	public boolean shouldBeDisplayed() {
		return true;
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
		this.states.put(this.indexState, this.getResult());
		boolean[] previousState = this.states.get(this.indexState - 1);
		if (previousState != null) {
			for (int i = 0; i < 2; i++) {
				this.newlySelected[i] = !previousState[i] && this.getResult()[i];
			}
		}
		this.indexState++;
	}

	@Override
	public boolean[] getResult() {
		return new boolean[] { this.rbAntre_yes.isSelected(), this.rbIntestine_yes.isSelected() };
	}

	@Override
	public boolean isInputValid() {
		return true;
	}

	@Override
	protected void prepareAsPrevious() {
		this.indexState--;
		// Restore state
		this.restoreState(this.states.get(this.indexState));
	}

}
