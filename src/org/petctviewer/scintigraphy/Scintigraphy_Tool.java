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
		
		void addPopupMenu(Toolbar par) {
			//On cree le popup menu general
			general = new PopupMenu();
			//On cree les menus par organes
			PopupMenu bone = new PopupMenu("Bone");
			PopupMenu gastric = new PopupMenu("Gastric");
			PopupMenu pulmonary = new PopupMenu("Pulmonary");
			PopupMenu hepatic = new PopupMenu("Hepatic");
			PopupMenu cardiac = new PopupMenu("Cardiac");
			PopupMenu colon = new PopupMenu("Colon");
			PopupMenu lymphatic = new PopupMenu("Lymphatic");
			PopupMenu renal = new PopupMenu("Renal");
			PopupMenu generic = new PopupMenu("Generic");
			PopupMenu other = new PopupMenu("Other");
			PopupMenu endocrinology = new PopupMenu("Endocrinology");
			PopupMenu thyroid = new PopupMenu("Thyroid");
			PopupMenu parathyroid = new PopupMenu("Parathyroid");
			
			if (Menus.getFontSize()!=0) general.setFont(Menus.getFont());

			//Gastric
			MenuItem gastricEmptyingSolid = new MenuItem("Gastric Emptying Solid");
			MenuItem gastricEmptyingLiquid = new MenuItem("Gastric Emptying Liquid");
			MenuItem esophagealTransit = new MenuItem("Esophageal Transit");

			//Pulmonary shunt
			MenuItem pulmonaryShunt = new MenuItem("Pulmonary Shunt");
			
			//Cardiac
			MenuItem amylose = new MenuItem("Amylose");
			MenuItem mibgQuant = new MenuItem("MIBG Quant");
			
			//Colon
			MenuItem colonTransit = new MenuItem("Colon Transit");
			
			//Hepatic
			MenuItem biliaryScintigraphyDynamic = new MenuItem("Biliary Scintigraphy");
			MenuItem radioEmbolization = new MenuItem("RadioEmbolization");
			
			//Lymphatic
			MenuItem lymphoScintigraphy = new MenuItem("Lymphoscintigraphy");
			
			//Bone
			MenuItem boneScintigraphy = new MenuItem("Bone Scintigraphy");
			
			//Renal
			MenuItem renogram = new MenuItem("Renogram");
			MenuItem dmsa = new MenuItem("DMSA");
			MenuItem renogramFollowUp = new MenuItem("Renogram Follow-Up");
			
			//About et preference
			MenuItem about = new MenuItem("About");
			MenuItem preferences=new MenuItem("Preferences");
			
			//Generic
			MenuItem dynquant = new MenuItem("Dynamic Quantification");
			MenuItem statquant = new MenuItem("Static Quantification");

			//Endocrinology
			MenuItem tcUptake = new MenuItem("Tc Uptake");
			MenuItem parathyroidMenu = new MenuItem("Parathyroid");
			
			//Other
			MenuItem schaeferCalibration = new MenuItem("Schaefer Calibration");


			//Ajout des listeners
			boneScintigraphy.addActionListener(this);
			gastricEmptyingSolid.addActionListener(this);
			gastricEmptyingLiquid.addActionListener(this);
			esophagealTransit.addActionListener(this);
			pulmonaryShunt.addActionListener(this);
			amylose.addActionListener(this);
			mibgQuant.addActionListener(this);
			colonTransit.addActionListener(this);
			biliaryScintigraphyDynamic.addActionListener(this);
			radioEmbolization.addActionListener(this);
			lymphoScintigraphy.addActionListener(this);
			renogram.addActionListener(this);
			dmsa.addActionListener(this);
			renogramFollowUp.addActionListener(this);
			about.addActionListener(this);
			preferences.addActionListener(this);
			dynquant.addActionListener(this);
			statquant.addActionListener(this);
			tcUptake.addActionListener(this);
			parathyroidMenu.addActionListener(this);
			schaeferCalibration.addActionListener(this);
			

			//
			bone.add(boneScintigraphy);
			
			gastric.add(gastricEmptyingSolid);
			gastric.add(gastricEmptyingLiquid);
			gastric.add(esophagealTransit);
			
			pulmonary.add(pulmonaryShunt);
			
			cardiac.add(amylose);
			cardiac.add(mibgQuant);

			colon.add(colonTransit);

			hepatic.add(biliaryScintigraphyDynamic);
			hepatic.add(radioEmbolization);
			
			renal.add(renogram);
			renal.add(dmsa);
			renal.add(renogramFollowUp);
			
			generic.add(dynquant);
			generic.add(statquant);
			
			lymphatic.add(lymphoScintigraphy);

			parathyroid.add(parathyroidMenu);
			thyroid.add(tcUptake);
			endocrinology.add(parathyroid);
			endocrinology.add(tcUptake);

			other.add(schaeferCalibration);
			
			
			general.add(generic);
			general.add(bone);
			general.add(cardiac);
			general.add(colon);
			general.add(endocrinology);
			general.add(gastric);
			general.add(hepatic);
			general.add(lymphatic);
			general.add(pulmonary);
			general.add(renal);
			general.add(other);
			general.add(preferences);
			general.add(about);
			
			
			par.add(general);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			switch (cmd) {
				case "Amylose":
					IJ.run("Amylose");
					break;
				case "Colon Transit":
					IJ.run("Colon Transit");
					break;
				case "Bone Scintigraphy":
					IJ.run("Bone Scintigraphy");
					break;
				case "Biliary Scintigraphy":
					IJ.run("Biliary Scintigraphy");
					break;
				case "Gastric Emptying Solid":
					IJ.run("Gastric Emptying Solid");
					break;
				case "Gastric Emptying Liquid":
					IJ.run("Gastric Emptying Liquid");
					break;
				case "Esophageal Transit":
					IJ.run("Esophageal Transit");
					break;
				case "Pulmonary Shunt":
					IJ.run("Pulmonary Shunt");
					break;
				case "Dynamic Quantification":
					IJ.run("Dynamic Quantification");
					break;
				case "Static Quantification":
					IJ.run("Static Quantification");
					break;
				case "Renogram":
					IJ.run("Renogram");
					break;
				case "DMSA":
					IJ.run("DMSA");
					break;
				case "Lymphoscintigraphy":
					IJ.run("Lymphoscintigraphy");
					break;
				case "Renogram Follow-Up":
					IJ.run("Renogram Follow-Up");
					break;
				case "About":
					IJ.run("About");
					break;
				case "Preferences":
					IJ.run("Preferences");
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

