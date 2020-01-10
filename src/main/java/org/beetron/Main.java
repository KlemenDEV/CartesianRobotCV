package org.beetron;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Main extends JFrame {

	private JTextArea log = new JTextArea();

	private JLabel display = new JLabel();

	private Main(SerialTask serialTask, OpenCVTask openCVTask) {
		JToolBar controls = new JToolBar();
		controls.setFloatable(false);
		add("North", controls);

		JButton sendData = new JButton("Send points");
		controls.add(sendData);
		sendData.addActionListener(e -> serialTask.sendPoints(openCVTask.getPoints()));

		controls.add(Box.createHorizontalGlue());

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

		log.setBackground(Color.darkGray);
		log.setForeground(Color.white);

		JScrollPane sp = new JScrollPane(log);
		sp.setPreferredSize(new Dimension(0, 200));
		sp.setBorder(null);
		log.setBorder(null);

		add("South", sp);

		JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
		center.add(display);

		center.setBackground(Color.black);

		add("Center", center);
	}

	void printToLog(String text) {
		log.append(text);
	}

	void setDisplay(BufferedImage image) {
		display.setIcon(new ImageIcon(image));
	}

	public static void main(String[] args) {
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

		OpenCVTask openCVTask = new OpenCVTask();
		SerialTask serialTask = new SerialTask();

		SwingUtilities.invokeLater(() -> {
			Main main = new Main(serialTask, openCVTask);

			serialTask.setMain(main);
			openCVTask.setMain(main);

			main.setTitle("Cartesian robot controller");
			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			main.setSize(1200, 760);
			main.setLocationRelativeTo(null);
			main.setVisible(true);

			new Thread(openCVTask).start();
			new Thread(serialTask).start();
		});
	}

}
