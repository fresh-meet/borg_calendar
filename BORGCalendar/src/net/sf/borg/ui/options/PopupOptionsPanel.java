/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003-2010 by Mike Berger
 */
package net.sf.borg.ui.options;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.ReminderTimes;
import net.sf.borg.ui.ResourceHelper;
import net.sf.borg.ui.options.OptionsView.OptionsPanel;
import net.sf.borg.ui.popup.ReminderPopupManager;
import net.sf.borg.ui.util.GridBagConstraintsFactory;

/**
 * The Class PopupOptionsPanel provies the options tab for editing popup
 * reminder options
 */
class PopupOptionsPanel extends OptionsPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5238561005974597879L;

	/** The checkfreq. */
	private JSpinner checkfreq = new JSpinner();

	/** The number Of Reminder Times. */
	private int numberOfReminderTimes = 0;

	/** The popenablebox. */
	private JCheckBox popenablebox = new JCheckBox();

	/** The soundbox. */
	private JComboBox soundbox = new JComboBox();

	/** The spinners for setting the reminder times */
	private JSpinner spinners[];


	/**
	 * Instantiates a new popup options panel.
	 */
	public PopupOptionsPanel() {

		this.setLayout(new java.awt.GridBagLayout());

		numberOfReminderTimes = ReminderTimes.getNum();

		ResourceHelper.setText(popenablebox, "enable_popups");
		this.add(popenablebox, GridBagConstraintsFactory.create(0, 0,
				GridBagConstraints.BOTH));

		JLabel jLabel15 = new JLabel();
		jLabel15.setText(Resource.getResourceString("min_between_chks") + " "
				+ Resource.getResourceString("restart_req"));

		this.add(jLabel15, GridBagConstraintsFactory.create(0, 1,
				GridBagConstraints.BOTH));

		checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
		this.add(checkfreq, GridBagConstraintsFactory.create(1, 1,
				GridBagConstraints.BOTH, 1.0, 0.0));

		/*
		 * sound
		 */
		JPanel soundPanel = new JPanel();
		soundPanel.add(new JLabel(Resource.getResourceString("beep_options")));

		soundbox.setEditable(true);
		soundbox.addItem(Resource.getResourceString("default"));
		soundbox.addItem(Resource.getResourceString("no_sound"));
		soundbox.addItem(Resource.getResourceString("Use_system_beep"));
		soundbox.addItem(Resource.getResourceString("Browse")+"...");
		soundbox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if( soundbox.getSelectedIndex() == 3)
				{
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("."));
					chooser.setDialogTitle(Resource
							.getResourceString("choose_file"));
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					int returnVal = chooser.showOpenDialog(null);
					if (returnVal != JFileChooser.APPROVE_OPTION)
						return;

					String fileName = chooser.getSelectedFile().getAbsolutePath();
					soundbox.setSelectedItem(fileName);
				}
			}
			
		});
		soundPanel.add(soundbox);
		
		JButton play = new JButton();
		play.setIcon(new ImageIcon(getClass().getResource(
				"/resource/Forward16.gif")));
		play.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {	
				ReminderPopupManager.playReminderSound(getSoundOption());
			}
			
		});
		soundPanel.add(play);
		
		GridBagConstraints sPanelGBC = GridBagConstraintsFactory.create(0, 2);
		sPanelGBC.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		sPanelGBC.anchor = java.awt.GridBagConstraints.WEST;

		this.add(soundPanel,sPanelGBC);


		JPanel remTimePanel = new JPanel();

		// border
		String title = Resource.getResourceString("Popup_Times") + " ("
				+ Resource.getResourceString("Minutes") + ")";
		Border b = BorderFactory.createTitledBorder(remTimePanel.getBorder(), title);
		remTimePanel.setBorder(b);

		remTimePanel.setLayout(new GridLayout(4, 0));

		// add the spinners
		spinners = new JSpinner[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i] = new JSpinner(new SpinnerNumberModel(0, -99999, 99999,
					1));
			remTimePanel.add(spinners[i]);
		}

		GridBagConstraints remPanelGBC = GridBagConstraintsFactory.create(0, 3);
		remPanelGBC.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		remPanelGBC.anchor = java.awt.GridBagConstraints.WEST;

		this.add(remTimePanel, remPanelGBC);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		OptionsPanel.setBooleanPref(popenablebox, PrefName.REMINDERS);
		Prefs.putPref(PrefName.BEEPINGREMINDERS, getSoundOption());		
		
		Integer checkMins = (Integer) checkfreq.getValue();
		int cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		if (checkMins.intValue() != cur) {
			// why does this not save a new pref if the value is the same?
			// I no longer remeber if this matters - will leave as is
			Prefs.putPref(PrefName.REMINDERCHECKMINS, checkMins);
		}

		int arr[] = new int[numberOfReminderTimes];
		for (int i = 0; i < numberOfReminderTimes; i++) {
			Integer ii = (Integer) spinners[i].getValue();
			arr[i] = ii.intValue();
		}
		ReminderTimes.setTimes(arr);
		loadTimes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.ui.options.OptionsView.OptionsPanel#loadOptions()
	 */
	@Override
	public void loadOptions() {
		OptionsPanel.setCheckBox(popenablebox, PrefName.REMINDERS);
		
		String beep = Prefs.getPref(PrefName.BEEPINGREMINDERS);
		if( beep.equals("true"))
		{
			soundbox.setSelectedIndex(0);
		}
		else if( beep.equals("false"))
		{
			soundbox.setSelectedIndex(1);
		}
		else if( beep.equals("system-beep"))
		{
			soundbox.setSelectedIndex(2);
		}
		else
		{
			soundbox.setSelectedItem(beep);
		}

		int mins = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		checkfreq.setValue(new Integer(mins));

		// load the times
		loadTimes();

	}

	/**
	 * Load the spinner valies from stored prefs
	 */
	private void loadTimes() {
		for (int i = 0; i < numberOfReminderTimes; i++) {
			spinners[i].setValue(new Integer(ReminderTimes.getTimes(i)));
		}
	}
	
	/**
	 * get the sound option preference value based on the current sound combo box setting
	 * @return
	 */
	private String getSoundOption()
	{
		String option = "";
		int i = soundbox.getSelectedIndex();
		if(i == 0)
		{
			option = "true";
		}
		else if( i == 1 )
		{
			option = "false";
		}
		else if( i == 2 )
		{
			option = "system-beep";
		}
		else
		{
			option = (String)soundbox.getSelectedItem();
		}
		return option;
	}

}