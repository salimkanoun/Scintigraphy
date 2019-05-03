package org.petctviewer.scintigraphy.gastric_refactored.dynamic;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.petctviewer.scintigraphy.scin.ControleurScin;
import org.petctviewer.scintigraphy.scin.instructions.prompts.PromptDialog;

public class PromptBkgNoise extends PromptDialog {
	private static final long serialVersionUID = 1L;

	private JRadioButton rbAntre_yes, rbAntre_no, rbIntestine_yes, rbIntestine_no;

	private JPanel panCenter;
	private JPanel panAntre, panIntestine;

	private boolean[] previousState;

	public PromptBkgNoise(ControleurScin controller) {
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
					controller.clicSuivant();
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

	private void reloadDisplay() {
		if (this.previousState[0])
			this.panAntre.setVisible(false);
		else
			this.panAntre.setVisible(true);

		if (this.previousState[1])
			this.panIntestine.setVisible(false);
		else
			this.panIntestine.setVisible(true);
	}

	public boolean shouldBeDisplayed() {
		return !(this.getResult()[0] && this.getResult()[1]);
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
	protected void prepareAsNext() {
		this.previousState = this.getResult();
		this.reloadDisplay();
	}

	@Override
	protected void prepareAsPrevious() {
		if (this.previousState != null) {
			this.reloadDisplay();
		}
	}

}