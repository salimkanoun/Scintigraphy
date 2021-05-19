/*
Copyright (C) 2017 KANOUN Salim
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

import ij.IJ;
import ij.Menus;
import ij.gui.Toolbar;
import ij.plugin.tool.PlugInTool;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class Scintigraphy_Tool extends PlugInTool implements ActionListener {


	PopupMenu general;
		
		
		@Override
		public void showPopupMenu(MouseEvent e, Toolbar par) {
			addPopupMenu(par);
			int OFFSET = 0;
			general.show(e.getComponent(), e.getX()+ OFFSET, e.getY()+ OFFSET);
		}
		
		void addPopupMenu(Toolbar bar) {
			//On cree le popup menu general
			general = new PopupMenu();
			
			if (Menus.getFontSize()!=0) general.setFont(Menus.getFont());

			//Generic
			PopupMenu generic = new PopupMenu("Generic");
			MenuItem dynQuant = new MenuItem("Dynamic Quantification");
			MenuItem statQuant = new MenuItem("Static Quantification");
			dynQuant.addActionListener(this);
			statQuant.addActionListener(this);
			generic.add(dynQuant);
			generic.add(statQuant);
			general.add(generic);

			//Biology
			PopupMenu biology = new PopupMenu("Biology");
			MenuItem plateletRetention = new MenuItem("Platelet Retention");
			plateletRetention.addActionListener(this);
			biology.add(plateletRetention);
			//general.add(biology);

			//Bone
			PopupMenu bone = new PopupMenu("Bone");
			MenuItem boneScintigraphy = new MenuItem("Bone Scintigraphy");
			boneScintigraphy.addActionListener(this);
			bone.add(boneScintigraphy);
			general.add(bone);

			//Cardiac
			PopupMenu cardiac = new PopupMenu("Cardiac");
			MenuItem amylose = new MenuItem("Amylose");
			MenuItem mibgQuant = new MenuItem("MIBG Quant");
			amylose.addActionListener(this);
			mibgQuant.addActionListener(this);
			cardiac.add(amylose);
			cardiac.add(mibgQuant);
			general.add(cardiac);

			//Colon
			PopupMenu colon = new PopupMenu("Colon");
			MenuItem colonTransit = new MenuItem("Colon Transit");
			colonTransit.addActionListener(this);
			colon.add(colonTransit);
			general.add(colon);

			//Endocrinology
			PopupMenu endocrinology = new PopupMenu("Endocrinology");
			PopupMenu thyroid = new PopupMenu("Thyroid");
			PopupMenu parathyroid = new PopupMenu("Parathyroid");
			MenuItem tcUptake = new MenuItem("Tc Uptake");
			MenuItem parathyroidScintigraphy = new MenuItem("Parathyroid");
			MenuItem parathyroidSubtraction = new MenuItem("Subtraction");
			tcUptake.addActionListener(this);
			parathyroidScintigraphy.addActionListener(this);
			parathyroidSubtraction.addActionListener(this);
			thyroid.add(tcUptake);
			parathyroid.add(parathyroidScintigraphy);
			//parathyroid.add(parathyroidSubtraction);
			endocrinology.add(thyroid);
			endocrinology.add(parathyroid);
			general.add(endocrinology);

			//Gastric
			PopupMenu gastric = new PopupMenu("Gastric");
			MenuItem gastricEmptyingSolid = new MenuItem("Gastric Emptying Solid");
			MenuItem gastricEmptyingLiquid = new MenuItem("Gastric Emptying Liquid");
			MenuItem esophagealTransit = new MenuItem("Esophageal Transit");
			gastricEmptyingSolid.addActionListener(this);
			gastricEmptyingLiquid.addActionListener(this);
			esophagealTransit.addActionListener(this);
			gastric.add(gastricEmptyingSolid);
			gastric.add(gastricEmptyingLiquid);
			gastric.add(esophagealTransit);
			general.add(gastric);
			
			//Hepatic
			PopupMenu hepatic = new PopupMenu("Hepatic");
			MenuItem biliaryScintigraphyDynamic = new MenuItem("Biliary Scintigraphy");
			MenuItem radioEmbolization = new MenuItem("RadioEmbolization");
			MenuItem scintivol = new MenuItem("Scintivol");
			MenuItem gallbladderEF = new MenuItem("GallBladder Ejection Fraction");
			biliaryScintigraphyDynamic.addActionListener(this);
			radioEmbolization.addActionListener(this);
			scintivol.addActionListener(this);
			gallbladderEF.addActionListener(this);
			hepatic.add(biliaryScintigraphyDynamic);
			hepatic.add(radioEmbolization);
			hepatic.add(scintivol);
			hepatic.add(gallbladderEF);
			general.add(hepatic);
			
			//Lymphatic
			PopupMenu lymphatic = new PopupMenu("Lymphatic");
			MenuItem lymphoscintigraphy = new MenuItem("Lymphoscintigraphy");
			lymphoscintigraphy.addActionListener(this);
			lymphatic.add(lymphoscintigraphy);
			general.add(lymphatic);

			//Pulmonary
			PopupMenu pulmonary = new PopupMenu("Pulmonary");
			MenuItem pulmonaryShunt = new MenuItem("Pulmonary Shunt");
			pulmonaryShunt.addActionListener(this);
			pulmonary.add(pulmonaryShunt);
			general.add(pulmonary);
			
			//Renal
			PopupMenu renal = new PopupMenu("Renal");
			MenuItem renogram = new MenuItem("Renogram");
			MenuItem dmsa = new MenuItem("DMSA");
			MenuItem renogramFollowUp = new MenuItem("Renogram Follow-Up");
			renogram.addActionListener(this);
			dmsa.addActionListener(this);
			renogramFollowUp.addActionListener(this);
			renal.add(renogram);
			renal.add(dmsa);
			renal.add(renogramFollowUp);
			general.add(renal);

			//Salivary Glands
			PopupMenu salivaryGlands = new PopupMenu("Salivary Glands");
			MenuItem salivaryGlandsScintigraphy = new MenuItem("Salivary Glands");
			salivaryGlandsScintigraphy.addActionListener(this);
			salivaryGlands.add(salivaryGlandsScintigraphy);
			general.add(salivaryGlands);
			
			//Other
			PopupMenu other = new PopupMenu("Other");
			MenuItem schaeferCalibration = new MenuItem("Schaefer Calibration");
			schaeferCalibration.addActionListener(this);
			other.add(schaeferCalibration);
			general.add(other);

			general.addSeparator();

			//About et preferences
			MenuItem preferences=new MenuItem("Preferences");
			MenuItem about = new MenuItem("About");
			preferences.addActionListener(this);
			about.addActionListener(this);
			general.add(preferences);
			general.add(about);
			
			bar.add(general);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			switch (cmd) {
				//Generic
				case "Dynamic Quantification":
					IJ.run("Dynamic Quantification");
					break;
				case "Static Quantification":
					IJ.run("Static Quantification");
					break;
				//Biology
				case "Platelet Retention":
					IJ.run("Platelet Retention");
					break;
				//Bone
				case "Bone Scintigraphy":
					IJ.run("Bone Scintigraphy");
					break;
				//Cardiac
				case "Amylose":
					IJ.run("Amylose");
					break;
				case "MIBG Quant":
					IJ.run("MIBG Quant");
					break;
				//Colon
				case "Colon Transit":
					IJ.run("Colon Transit");
					break;
				//Endocrinology
				case "Tc Uptake":
					IJ.run("Tc Uptake");
					break;
				case "Parathyroid":
					IJ.run("Parathyroid");
					break;
				case "Subtraction":
					IJ.run("Subtraction");
					break;
				//Gastric
				case "Gastric Emptying Solid":
					IJ.run("Gastric Emptying Solid");
					break;
				case "Gastric Emptying Liquid":
					IJ.run("Gastric Emptying Liquid");
					break;
				case "Esophageal Transit":
					IJ.run("Esophageal Transit");
					break;
				//Hepatic
				case "Biliary Scintigraphy":
					IJ.run("Biliary Scintigraphy");
					break;
				case "RadioEmbolization":
					IJ.run("RadioEmbolization");
					break;
				case "Scintivol":
					IJ.run("Scintivol");
					break;
				case "GallBladder Ejection Fraction":
					IJ.run("GallBladder Ejection Fraction");
					break;
				//Lymphatic
				case "Lymphoscintigraphy":
					IJ.run("Lymphoscintigraphy");
					break;
				//Pulmonary
				case "Pulmonary Shunt":
					IJ.run("Pulmonary Shunt");
					break;
				//Renal
				case "Renogram":
					IJ.run("Renogram");
					break;
				case "DMSA":
					IJ.run("DMSA");
					break;
				case "Renogram Follow-Up":
					IJ.run("Renogram Follow-Up");
					break;
				//Salivary Glands
				case "Salivary Glands":
					IJ.run("Salivary Glands");
					break;
				//Other
				case "Schaefer Calibration":
					IJ.run("Schaefer Calibration");
					break;
				//About & Preferences
				case "Preferences":
					IJ.run("Preferences");
					break;
				case "About":
					IJ.run("About");
					break;
			}
		}
		

		@Override
		public String getToolIcon() {
			return "T05079 T55079 T7b11T Tbb11c";
		}

		@Override
		public String getToolName() {
			return "Scintigraphy Access, Right click to show menu";
		}

	}

