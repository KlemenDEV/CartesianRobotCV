package org.beetron;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class SerialTask implements Runnable {

	private SerialPort comPort;

	public SerialPort getSerialPort() {
		return this.comPort;
	}

	@Override public void run() {
		for (SerialPort port : SerialPort.getCommPorts()) {
			if (port.getDescriptivePortName().contains("USB Serial Port")) {
				System.err.println(port.getDescriptivePortName());
				this.comPort = port;
			}
		}

		System.out.println("Opening serial port");

		comPort.openPort();

		comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 20, 20);
		comPort.setComPortParameters(115200, 8, 1, SerialPort.NO_PARITY);

		comPort.addDataListener(new SerialPortDataListener() {
			@Override public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;

				byte[] newData = new byte[comPort.bytesAvailable()];
				comPort.readBytes(newData, newData.length);
				System.out.println(new String(newData));
			}
		});

		//this.comPort.writeBytes(data, data.length);
	}
}
