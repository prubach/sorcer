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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;

import sorcer.core.SorcerConstants;
import sorcer.util.CallbackModel;
import sorcer.util.ui.AlertDialog;
import sorcer.util.ui.Launcher;
import sorcer.util.ui.View;

/**
 * The class RequestDialog displays a dialog window requesting a value from the
 * user for a given attribute.
 * <p>
 * When the user has entered the requested value, this RequestDialog calls
 * model's observers to be updated. A model is the instance a subclass of Model
 * or class implementing the jgapp.util.CallbackModel interface. When a model is
 * not provided, by default the Launcher's model is used. Any launcher maybe
 * provided as a class variable launcher. Class variables aspect, length and
 * position hold default values for RequestDialog. These class variables might
 * be customized and are reset automatically to default values after closing a
 * RequestDialog.
 */

public class RequestDialog extends Dialog implements SorcerConstants {
	TextField valField = null;
	CallbackModel model = null;
	static public String aspect = "RequestDialog";
	// myAspect copies the class variable aspect
	private String myAspect;
	public static final int TOP = 1, LEFT = 2;
	public static int length = 20, labelPosition = LEFT;
	public static Dimension position;
	public static Launcher launcher = Launcher.any();
	private Button okBtn;

	public RequestDialog(CallbackModel model, Frame parent, String title,
			String key, String defaultStr) {
		this(model, parent, title, key, defaultStr, OK);
	}

	/**
	 * Creates a request dialog window
	 * 
	 * @param model
	 *            - the model for the dialog
	 * @param parent
	 *            - the parent frame
	 * @param title
	 *            - the title of the dialog
	 * @param key
	 *            - the attribute that is requested from the user
	 * @param obBtnName
	 *            - the label for OK button (this is shown as the message in the
	 *            dialog window)
	 * @default the defaultStr value of the attribute given to the user (if
	 *          defaultStr is not null)
	 */
	public RequestDialog(CallbackModel model, Frame parent, String title,
			String key, String defaultStr, String okBtnName) {
		super((parent != null) ? parent : View.getFrame(launcher),
				(title != null) ? title : "Request Dialog", true);
		this.model = (model != null) ? model : launcher.model();
		setResizable(false);
		setLayout(new BorderLayout());

		if (defaultStr != null)
			valField = new TextField(defaultStr, length);
		else
			valField = new TextField(length);

		Panel request = new Panel();
		if (labelPosition == LEFT) {
			request.setLayout(new FlowLayout());
			request.add(new Label(key, Label.RIGHT));
			request.add(valField);
		} else {
			request.setLayout(new BorderLayout());
			request.add("North", new Label(key, Label.CENTER));
			request.add("South", valField);
		}

		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout());
		okBtn = new Button(okBtnName);

		buttons.add(okBtn);
		buttons.add(new Button(QUIT));

		add("North", request);
		add("South", buttons);

		// copy class aspect into instance myAspect
		myAspect = aspect;
		reset();
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

	/**
	 * Handles user interactions with the RequestDialog
	 */
	public boolean action(Event e, Object arg) {
		if (e.target == okBtn) {
			String answer = valField.getText().trim();
			if (answer.length() == 0) {
				AlertDialog.popup("No requested data");
				return true;
			}
			model.changed(myAspect, answer);
		}
		/*
		 * else if (Const.QUIT.equals(arg)) model.changed(myAspect, Const.QUIT);
		 */
		if (arg.equals(OK) || arg.equals(QUIT)) {
			hide();
			dispose();
		}
		return super.action(e, arg);
	}

	/**
	 * Accepts user's enter key as the Ok dialog action and closses this dialog
	 */
	public boolean keyDown(Event evt, int code) {
		if (evt.target == valField && code == 10) {
			String answer = valField.getText().trim();
			model.changed(myAspect, answer);

			if (okBtn.getLabel().equals(OK)) {
				hide();
				dispose();
			}
		}
		return super.keyDown(evt, code);
	}

	static public void popup(CallbackModel model, Frame parent, String title,
			String key, String defaultStr, String okBtnName) {
		RequestDialog rd = new RequestDialog(model, parent, title, key,
				defaultStr, okBtnName);
		boolean isMoved = false;
		if (position != null) {
			rd.move(position.width, position.height);
			isMoved = true;
		} else if (launcher != null) {
			Point p = View.absLocation(launcher);
			if (p.x != 0 && p.y != 0) {
				rd.move(p.x, p.y);
				isMoved = true;
			}
		}

		if (!isMoved) {
			Dimension screenSize = rd.getToolkit().getScreenSize();
			rd.move((screenSize.width - rd.size().width) / 2,
					(screenSize.height - rd.size().height) / 2);
		}
		rd.pack();
		rd.show();
	}

	static public void popup(CallbackModel model, String key,
			String defaultStr, String okBtnName) {
		RequestDialog.popup(model, null, null, key, defaultStr, okBtnName);
	}

	static public void popup(CallbackModel model, String key, String defaultStr) {
		RequestDialog.popup(model, null, null, key, defaultStr, OK);
	}

	static public void popup(CallbackModel model, String key) {
		RequestDialog.popup(model, key, null);
	}

	static public void popup(String key, String defaultStr) {
		RequestDialog.popup(null, key, null);
	}

	static public void popup(String key) {
		RequestDialog.popup(key, null);
	}

	/**
	 * Sets default static parameters of the RequestDialog
	 */
	public static void reset() {
		aspect = "RequestDialog";
		length = 20;
		position = new Dimension(300, 250);
		labelPosition = LEFT;
	}

}
