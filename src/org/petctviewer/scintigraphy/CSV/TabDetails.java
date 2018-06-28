package org.petctviewer.scintigraphy.CSV;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TabDetails extends JPanel{
	
	
	
	//contient les tableaux de chaque patient
	private ArrayList<HashMap<String, Double[][]>> tableaux;
	
	public TabDetails(ArrayList<String> chemins) {
		Controleur_FollowUp_TabDetails controleurTabDetails = new Controleur_FollowUp_TabDetails(this,chemins);

		

		this.setLayout(new GridLayout(1,tableaux.size()));
		
		
		
		for(int i=0; i<tableaux.size(); i++) {
			
			//ArrayList<String> cleTableaux = new ArrayList<>(tableaux.get(clePatient.get(i)).keySet());			
			
			
			JPanel resultats = new JPanel(new GridLayout(6, 1));
			
			
			resultats.add(setNoraTab(tableaux.get(i).get("nora")));
			resultats.add(setExcretionRatioTab(tableaux.get(i).get("excretion")));
			resultats.add(setTimingTab(tableaux.get(i).get("timing")));
			
			this.add(resultats);
		}
		
	}
	
	private Box setNoraTab(Double[][] nora) {
		JPanel noraTabPanel = new JPanel(new GridLayout(4,3));
		noraTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noraTabPanel.add(rightLabel);
		
		for (int i = 0; i < nora.length; i++) {

			noraTabPanel.add(new JLabel(nora[i][0] + "  min"));

			for (int j = 1; j < nora[i].length; j++) {
				if (nora[i][j] != null) {
					JLabel lbl_g = new JLabel(nora[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					noraTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					noraTabPanel.add(lbl_na);
				}
			}
		}
		
		Box noraBox = Box.createVerticalBox();
		noraBox.add(new JLabel(" NORA"));
		noraBox.add(noraTabPanel);

		return noraBox;
	}

	private Box setExcretionRatioTab(Double[][] excr) {
		JPanel excrTabPanel = new JPanel(new GridLayout(4, 3));
		excrTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		excrTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		excrTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		excrTabPanel.add(rightLabel);
		
		for (int i = 0; i < excr.length; i++) {

			excrTabPanel.add(new JLabel(excr[i][0] + "  min"));

			for (int j = 1; j < excr[i].length; j++) {
				if (excr[i][j] != null) {
					JLabel lbl_g = new JLabel(excr[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					excrTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					excrTabPanel.add(lbl_na);
				}
			}
		}		
		
		Box excrBox = Box.createVerticalBox();
		excrBox.add(new JLabel(" Excretion ratio"));
		excrBox.add(excrTabPanel);
		
		return excrBox;
	}
	
	private Box setTimingTab(Double[][] timing) {
		JPanel timingTabPanel = new JPanel(new GridLayout(3, 3));
		timingTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		timingTabPanel.add(rightLabel);
		
		
		timingTabPanel.add(new JLabel("TMax (min)"));
		
		for (int i = 0; i < timing.length; i++) {

			if(i==1) {
				timingTabPanel.add(new JLabel("T1/2(min)"));
			}
			
			for (int j = 0; j < timing[i].length; j++) {
				if (timing[i][j] != null) {
					JLabel lbl_g = new JLabel(timing[i][j]+"");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					timingTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					timingTabPanel.add(lbl_na);
				}
			}
		}		
		
		Box timingBox = Box.createVerticalBox();
		timingBox.add(new JLabel(" Timing"));
		timingBox.add(timingTabPanel);
		
		return timingBox;
	}
	
	public void setTableaux(ArrayList<HashMap<String, Double[][]>> tableaux) {
		this.tableaux = tableaux;
	}
}
