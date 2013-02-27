/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.util.dbas;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import sorcer.core.SorcerConstants;
import sorcer.util.CallbackModel;

public class ProcessMonitor extends Frame implements ActionListener,
		CallbackModel, SorcerConstants {
	public List messageList;
	private MonitoredProcess process;
	private int listSize;

	ProcessMonitor(MonitoredProcess process) {
		this(process, -1);
	}

	ProcessMonitor(MonitoredProcess process, int listSize) {
		// Create a window to display our connections in
		setTitle(process.appName() + " Monitor");
		this.process = process;
		this.listSize = listSize;

		// Create a list to hold current user connections
		if (listSize > 0)
			messageList = new List(listSize);
		else
			messageList = new List();
		Font aFont = new Font("Helvetica", Font.PLAIN, 12);
		messageList.setFont(aFont);
		setFont(aFont);

		// Bottom command panel
		Panel bottomPanel = new Panel();
		bottomPanel.setLayout(new FlowLayout((FlowLayout.CENTER), 10, 10));

		Button cancelBtn = new Button(CLOSE);
		cancelBtn.addActionListener(this);

		Button killBtn = new Button(KILL);
		killBtn.addActionListener(this);

		bottomPanel.add(killBtn);
		bottomPanel.add(cancelBtn);

		add("Center", messageList);
		add("South", bottomPanel);

		setSize(200, 300);
		setLocation(20, 40);
	}

	public void addItem(String item) {
		// keep messageList limited to the predefined size
		if (listSize > 0 && messageList.getItemCount() > listSize) {
			messageList.delItem(0);
			messageList.addItem(item);
			return;
		}
		messageList.addItem(item);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CLOSE)) {
			process.isMonitored(false);
			dispose();
		} else if (e.getActionCommand().equals(KILL)) {
			RequestDialog.aspect = KILL;
			RequestDialog.popup(this, this, "Kill Application Server",
					"Password:", null, OK);
			dispose();
		}
	}

	public void changed(Object aspect, Object arg) {
		if (aspect.equals(KILL) && arg.equals(process.passwd()))
			process.stopProcess();
	}
}
