package org.petctviewer.scintigraphy.scin.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WindowDifferentPatient extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	public WindowDifferentPatient(Object[][] difference) {

		this.setTitle("Multiple patient");

		JPanel flow = new JPanel(new GridLayout(4, 1));

		flow.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		flow.add(this.add(new JLabel("There is more than one patient ID")));

		JPanel panel = new JPanel(new BorderLayout());

		JTable differences = new JTable(difference, new String[] { "Conflict", "From Json"," Current patient" });

		panel.add(differences.getTableHeader(), BorderLayout.NORTH);
		panel.add(differences, BorderLayout.CENTER);
		flow.add(panel);

		flow.add(this.add(new JLabel("Do you want to still process the exam?")));

		JPanel radio = new JPanel();
		JButton btn_y = new JButton("Yes");
		btn_y.addActionListener(this);
		radio.add(btn_y);
		JButton btn_n = new JButton("No");
		btn_n.addActionListener(this);
		radio.add(btn_n);

		flow.add(radio);

		this.add(flow);

		this.setLocationRelativeTo(null);

		this.pack();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dispose();
	}

}
