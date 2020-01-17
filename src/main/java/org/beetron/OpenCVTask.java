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

						for (Point point : getPointsRaw()) {
							g.fillOval((int) point.getX() - 3, (int) point.getY() - 3, 6, 6);
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

	// minx: 195 miny: 240
	// maxx: 490 maxy: 357
	private List<Point> getPointsRaw() {
		List<Point> outputPoints = new ArrayList<>();
		for (KeyPoint point : points) {
			float x = (float) point.pt.x;
			float y = (float) point.pt.y;

			if(x < 195 || y < 240 || x > 490 || y > 370)
				continue;

			outputPoints.add(new Point(x, y));
		}
		return outputPoints;
	}


	List<Point> getPoints() {
		return tranformPoints(getPointsRaw());
	}

	float a = 0.0000803573f, b = 0.0579862f, c = -12.3441f;
	float d = 0.0573585f, e = 0.00102836f, f = -5.8176f;

	private List<Point> tranformPoints(List<Point> points) {
		for (Point point : points) {
			float x = point.getX();
			float y = point.getY();

			float rx = a*x + b*y + c;
			float ry = d*x + e*y + f - 0.1f;

			point.setX(rx);
			point.setY(ry);
		}
		return points;
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
