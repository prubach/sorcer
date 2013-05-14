/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.core.provider.logger.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;

/**
 * The levels in descending order are:
 * <ul>
 * <li>SEVERE (highest value)
 * <li>WARNING
 * <li>INFO
 * <li>CONFIG
 * <li>FINE
 * <li>FINER
 * <li>FINEST (lowest value)
 * </ul>
 * 
 */
public class FilterPane extends Observable {
	private JPanel filterPanel;
	private GridBagConstraints c;
	private String selectedLevel;
	private JComboBox levelComboBox;
	private JTextField searchField;
	private final static String[] levels = { "ALL", "FINEST", "FINER", "FINE",
			"CONFIG", "INFO", "WARNING", "SEVERE" };

	public FilterPane() {
		initialize();
	}

	public void initialize() {
		filterPanel = new JPanel();
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
		JLabel level = new JLabel("Level:");
		levelComboBox = new JComboBox(levels);
		levelComboBox.addActionListener(new LevelListener());

		JLabel searchLabel = new JLabel("Expression:");
		searchField = new JTextField(16);

		JButton filterButton = new JButton("Filter Out");
		filterButton.setActionCommand("Filter Out");
		filterButton.addActionListener(new FilterActionListener());

		JButton clearButton = new JButton("Clear");
		clearButton.setActionCommand("Clear");
		clearButton.addActionListener(new FilterActionListener());
		
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		c.weightx = .5;
		c.gridx = 0;
		c.gridy = 0;
		inputPanel.add(level, c);
		c.gridx = 1;
		c.gridy = 0;
		inputPanel.add(levelComboBox, c);

		c.insets = new Insets(20, 0, 0, 10);
		c.gridx = 0;
		c.gridy = 1;
		inputPanel.add(searchLabel, c);
		c.insets = new Insets(20, 0, 0, 10);
		c.gridx = 1;
		c.gridy = 1;
		inputPanel.add(searchField, c);

		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		controlPanel.add(filterButton);
		controlPanel.add(clearButton);
		
		filterPanel.add(inputPanel, BorderLayout.CENTER);
		filterPanel.add(controlPanel, BorderLayout.PAGE_END);
	}

	public JPanel getFilterPanel() {
		return filterPanel;
	}

	public void resetLevel() {
		levelComboBox.setSelectedIndex(0);
	}

	public String getText() {
		return searchField.getText();
	}

	public String getLevel() {
		return this.selectedLevel;
	}

	private class LevelListener implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			selectedLevel = (String) cb.getSelectedItem();
			setChanged();
			notifyObservers("level");
		}
	}

	private class FilterActionListener implements ActionListener {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent ae) {
			String cmd = ae.getActionCommand();
			if (cmd.equals("Filter Out")) {
				setChanged();
				notifyObservers("search");
			} else if (cmd.equals("Clear")) {
				searchField.setText("");
				resetLevel();
			}
		}
	}
}