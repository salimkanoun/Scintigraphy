package org.petctviewer.scintigraphy.generic.dynamic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FenGroup_GeneralDyn extends JDialog {

	private static final long serialVersionUID = 145239677437316066L;
	
	private List<ChartGroup> cgs;
	private JList<String> listDroite;
	private String[] roiNames;

	/**
	 * Cette fenetre permet de choisir comment organiser ses courbes dans les differents graphiques, elle genere un tableau de String d'association
	 * @param roiNames studyName des courbes a placer dans les graphiques
	 */
	public FenGroup_GeneralDyn(String[] roiNames) {
		this.cgs = new ArrayList<>();
		this.roiNames = roiNames;

		this.setTitle("Select how you want to display the results");
		this.setLayout(new BorderLayout());

		this.listDroite = new JList<>(roiNames);
		JPanel listContainer = new JPanel(new GridLayout(1, 1));
		listContainer.setBorder(BorderFactory.createTitledBorder("Rois"));
		listContainer.setPreferredSize(new Dimension(200, 100));
		listContainer.add(this.listDroite);

		this.add(listContainer, BorderLayout.WEST);

		JPanel gridGauche = new JPanel(new GridLayout(5, 1));

		for (int i = 0; i < 5; i++) {
			ChartGroup cg = new ChartGroup(i);
			this.cgs.add(cg);

			JPanel list_btn = new JPanel();

			JPanel listContainerDroite = new JPanel(new GridLayout(1, 1));
			listContainerDroite.setBorder(BorderFactory.createTitledBorder("Chart " + (i + 1)));
			listContainerDroite.setPreferredSize(new Dimension(200, 100));
			listContainerDroite.add(cg.list);

			list_btn.add(listContainerDroite);

			JPanel btns = new JPanel(new GridLayout(2, 1, 0, 10));
			btns.add(cg.btn_plus);
			btns.add(cg.btn_moins);

			list_btn.add(btns);

			gridGauche.add(list_btn);
		}

		this.add(gridGauche, BorderLayout.EAST);

		JButton valider = new JButton("Ok");
		valider.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FenGroup_GeneralDyn.this.dispose();
			}
		});
		JPanel wrapValider = new JPanel();
		wrapValider.add(valider);
		this.add(wrapValider, BorderLayout.SOUTH);
		
		this.setResizable(false);
		this.pack();
	}

	private class ChartGroup {
		private JButton btn_plus, btn_moins;
		private JList<String> list;
		private DefaultListModel<String> model;

		public ChartGroup(int id) {
			this.btn_plus = new JButton("+");
			this.btn_moins = new JButton("-");
			this.model = new DefaultListModel<>();
			this.list = new JList<>(this.model);

			CtrlChartGroup ctrl = new CtrlChartGroup(id);
			this.btn_moins.addActionListener(ctrl);
			this.btn_plus.addActionListener(ctrl);
		}
	}

	private class CtrlChartGroup implements ActionListener {

		private int id;

		public CtrlChartGroup(int id) {
			this.id = id;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JButton b = (JButton) arg0.getSource();
			ChartGroup cg = FenGroup_GeneralDyn.this.cgs.get(this.id);

			switch (b.getText()) {
			case "+":
				String s = FenGroup_GeneralDyn.this.listDroite.getSelectedValue();
				cg.model.addElement(s);
				break;
			case "-":
				cg.model.removeElementAt(cg.list.getSelectedIndex());
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Renvoie un tableau de tableau de string correspondant a l'association choisie par l'utilisateur.
	 * Si il n'y a eu aucune association de faite, les 5 premieres courbes se repartiront dans l'ordre dans les graphes
	 * @return String[][] de la forme {{courbe1, courbe2}, {courbe3}, {courbe1, courbe2}}
	 */
	public String[][] getAssociation() {
		String[][] association;

		boolean vide = true;
		for (ChartGroup cg : this.cgs) {
			if (cg.model.size() > 0) {
				vide = false;
				break;
			}
		}

		if (vide) {
			association = new String[5][1];
			for (int i = 0; i < 5; i++) {
				if (i < this.roiNames.length) {
					association[i] = new String[] { this.roiNames[i] };
				}else {
					association[i] = new String[] {};
				}
			}
		} else {
			association = new String[5][];
			for (int i = 0; i < this.cgs.size(); i++) {
				ChartGroup cg = this.cgs.get(i);
				String[] chartRois = new String[cg.model.size()];
				for (int j = 0; j < cg.model.size(); j++) {
					chartRois[j] = cg.model.getElementAt(j);
				}
				association[i] = chartRois;
			}
		}

		return association;
	}

}
