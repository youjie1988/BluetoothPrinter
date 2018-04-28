package com.jerry.bluetoothprinter.service.lattice;

import java.io.IOException;
import java.util.Arrays;

/**
 * 鐐归樀瀛椾綋鏂囦欢
 * 
 * @author houj
 * @ID 4003054FD44ED85D532980CD2B915495
 * @time 2014骞�12鏈�21鏃� 涓婂崍10:07:39
 */
public class DotFont implements IDotFont {
	/**
	 * 鍖呭惈鐨勫瓧绗�,浠庡皬鍒板ぇ鎺掑ソ搴�
	 */
	public final char[] cs;
	/**
	 * 鐐归樀鍦板潃琛�
	 */
	public final int[] dotAddr;
	/**
	 * 瀛椾綋鍚嶇О
	 */
	public final String name;
	/**
	 * 瀛楃楂樺害
	 */
	public final int fh;
	/**
	 * 瀛楃瀹藉害
	 */
	public final int fw;
	int version;
	public final byte[] dots;

	/**
	 * 閫氳繃瀛椾綋鏂囦欢鏁版嵁鍒濆鍖栧瓧浣�.
	 * 
	 * @param bs
	 *            瀛椾綋鏂囦欢鏁版嵁.
	 * @throws IOException
	 */
	public DotFont(byte[] bs) throws IOException {
		LEDataInputStream2 dis = new LEDataInputStream2(bs);
		byte[] bflag = new byte[4];
		dis.readFully(bflag);
		if (!Arrays.equals(bflag, "PDFU".getBytes("UTF8"))) {
			throw new IOException("flag err.");
		}
		version = dis.readInt();
		int charPos = dis.readInt();
		int charCount = dis.readInt();
		int dotPos = dis.readInt();
		int dotLen = dis.readInt();
		fw = dis.readInt();
		fh = dis.readInt();
		{
			int nameLen = dis.readInt();
			char[] cs = new char[nameLen];
			for (int i = 0; i < nameLen; i++) {
				cs[i] = dis.readChar();
			}
			this.name = new String(cs);
		}

		dis.skipBytes(charPos - dis.getPos());
		cs = new char[charCount];
		for (int i = 0; i < charCount; i++) {
			cs[i] = dis.readChar();
		}
		dotAddr = new int[charCount];
		for (int i = 0; i < charCount; i++) {
			dotAddr[i] = dis.readInt();
		}
		dis.skipBytes(dotPos - dis.getPos());
		dots = new byte[dotLen];
		dis.readFully(dots);
	}

	/**
	 * 鎵惧埌瀛楃鐐归樀鎵�鍦ㄧ殑鍦板潃,鏈壘鍒拌繑鍥�(-1
	 * @param c
	 * @return
	 */
	int findAddr(char c) {
		int i = Arrays.binarySearch(cs, c);
		if (i < 0) {
			return -1;
		}
		return dotAddr[i];
	}
	
	
	public boolean getCharBitMap(char c, BitMapInfo info) {
		int i = findAddr(c);
		if (i < 0) {
			return false;
		}
		info.w = dots[i] & 0xFF;
		info.bs = dots;
		info.pos = i + 1;
		info.h = this.fh;
		return true;
	}

	public String toString() {
		return name + " " + fw + "x" + fh + " " + cs.length;
	}

	@Override
	public int getFontHeight() {
		return fh;
	}

	

	@Override
	public int paintTo(char c, byte[] buf, int screenW, int x, int bottomY) {
		int pos = findAddr(c);
		if (pos < 0) {
			return x;
		}
		int w = this.dots[pos] & 0xFF;
		if (x + w > screenW) {
			return x;
		}
		pos++;
		PrinterCanvas.copy(this.dots, pos, w, this.fh, buf, x, bottomY - this.fh, screenW);
		return x + w;
	}

	@Override
	public IDotFont getScanIns(int w, int h) {
		if (w <= 0 || h <= 0) {
			return null;
		}
		return new DotFontScan(this, w, h);
	}
}
