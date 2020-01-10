package org.beetron;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OpenCVTask implements Runnable {

	private Main main;

	void setMain(Main main) {
		this.main = main;
	}

	@Override public void run() {
		VideoCapture camera = new VideoCapture(1);

		Mat frame = new Mat();
		camera.read(frame);

		main.printToLog("Opening camera\n");

		if (!camera.isOpened()) {
			main.printToLog("Failed to open camera!\n");
		} else {
			BufferedImage result = new BufferedImage(frame.cols(), frame.rows(), BufferedImage.TYPE_4BYTE_ABGR);

			while (true) {
				if (camera.read(frame)) {
					result = matToBufferedImage(frame, result);

					main.setDisplay(result);
				} else {
					break;
				}
			}
		}
		camera.release();
	}

	public List<Point> getPoints() {
		return Arrays.asList(new Point(7, 0), new Point(1, 0), new Point(5, 0));
	}

	private BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg) {
		if (matrix != null) {
			int cols = matrix.cols();
			int rows = matrix.rows();
			int elemSize = (int) matrix.elemSize();
			byte[] data = new byte[cols * rows * elemSize];
			int type;
			matrix.get(0, 0, data);
			switch (matrix.channels()) {
			case 1:
				type = BufferedImage.TYPE_BYTE_GRAY;
				break;
			case 3:
				type = BufferedImage.TYPE_3BYTE_BGR;
				// bgr to rgb
				byte b;
				for (int i = 0; i < data.length; i = i + 3) {
					b = data[i];
					data[i] = data[i + 2];
					data[i + 2] = b;
				}
				break;
			default:
				return null;
			}

			// Reuse existing BufferedImage if possible
			if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
				bimg = new BufferedImage(cols, rows, type);
			}
			bimg.getRaster().setDataElements(0, 0, cols, rows, data);
		} else { // mat was null
			bimg = null;
		}
		return bimg;
	}

}
