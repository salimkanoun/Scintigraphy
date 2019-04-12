package org.petctviewer.scintigraphy.shunpo;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FenResults extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Font FONT_TITLE = new Font("Arial", Font.PLAIN, 25);

	private JLabel resultTitle, studyName;

	private JPanel panCenter, panResult, panEast;

	public FenResults() {
		this("Untitiled", "Study Unknown");
	}

	public FenResults(String resultTitle, String studyName) {
		this.setLocationRelativeTo(null);
		this.setTitle("Result for " + studyName);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.getContentPane().setLayout(new BorderLayout());

		// Center
		this.panCenter = new JPanel(new BorderLayout());
		this.panCenter.setAlignmentX(JPanel.CENTER_ALIGNMENT);

		this.resultTitle = new JLabel(resultTitle);
		this.resultTitle.setHorizontalAlignment(JLabel.CENTER);
		this.resultTitle.setFont(FONT_TITLE);
		panCenter.add(this.resultTitle, BorderLayout.NORTH);

		this.panResult = new JPanel();
		this.panCenter.add(panResult, BorderLayout.CENTER);

		// East
		this.panEast = new JPanel();

		// Add components
		this.getContentPane().add(panCenter, BorderLayout.CENTER);
		this.getContentPane().add(panEast, BorderLayout.EAST);
		this.pack();
	}

	public void setResultTitle(String title) {
		this.resultTitle.setText(title);
		this.pack();
	}

	public void setStudyName(String studyName) {
		this.studyName.setText(studyName);
		this.pack();
	}

	public void setResult(JPanel panel) {
		this.panCenter.remove(this.panResult);
		this.panCenter.add(panel, BorderLayout.CENTER);
		this.pack();
	}
	
	public void setInfos(Component component) {
		this.panEast.removeAll();
		this.panEast.add(component);
		this.pack();
	}

}
