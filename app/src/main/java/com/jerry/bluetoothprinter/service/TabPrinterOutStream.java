package com.jerry.bluetoothprinter.service;

import java.io.IOException;
import java.io.OutputStream;

import android.util.Log;

import com.jerry.bluetoothprinter.action.HEX;

public class TabPrinterOutStream {

	private final String TAG = "PrinterOutStream";
	
	private static byte[] buf = new byte[8192*10];
	private static int outLen;
	private static long writeTime;
	private static int bufLength = 0;
	
	public void write(int oneByte) throws IOException {
		// TODO Auto-generated method stub
		byte[] data = new byte[1];
		data[0] = (byte)oneByte;
		Log.i("PrinterOutStream", "add buffer oneByte:" + oneByte);
		appendData(data, 1);
	}


	public void write(byte[] buffer) throws IOException {
		// TODO Auto-generated method stub
		Log.i("PrinterOutStream", "add buffer:" + HEX.bytesToHex(buffer));
		appendData(buffer, buffer.length);
	}
	

	public void write(byte[] buffer, int offset, int count) throws IOException {
		// TODO Auto-generated method stub
		Log.i("PrinterOutStream", "add buffer@@:" + HEX.bytesToHex(buffer));
		appendData(buffer, buffer.length);
	}
	
	public synchronized void appendData(byte[] bs, int len) {
		 Log.i(TAG, " appendData  len:" + len);
		if(bs != null && len != 0) {
			System.arraycopy(bs, 0, buf, bufLength, len);
			bufLength = bufLength + len;
		}
	}
	
	public byte[] getDataBuf() {
		 if(bufLength != 0) {
			 byte[] printBuf = new byte[bufLength];
			 System.arraycopy(buf, 0, printBuf, 0, bufLength);
			 Log.i(TAG, " submitPrint:" + "   bufLength:" + bufLength);
			 reset();
			 return printBuf;
		 } else {
			 reset();
		 }
		 return null;
	}
	
	private void reset() {
		buf = new byte[8192];
		bufLength = 0;
	}
}
