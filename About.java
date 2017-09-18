/**
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

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ij.plugin.PlugIn;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;

public class About extends JDialog implements PlugIn {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel panel;
	private JPanel PanelBas;
	private JButton btnOk;
	private JTextField txtAbout;
	private JScrollPane scrollPane_1;
	private JTable table;
	private JPanel About;
	private JPanel Logo;
	private JPanel CHUToulouse;
	private JPanel IUTInformatique;
	private JPanel ImageJ;
	private JPanel Oncopole;
	private JTextPane txtpnGeneralPublicLicence;
	private JTextPane txtpnGeneralPublicLicence_1;
	private JPanel panel_1;
	private JPanel panel_2;
	private JTextPane txtpnProjectLeaders;
	private JTextPane txtpnGerardVictorPierre;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			About dialog = new About();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setBounds(100, 100, 545, 402);
		getContentPane().setLayout(new GridLayout(3, 1, 0, 0));
		{
			panel = new JPanel();
			getContentPane().add(panel);
			panel.setLayout(new GridLayout(2, 1, 0, 0));
			{
				About = new JPanel();
				panel.add(About);
				About.setLayout(new GridLayout(0, 1, 0, 0));
				{
					txtAbout = new JTextField();
					txtAbout.setFont(new Font("Tahoma", Font.BOLD, 16));
					txtAbout.setEditable(false);
					About.add(txtAbout);
					txtAbout.setBackground(Color.LIGHT_GRAY);
					txtAbout.setHorizontalAlignment(SwingConstants.CENTER);
					txtAbout.setText("About us...");
					txtAbout.setColumns(5);
				}
			}
			{
				Logo = new JPanel();
				panel.add(Logo);
				Logo.setLayout(new GridLayout(1, 0, 0, 0));
				{
					CHUToulouse = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/logo_chu_toulouse.jpeg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 167, 62);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						CHUToulouse.add(picLabel);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					Logo.add(CHUToulouse);
				}
				{
					IUTInformatique = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/IUT_Toulouse.jpg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 112, 70);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						IUTInformatique.add(picLabel);
					} catch (IOException e) {
						e.printStackTrace();
					}
					Logo.add(IUTInformatique);
				}
				{
					Oncopole = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/Institut-Claudius-Regaud.jpg"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 187, 64);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						Oncopole.add(picLabel);
					} catch (IOException e) {e.printStackTrace();}
					Logo.add(Oncopole);
				}
				{
					ImageJ = new JPanel();
					BufferedImage myPicture;
					try {
						myPicture = ImageIO.read(getClass().getResource("/logos/ImageJ.png"));
						BufferedImage myPicture2=(BufferedImage) this.scale(myPicture, 64, 64);
						JLabel picLabel = new JLabel(new ImageIcon(myPicture2));
						ImageJ.add(picLabel);
					} catch (IOException e) {e.printStackTrace();}
					Logo.add(ImageJ);
				}
				
			}
		}
		{
		}
		{
			scrollPane_1 = new JScrollPane();
			getContentPane().add(scrollPane_1);
			{
				table = new JTable();
				table.setModel(new DefaultTableModel(
					new Object[][] {
						{"Pulmonary Shunt", "Gerard Victor", "Mathis Mohand", "CHU Toulouse"},
						{"Gastric Emptying", "Gerard Victor", "Ping Xie", "CHU Toulouse"},
					},
					new String[] {
						"Software", "Creator", "Developper", "Institution"
					}
				));
				scrollPane_1.setViewportView(table);
			}
		}
		{
			PanelBas = new JPanel();
			getContentPane().add(PanelBas);
			PanelBas.setLayout(new GridLayout(3, 1, 0, 0));
			{
			}
			{
				panel_2 = new JPanel();
				PanelBas.add(panel_2);
				panel_2.setLayout(new GridLayout(0, 2, 0, 0));
				{
					txtpnProjectLeaders = new JTextPane();
					txtpnProjectLeaders.setBackground(Color.LIGHT_GRAY);
					panel_2.add(txtpnProjectLeaders);
					txtpnProjectLeaders.setText("Project Leaders :");
				}
				{
					txtpnGeneralPublicLicence_1 = new JTextPane();
					panel_2.add(txtpnGeneralPublicLicence_1);
					txtpnGeneralPublicLicence_1.setBackground(Color.LIGHT_GRAY);
					txtpnGeneralPublicLicence_1.setText("General Public Licence v.3");
				}
			}
			btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
			{
				panel_1 = new JPanel();
				PanelBas.add(panel_1);
				panel_1.setLayout(new GridLayout(0, 2, 0, 0));
				{
					txtpnGerardVictorPierre = new JTextPane();
					txtpnGerardVictorPierre.setBackground(Color.LIGHT_GRAY);
					panel_1.add(txtpnGerardVictorPierre);
					txtpnGerardVictorPierre.setText("Gerard Victor, Pierre Pascal, Olivier Morel\r\nSalim Kanoun, Ilan Tal");
				}
				{
					txtpnGeneralPublicLicence = new JTextPane();
					panel_1.add(txtpnGeneralPublicLicence);
					txtpnGeneralPublicLicence.setFont(new Font("Tahoma", Font.BOLD, 15));
					txtpnGeneralPublicLicence.setBackground(Color.LIGHT_GRAY);
					txtpnGeneralPublicLicence.setText(" http://petctviewer.org\r\n");
				}
			}
			PanelBas.add(btnOk);
		}
	}
	
	private Image scale(Image source, int width, int height) { 
	    /* On crée une nouvelle image aux bonnes dimensions. */ 
	    BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
	  
	    /* On dessine sur le Graphics de l'image bufferisée. */ 
	    Graphics2D g = buf.createGraphics(); 
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
	    g.drawImage(source, 0, 0, width, height, null); 
	    g.dispose(); 
	  
	    /* On retourne l'image bufferisée, qui est une image. */ 
	    return buf; 
	}
	
	@Override
	public void run(String arg0) {
		About dialog = new About();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		dialog.setResizable(false);
		dialog.setSize(800,500);
		
	}

}
