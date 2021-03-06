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
			
			if (Menus.getFontSize()!=0) general.setFont(Menus.getFont());
			
			MenuItem gastricEmptyingSolid = new MenuItem("Gastric Emptying Solid");
			MenuItem gastricEmptyingLiquid = new MenuItem("Gastric Emptying Liquid");
			MenuItem esophagealTransit = new MenuItem("Esophageal Transit");
			
			
			//plumonary shunt
			MenuItem plumonaryShunt = new MenuItem("Pulmonary Shunt");
			
			//Cardiac
			MenuItem dpdQuant = new MenuItem("DPD Quant");
			
			//Colon
			MenuItem colonTransit = new MenuItem("Colon Transit");
			
			//Hepatic
			MenuItem biliaryScintigraphyDynamic = new MenuItem("Biliary Scintigraphy");
			
			//Lymphoscintigraphy
			MenuItem lymphoScintigraphy = new MenuItem("Lymphoscintigraphy");
			
			//Lymphoscintigraphy
			MenuItem boneScintigraphy = new MenuItem("Bone Scintigrahy");
			
			//Renal
			MenuItem renogram = new MenuItem("Renogram");
			MenuItem dmsa = new MenuItem("DMSA");
			MenuItem renogramFollowUp = new MenuItem("Renogram Follow-Up");
			
			//About et preference
			MenuItem about = new MenuItem("About");
			MenuItem preferences=new MenuItem("Preferences");
			
			//generic
			MenuItem dynquant = new MenuItem("Dynamic Quantification");
			MenuItem statquant = new MenuItem("Static Quantification");
			
			
			
			
			//Ajout des listeners
			boneScintigraphy.addActionListener(this);
			gastricEmptyingSolid.addActionListener(this);
			gastricEmptyingLiquid.addActionListener(this);
			esophagealTransit.addActionListener(this);
			plumonaryShunt.addActionListener(this);
			dpdQuant.addActionListener(this);
			colonTransit.addActionListener(this);
			biliaryScintigraphyDynamic.addActionListener(this);
			lymphoScintigraphy.addActionListener(this);
			renogram.addActionListener(this);
			dmsa.addActionListener(this);
			renogramFollowUp.addActionListener(this);
			about.addActionListener(this);
			preferences.addActionListener(this);
			dynquant.addActionListener(this);
			statquant.addActionListener(this);
			
			
			bone.add(boneScintigraphy);
			
			gastric.add(gastricEmptyingSolid);
			gastric.add(gastricEmptyingLiquid);
			gastric.add(esophagealTransit);
			
			pulmonary.add(plumonaryShunt);
			
			cardiac.add(dpdQuant);
			colon.add(colonTransit);
			hepatic.add(biliaryScintigraphyDynamic);
			
			renal.add(renogram);
			renal.add(dmsa);
			renal.add(renogramFollowUp);
			
			generic.add(dynquant);
			generic.add(statquant);
			
			lymphatic.add(lymphoScintigraphy);
			
			
			general.add(generic);
			general.add(bone);
			general.add(cardiac);
			general.add(colon);
			general.add(gastric);
			general.add(hepatic);
			general.add(lymphatic);
			general.add(pulmonary);
			general.add(renal);
			general.add(preferences);
			general.add(about);
			
			
			par.add(general);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("DPD Quant")) {
				IJ.run("DPD Quant");
			}if (cmd.equals("Colon Transit")) {
				IJ.run("Colon Transit");
			}
			if(cmd.equals("Bone Scintigrahy")) {
				IJ.run("Bone Scintigraphy");
				
			}
			if (cmd.equals("Biliary Scintigraphy")) {
				IJ.run("Biliary Scintigraphy");
			}
			if (cmd.equals("Gastric Emptying Solid")) {
				IJ.run("Gastric Emptying Solid");
			}
			if (cmd.equals("Gastric Emptying Liquid")) {
				IJ.run("Gastric Emptying Liquid");
			}
			if (cmd.equals("Esophageal Transit")) {
				IJ.run("Esophageal Transit");
			}
			if (cmd.equals("Pulmonary Shunt")) {
				IJ.run("Pulmonary Shunt");
			}
			if (cmd.equals("Dynamic Quantification")) {
				IJ.run("Dynamic Quantification");
			}
			if (cmd.equals("Static Quantification")) {
				IJ.run("Static Quantification");
			}
			if (cmd.equals("Renogram")) {
				IJ.run("Renogram");
			}
			if (cmd.equals("DMSA")) {
				IJ.run("DMSA");
			}if(cmd.equals("Lymphoscintigraphy")) {
				IJ.run("LymphoScintigraphy");
			}
			if (cmd.equals("Renogram Follow-Up")) {
				IJ.run("Renogram Follow-Up");
			}
			if (cmd.equals("About")) {
				IJ.run("About");
			}
			if (cmd.equals("Preferences")) {
				IJ.run("Preferences");
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

