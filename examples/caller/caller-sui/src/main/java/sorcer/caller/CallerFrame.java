package sorcer.caller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * SORCER provider info UI
 */
public class CallerFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private static final int FRAME_WIDTH = 650;

	private static final int FRAME_HEIGHT = 700;

	/** Creates an Arithmetic Tester frame */
	public CallerFrame(Object obj) {
		super();
		getAccessibleContext().setAccessibleName("Console Viewer");
		createFrame(obj);
	}

	/**
	 * Create the GUI frame and do mot make it visible and do not allow to close
	 * it.
	 */
	private void createFrame(Object serviceItem) {
		// Create and set up the window.
		setTitle("Caller Console Viewer");

		// Create and set up the content pane.
		CallerImplUI panel = new CallerImplUI(serviceItem);
		panel.setOpaque(true); // content panes must be opaque
		setContentPane(panel);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
	}
}
