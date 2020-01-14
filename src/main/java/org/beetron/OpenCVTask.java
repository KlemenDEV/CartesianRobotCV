package org.beetron;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenCVTask implements Runnable {

	private Main main;

	private transient KeyPoint[] points;

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
					Imgproc.resize(frame, frame, new Size(640, 480));
					Imgproc.GaussianBlur(frame, frame, new Size(0, 0), 1, 1);

					FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);

					MatOfKeyPoint matOfKeyPoint = new MatOfKeyPoint();
					blobDetector.detect(frame, matOfKeyPoint);

					points = matOfKeyPoint.toArray();

					result = matToBufferedImage(frame, result);

					if (result != null) {
						Graphics g = result.getGraphics();
						g.setColor(Color.green);

						for (KeyPoint point : points) {
							g.fillOval((int) point.pt.x - 3, (int) point.pt.y - 3, 6, 6);
						}

						g.dispose();
					}

					main.setDisplay(result);
				} else {
					break;
				}
			}
		}
		camera.release();
	}

	List<Point> getPoints() {
		List<Point> outputPoints = new ArrayList<>();
		for (KeyPoint point : points) {
			outputPoints.add(new Point((float) point.pt.x, (float) point.pt.y));
		}
		return outputPoints;
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
