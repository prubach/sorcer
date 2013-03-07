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

package sorcer.util.ui;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;

import sorcer.core.SorcerConstants;

/**
 * The AlertDialog informs the user about a specific message and waits for the
 * user to confirm this message.
 */
public class AlertDialog extends Dialog implements SorcerConstants {
	public static Dimension position = null;

	/**
	 * Creates a new AlertDialog
	 * 
	 * @param parent
	 *            - the owner of the dialog
	 * @param title
	 *            - the title of the dialog window
	 * @param message
	 *            - the message to be displayed in the dialog
	 */
	public AlertDialog(Frame parent, String title, String message) {
		super((parent != null) ? parent : View.getFrame(Launcher.any()),
				(title != null) ? title : "Alert Dialog", true);
		Label aLabel = new Label(message, Label.CENTER);
		Font aFont = new Font("Helvetica", Font.BOLD, 14);
		aLabel.setFont(aFont);
		add("Center", aLabel);
		Panel aPanel = new Panel();
		aPanel.add(new Button(OK));
		add("South", aPanel);
	}

	/**
	 * Creates a new AlertDialog with the title "Alert Dialog"
	 * 
	 * @param parent
	 *            - the owner of the dialog
	 * @param message
	 *            - the message to be displayed in the dialog
	 */
	public AlertDialog(Frame parent, String message) {
		this(parent, null, message);
	}

	/**
	 * Handles event for window destroy
	 */
	public boolean handleEvent(Event evt) {
		if (evt.id == Event.WINDOW_DESTROY && evt.target == this) {
			dispose();
			return true;
		}
		return super.handleEvent(evt);
	}

	public boolean action(Event e, Object arg) {
		if (OK.equals(arg))
			dispose();
		reset();
		return true;
	}

	static public void popup(Frame parent, String info) {
		AlertDialog ad = new AlertDialog((parent != null) ? parent : View
				.getFrame(Launcher.any()), info);
		ad.pack();
		boolean isMoved = false;
		if (position != null) {
			ad.move(position.width, position.height);
			isMoved = true;
		} else if (Launcher.any() != null) {
			Point p = View.absLocation(Launcher.any());
			if (p.x != 0 && p.y != 0) {
				ad.move(p.x, p.y);
				isMoved = true;
			}
		}

		if (!isMoved) {
			Dimension screenSize = ad.getToolkit().getScreenSize();
			ad.move((screenSize.width - ad.size().width) / 2,
					(screenSize.height - ad.size().height) / 2);
		}
		ad.show();
	}

	static public void popup(String info) {
		AlertDialog.popup(null, info);
	}

	/**
	 * Sets default static parameters of the AlertDialog
	 */
	public static void reset() {
		position = null;
	}
}
