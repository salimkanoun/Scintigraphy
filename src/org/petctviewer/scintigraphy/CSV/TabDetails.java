package org.petctviewer.scintigraphy.CSV;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class TabDetails extends JPanel{
	
	
	
	//contient les tableaux de chaque patient
	private ArrayList<HashMap<String, Double[][]>> tableaux;
	private String[] dateExamen;
	
	public TabDetails(ArrayList<String> chemins) {
		Controleur_FollowUp_TabDetails controleurTabDetails = new Controleur_FollowUp_TabDetails(this,chemins);

		

		this.setLayout(new FlowLayout());
		Box allResultats = Box.createHorizontalBox();
		
		
		for(int i=0; i<tableaux.size(); i++) {
			
			//ArrayList<String> cleTableaux = new ArrayList<>(tableaux.get(clePatient.get(i)).keySet());			
			
			
			Box resultats =  Box.createVerticalBox();
			
		
			JLabel date = new JLabel(dateExamen[i]);
	        date.setAlignmentX(CENTER_ALIGNMENT);
			
			date.setFont(new Font("Helvetica", Font.PLAIN, 18));
			resultats.add(date);
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));// add space

			resultats.add(setIntegralTab(tableaux.get(i).get("integral")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setTimingTab(tableaux.get(i).get("timing")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));
			
			resultats.add(setExcretionRatioTab(tableaux.get(i).get("excretion")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setRoeTab(tableaux.get(i).get("roe")));
			resultats.add(Box.createRigidArea(new Dimension(0, 30)));

			resultats.add(setNoraTab(tableaux.get(i).get("nora")));
			
			//pour que tableau ne soit pas etalé sur toute la fenetre
			JPanel jp = new JPanel(new FlowLayout());
			jp.add(resultats);
			
			allResultats.add(jp);
	
		}
		this.add(allResultats);
		
	}
	
	private Box setNoraTab(Double[][] nora) {
		JPanel noraTabPanel = new JPanel(new GridLayout(4,3,10,5));
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
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(noraTabPanel);
		
		Box noraBox = Box.createVerticalBox();
		JLabel title = new JLabel("NORA");
        title.setAlignmentX(CENTER_ALIGNMENT);
		noraBox.add(title);
		noraBox.add(jp);

		return noraBox;
	}
	

	private Box setExcretionRatioTab(Double[][] excr) {
		JPanel excrTabPanel = new JPanel(new GridLayout(4, 3,10,5));
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
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(excrTabPanel);
		
		Box excrBox = Box.createVerticalBox();
		JLabel title = new JLabel("Excretion ratio");
        title.setAlignmentX(CENTER_ALIGNMENT);
		excrBox.add(title);
		excrBox.add(jp);
		
		return excrBox;
	}
	
	private Box setTimingTab(Double[][] timing) {
		JPanel timingTabPanel = new JPanel(new GridLayout(3, 3,0,5));
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
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(timingTabPanel);
		
		Box timingBox = Box.createVerticalBox();
		JLabel title = new JLabel("Timing");
        title.setAlignmentX(CENTER_ALIGNMENT);
		timingBox.add(title);
		timingBox.add(jp);
		
		return timingBox;
	}
	
	private Box setRoeTab(Double[][] roe) {
		JPanel roeTabPanel = new JPanel(new GridLayout(4, 3,10,5));
		roeTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		JLabel timeLabel = new JLabel("T");
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(timeLabel);
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roeTabPanel.add(rightLabel);
		

		for (int i = 0; i < roe.length; i++) {

			roeTabPanel.add(new JLabel(roe[i][0] + "  min"));

			for (int j = 1; j < roe[i].length; j++) {
				if (roe[i][j] != null) {
					JLabel lbl_g = new JLabel(roe[i][j] + " %");
					lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
					roeTabPanel.add(lbl_g);
				} else {
					JLabel lbl_na = new JLabel("N/A");
					lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
					roeTabPanel.add(lbl_na);
				}
			}
		}		
		
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(roeTabPanel);
		
		Box roeBox = Box.createVerticalBox();
		JLabel title = new JLabel("ROE");
        title.setAlignmentX(CENTER_ALIGNMENT);
		roeBox.add(title);
		roeBox.add(jp);
		
		return roeBox;
	}
	
	private Box setIntegralTab(Double[][] integral) {
		JPanel integralTabPanel = new JPanel(new GridLayout(2, 2,10,5));
		integralTabPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		
		
		JLabel leftLabel = new JLabel("L");
		leftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		integralTabPanel.add(leftLabel);
		
		JLabel rightLabel = new JLabel("R");
		rightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		integralTabPanel.add(rightLabel);
		
		int i =0;

		for (int j = 0; j < integral[i].length; j++) {
			if (integral[i][j] != null) {
				JLabel lbl_g = new JLabel(integral[i][j] + " %");
				lbl_g.setHorizontalAlignment(SwingConstants.CENTER);
				integralTabPanel.add(lbl_g);
			} else {
				JLabel lbl_na = new JLabel("N/A");
				lbl_na.setHorizontalAlignment(SwingConstants.CENTER);
				integralTabPanel.add(lbl_na);
			}
		}
			
		//pour que tableau ne soit pas etalé sur toute la fenetre
		JPanel jp = new JPanel(new FlowLayout());
		jp.add(integralTabPanel);
				
		Box integralBox = Box.createVerticalBox();
		JLabel title = new JLabel("Relative function integral");
        title.setAlignmentX(CENTER_ALIGNMENT);
		integralBox.add(title);
		integralBox.add(jp);
		
		return integralBox;
	}
	
	public void setTableaux(ArrayList<HashMap<String, Double[][]>> tableaux) {
		this.tableaux = tableaux;
	}

	public void setDateExamen(String[] dateExamen) {
		this.dateExamen = dateExamen;
	}

}
