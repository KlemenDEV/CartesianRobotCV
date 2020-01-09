package org.beetron;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

	private JTextArea log = new JTextArea();

	private Main(SerialTask serialTask, OpenCVTask openCVTask) {
		JToolBar controls = new JToolBar();
		add("North", controls);

		JButton sendData = new JButton("Send points");
		controls.add(sendData);
		sendData.addActionListener(e -> {
			serialTask.sendPoints(
					new Point(7, 0),
					new Point(1, 0),
					new Point(5, 0)
			);
		});

		JButton start = new JButton("Start");
		controls.add(start);
		start.addActionListener(e -> serialTask.sendData(new byte[] { 0x03 }));
		start.setBackground(new Color(0, 255, 0));

		JButton stop = new JButton("Stop");
		controls.add(stop);
		stop.addActionListener(e -> serialTask.sendData(new byte[] { 0x04 }));
		stop.setBackground(new Color(255, 234, 0));

		JButton emergency = new JButton("Emergency");
		controls.add(emergency);
		emergency.addActionListener(e -> serialTask.sendData(new byte[] { 0x0F }));
		emergency.setBackground(new Color(255, 0, 0));

		JScrollPane sp = new JScrollPane(log);
		sp.setPreferredSize(new Dimension(0, 200));

		add("South", sp);
	}

	void printToLog(String text) {
		log.append(text);
	}

	public static void main(String[] args) {
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

		OpenCVTask openCVTask = new OpenCVTask();
		SerialTask serialTask = new SerialTask();

		SwingUtilities.invokeLater(() -> {
			Main main = new Main(serialTask, openCVTask);

			serialTask.setMain(main);

			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			main.setSize(900, 550);
			main.setLocationRelativeTo(null);
			main.setVisible(true);

			//new Thread(openCVTask).start();
			new Thread(serialTask).start();
		});
	}

}
