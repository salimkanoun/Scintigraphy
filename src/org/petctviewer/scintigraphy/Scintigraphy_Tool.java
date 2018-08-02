/**
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
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class Scintigraphy_Tool extends PlugInTool implements ActionListener {
	
	
		private final int OFFSET = 0;
		PopupMenu general;
		
		
		@Override
		public void showPopupMenu(MouseEvent e, Toolbar par) {
			addPopupMenu(par);
			general.show(e.getComponent(), e.getX()+OFFSET, e.getY()+OFFSET);
		}
		
		void addPopupMenu(Toolbar par) {
			//On cree le popup menu general
			general = new PopupMenu();
			//On cree les menus par organes
			PopupMenu gastric = new PopupMenu("Gastric");
			PopupMenu pulmonary = new PopupMenu("Pulmonary");
			PopupMenu hepatic = new PopupMenu("Hepatic");
			PopupMenu cardiac = new PopupMenu("Cardiac");
			PopupMenu renal = new PopupMenu("Renal");
			PopupMenu generic = new PopupMenu("Generic");
			
			if (Menus.getFontSize()!=0) general.setFont(Menus.getFont());
			
			MenuItem gastricEmptying = new MenuItem("Gastric Emptying");
			MenuItem gastricDynamic = new MenuItem("Gastric Dyamique");
			MenuItem gastricCondense = new MenuItem("Gastric Condense");
			MenuItem esophagealTransit = new MenuItem("Esophageal Transit");
			
			
			//plumonary shunt
			MenuItem plumonaryShunt = new MenuItem("Pulmonary Shunt");
			
			//Cardiac
			MenuItem dpdQuant = new MenuItem("DPD Quant");
			
			//Hepatic
			MenuItem biliaryScintigraphyDynamic = new MenuItem("Biliary Scintigraphy Dynamic");
			MenuItem biliaryScintigraphyStatic = new MenuItem("Biliary Scintigraphy Static");
			
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
			gastricEmptying.addActionListener(this);
			gastricDynamic.addActionListener(this);
			gastricCondense.addActionListener(this);
			esophagealTransit.addActionListener(this);
			plumonaryShunt.addActionListener(this);
			dpdQuant.addActionListener(this);
			biliaryScintigraphyDynamic.addActionListener(this);
			biliaryScintigraphyStatic.addActionListener(this);
			renogram.addActionListener(this);
			dmsa.addActionListener(this);
			renogramFollowUp.addActionListener(this);
			about.addActionListener(this);
			preferences.addActionListener(this);
			dynquant.addActionListener(this);
			statquant.addActionListener(this);
			
			gastric.add(gastricEmptying);
			gastric.add(gastricDynamic);
			gastric.add(gastricCondense);
			gastric.add(esophagealTransit);
			
			pulmonary.add(plumonaryShunt);
			
			cardiac.add(dpdQuant);
			hepatic.add(biliaryScintigraphyDynamic);
			hepatic.add(biliaryScintigraphyStatic);
			
			renal.add(renogram);
			renal.add(dmsa);
			renal.add(renogramFollowUp);
			
			generic.add(dynquant);
			generic.add(statquant);
			
			
			general.add(generic);
			general.add(cardiac);
			general.add(gastric);
			general.add(hepatic);
			general.add(pulmonary);
			general.add(renal);
			general.add(preferences);
			general.add(about);
			
			
			
			par.add(general);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd=="DPD Quant") {
				IJ.run("DPD Quant");
			}
			if (cmd=="Biliary Scintigraphy Dynamic") {
				IJ.run("Biliary Scintigraphy Dynamic");
			}
			if (cmd=="Biliary Scintigraphy Static") {
				IJ.run("Biliary Scintigraphy Static");
			}
			if (cmd=="Gastric Emptying") {
				IJ.run("Gastric Emptying software");
			}
			if (cmd=="Gastric Dyamique") {
				IJ.run("Gastric Emptying Dynamic");
			}
			if (cmd=="Esophageal Transit") {
				IJ.run("Esophageal Transit");
			}
			if (cmd=="Pulmonary Shunt") {
				IJ.run("Pulmonary Shunt");
			}
			if (cmd=="Dynamic Quantification") {
				IJ.run("Dynamic Quantification");
			}
			if (cmd=="Static Quantification") {
				IJ.run("Static Quantification");
			}
			if (cmd=="Renogram") {
				IJ.run("Renogram");
			}
			if (cmd=="DMSA") {
				IJ.run("DMSA");
			}
			if (cmd=="Renogram Follow-Up") {
				IJ.run("Renogram Follow-Up");
			}
			if (cmd=="Gastric Condense") {
				Thread condense=new Thread (new Runnable() 
			    {
			      public void run()
			      {  
			    	  IJ.run("Gastric Condense");
			      }
			    });
			   condense.start();
			   
			}
			if (cmd=="About") {
				IJ.run("About");
			}
			if (cmd=="Preferences") {
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

