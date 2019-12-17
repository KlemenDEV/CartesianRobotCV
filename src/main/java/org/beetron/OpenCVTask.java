package org.beetron;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class OpenCVTask implements Runnable {

	@Override public void run() {
		VideoCapture camera = new VideoCapture(0);

		Mat frame = new Mat();
		camera.read(frame);

		if (!camera.isOpened()) {
			System.out.println("Failed to open camera");
			System.exit(-1);
		} else {
			while (true) {
				if (camera.read(frame)) {


					break;
				}
			}
		}
		camera.release();
	}


}
