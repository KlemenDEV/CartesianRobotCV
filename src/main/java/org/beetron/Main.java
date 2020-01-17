package org.beetron;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

	private JTextArea log = new JTextArea();

	private JLabel display = new JLabel();

	JProgressBar bar = new JProgressBar();

	private Main(SerialTask serialTask, OpenCVTask openCVTask) {
		JToolBar controls = new JToolBar();
		controls.setFloatable(false);
		add("North", controls);

		JButton startCamera = new JButton("Start camera");
		controls.add(startCamera);
		startCamera.addActionListener(e -> {
			startCamera.setEnabled(false);
			new Thread(openCVTask).start();
		});

		JButton sendData = new JButton("Send points from camera");
		controls.add(sendData);
		sendData.addActionListener(e -> {
			List<Point> currentActionPoints = openCVTask.getPoints();
			serialTask.sendPoints(currentActionPoints);

			bar.setMaximum(currentActionPoints.size());
			bar.setValue(0);
		});

		JButton sendDataManual = new JButton("Send manual points");
		controls.add(sendDataManual);
		sendDataManual.addActionListener(e -> {
			String data = JOptionPane.showInputDialog(null, "Enter points in the following format: (1.1, 1.1) (2, 2)");
			if (data != null) {
				try {
					List<Point> pts = new ArrayList<>();
					String[] points = data.split("\\) \\(");
					for (String point : points) {
						String[] cds = point.split(",");
						float x = Float.parseFloat(cds[0].replace("(", "").trim());
						float y = Float.parseFloat(cds[1].replace(")", "").trim());
						pts.add(new Point(x, y));
					}
					serialTask.sendPoints(pts);
					System.err.println(pts);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		controls.add(Box.createHorizontalGlue());

		controls.add(bar);

		bar.setStringPainted(true);
		bar.setForeground(Color.black);

		controls.add(new JLabel(" - "));

		JButton start = new JButton("Start");
		controls.add(start);
		start.addActionListener(e -> serialTask.sendData(new byte[] { 0x03 }));
		start.setBackground(new Color(0, 255, 0));

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

		bar.setValue(Math.min(Math.round((log.getText().split("p:", -1).length - 1) / 3.f), 100));
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

			new Thread(serialTask).start();
		});
	}

}
