package org.beetron;

import javax.swing.*;

public class Main extends JFrame {

	private Main() {

	}

	public static void main(String[] args) {
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

		OpenCVTask openCVTask = new OpenCVTask();
		SerialTask serialTask = new SerialTask();

		new Thread(openCVTask).start();
		new Thread(serialTask).start();

		SwingUtilities.invokeLater(() -> {
			Main main = new Main();
			main.setSize(900, 550);
			main.setLocationRelativeTo(null);
			main.setVisible(true);
		});
	}

}
