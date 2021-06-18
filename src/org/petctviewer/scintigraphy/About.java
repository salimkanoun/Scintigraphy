/*
Copyright (C) 2017 MOHAND Mathis and KANOUN Salim
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.scintigraphy;

import ij.plugin.PlugIn;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class About extends JDialog implements PlugIn {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		try {
			About dialog = new About();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.setSize(800,500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public About() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setBounds(100, 100, 545, 402);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel about = new JPanel();
				panel.add(about, BorderLayout.NORTH);
				about.setLayout(new GridLayout(0, 1, 0, 0));
				{
					JTextField txtAbout = new JTextField();
					txtAbout.setFont(new Font("Tahoma", Font.BOLD, 16));
					txtAbout.setEditable(false);
					about.add(txtAbout);
					txtAbout.setBackground(Color.LIGHT_GRAY);
					txtAbout.setHorizontalAlignment(SwingConstants.CENTER);
					txtAbout.setText("About us...");
					txtAbout.setColumns(5);
				}
			}
			{
				JPanel logo = new JPanel();
				panel.add(logo);
				{
					JPanel CHUToulouse = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/logo_chu_toulouse.jpeg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 167, 62);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						CHUToulouse.add(picLabel);
					} catch (IOException e) {
						e.printStackTrace();
					}
					logo.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
					
					logo.add(CHUToulouse);
				}
				{
					JPanel IUTInformatique = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/IUT_Toulouse.jpg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 112, 70);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						IUTInformatique.add(picLabel);
					} catch (IOException e) {
						e.printStackTrace();
					}
					logo.add(IUTInformatique);
				}
				{
					JPanel oncopole = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/Institut-Claudius-Regaud.jpg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 187, 64);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						oncopole.add(picLabel);
					} catch (IOException e) {e.printStackTrace();}
					logo.add(oncopole);
				}
				{
					JPanel imageJ = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/ImageJ.png"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 64, 64);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						imageJ.add(picLabel);
					} catch (IOException e) {e.printStackTrace();}
					logo.add(imageJ);
				}
				
			}
		}
		{
			JScrollPane scrollPane_1 = new JScrollPane();
			getContentPane().add(scrollPane_1, BorderLayout.CENTER);
			{
				JTable table = new JTable();
				table.setModel(new DefaultTableModel(
					new Object[][] {
						{"Pulmonary Shunt", "Gerard Victor", "Titouan Quema, Mathis Mohand", "CHU Toulouse"},
						{"Gastric Emptying", "Gerard Victor, Pierre Pascal", "Titouan Quema, Ping Xie", "CHU Toulouse"},
						{"DPD Quant", "Gerard Victor", "Ruben Gres", "CHU Toulouse"},
						{"Biliary Scintigraphy", "Gerard Victor", "Esteban Baicho, Ruben Gres", "CHU Toulouse"},
						{"Renogram", "Gerard Victor, Salim Kanoun, Pierre Pascal", "Ruben Gres", "CHU Toulouse"},
						{"DMSA", "Gerard Victor", "Ruben Gres", "CHU Toulouse"},
						{"Renogram Follow Up", "Alina Berriolo", "Diego Romero", "CHU Toulouse"},
						{"Dynamic Quantification", "Salim Kanoun", "Esteban Baicho, Ruben Gres", "CHU Toulouse"},
						{"Static Quantifiication", "Salim Kanoun", "Esteban Baicho, Diego Romero", "CHU Toulouse"},
						{"Schaefer Calibration", "Salim Kanoun", "Diego Romero", "CHU Toulouse"},
						{"Esophageal Transit", "Pierre Pascal, Gerard Victor", "Diego Romero", "CHU Toulouse"},
						{"Bone Scintigraphy", "Salim Kanoun", "Esteban Baicho", "IUCT Oncopole"},
						{"Lymphoscintigraphy", "Pierre Pascal", "Esteban Baicho", "CHU Toulouse"},
						{"Liver Function", "Pierre Pascal", "Lou-Anne Costes, Axel Metzinger", "CHU Toulouse"},
						{"Gallbladder Scintigraphy", "Pierre Pascal", "Lou-Anne Costes, Axel Metzinger", "CHU Toulouse"},
						{"Salivary Glands Scintigraphy", "Pierre Pascal", "Lou-Anne Costes, Axel Metzinger", "CHU Toulouse"},
					},
					new String[] {
						"Software", "Creator", "Developer", "Institution"
					}
				));
				table.getColumnModel().getColumn(0).setPreferredWidth(148);
				table.getColumnModel().getColumn(2).setPreferredWidth(136);
				scrollPane_1.setViewportView(table);
			}
		}
		{
			JPanel panelBas = new JPanel();
			getContentPane().add(panelBas, BorderLayout.SOUTH);
			panelBas.setLayout(new GridLayout(3, 1, 0, 0));
			{
				JPanel panel_2 = new JPanel();
				panelBas.add(panel_2);
				panel_2.setLayout(new GridLayout(0, 2, 0, 0));
				{
					JTextPane txtpnProjectLeaders = new JTextPane();
					txtpnProjectLeaders.setBackground(Color.LIGHT_GRAY);
					panel_2.add(txtpnProjectLeaders);
					txtpnProjectLeaders.setText("Project Leaders :");
				}
				{
					JTextPane txtpnGeneralPublicLicence_1 = new JTextPane();
					panel_2.add(txtpnGeneralPublicLicence_1);
					txtpnGeneralPublicLicence_1.setBackground(Color.LIGHT_GRAY);
					txtpnGeneralPublicLicence_1.setText("General Public Licence v.3");
				}
			}
			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(arg0 -> dispose());
			{
				JPanel panel_1 = new JPanel();
				panelBas.add(panel_1);
				panel_1.setLayout(new GridLayout(0, 2, 0, 0));
				{
					JTextPane txtpnGerardVictorPierre = new JTextPane();
					txtpnGerardVictorPierre.setBackground(Color.LIGHT_GRAY);
					panel_1.add(txtpnGerardVictorPierre);
					txtpnGerardVictorPierre.setText("Salim Kanoun, Gerard Victor, Pierre Pascal,\r\nAlina Berriolo-Riedinger, Olivier Morel, Ilan Tal");
				}
				{
					JTextPane txtpnGeneralPublicLicence = new JTextPane();
					panel_1.add(txtpnGeneralPublicLicence);
					txtpnGeneralPublicLicence.setFont(new Font("Tahoma", Font.BOLD, 15));
					txtpnGeneralPublicLicence.setBackground(Color.LIGHT_GRAY);
					txtpnGeneralPublicLicence.setText(" http://petctviewer.org\r\n");
				}
			}
			panelBas.add(btnOk);
		}
	}
	
	private Image scale(Image source, int width, int height) { 
	    /* On cr�e une nouvelle image aux bonnes dimensions. */ 
	    BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
	  
	    /* On dessine sur le Graphics de l'image bufferis�e. */ 
	    Graphics2D g = buf.createGraphics(); 
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
	    g.drawImage(source, 0, 0, width, height, null); 
	    g.dispose(); 
	  
	    /* On retourne l'image bufferis�e, qui est une image. */ 
	    return buf; 
	}
	
	@Override
	public void run(String arg0) {
		About dialog = new About();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		dialog.setResizable(false);
		dialog.setSize(800,500);
		
	}

}
