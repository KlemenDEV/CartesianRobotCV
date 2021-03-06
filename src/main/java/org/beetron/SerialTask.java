package org.beetron;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.List;

public class SerialTask implements Runnable {

	private Main main;

	private SerialPort comPort;

	void setMain(Main main) {
		this.main = main;
	}

	void sendPoints(List<Point> points) {
		for (Point point : points) {
			sendData(new byte[] { 0x02 });
			wait(20);
			sendData(float2bytearr(point.getX()));
			wait(30);
			sendData(float2bytearr(point.getY()));
		}
	}

	void sendData(byte[] data) {
		if (comPort != null)
			comPort.writeBytes(data, data.length);
	}

	@Override public void run() {
		for (SerialPort port : SerialPort.getCommPorts()) {
			if (port.getDescriptivePortName().contains("STLink Virtual COM Port")) {
				this.comPort = port;
			}
		}

		main.printToLog("Opening serial port\n");

		if (comPort == null) {
			main.printToLog("Failed to open serial port\n");
			return;
		}

		comPort.openPort();

		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 20, 20);
		comPort.setComPortParameters(115200, 8, 1, SerialPort.NO_PARITY);

		main.printToLog(comPort.getDescriptivePortName() + " opened\n");

		main.printToLog("Reset board using onboard button\n\n");

		comPort.addDataListener(new SerialPortDataListener() {
			String currentData = "";

			@Override public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;

				byte[] data = new byte[comPort.bytesAvailable()];
				comPort.readBytes(data, data.length);

				currentData += new String(data);
				if (currentData.contains("\r\n")) {
					String[] lines = currentData.split("\n\n");
					if (lines.length == 1) {
						main.printToLog(new String(lines[0].getBytes()));
						currentData = "";
					} else {
						for (int i = 0; i < lines.length - 1; i++)
							main.printToLog(new String(lines[i].getBytes()));
						currentData = lines[lines.length - 1];
					}
				}
			}
		});
	}

	private void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private byte[] float2bytearr(float num) {
		int bits = Float.floatToIntBits(num);
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (bits & 0xff);
		bytes[1] = (byte) ((bits >> 8) & 0xff);
		bytes[2] = (byte) ((bits >> 16) & 0xff);
		bytes[3] = (byte) ((bits >> 24) & 0xff);
		return bytes;
	}

}
