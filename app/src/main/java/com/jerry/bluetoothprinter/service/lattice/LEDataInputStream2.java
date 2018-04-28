package com.jerry.bluetoothprinter.service.lattice;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import cn.shellinfo.wall.remote.ParamMap;

public class LEDataInputStream2 {

	private char[] cbuf;

	private final byte[] buf;
	private int pos;

	public int getPos() {
		return pos;
	}

	public LEDataInputStream2(byte[] buf) {
		this.buf = buf;
	}

	public LEDataInputStream2(byte[] buf, int pos) {
		this.buf = buf;
		this.pos = pos;
	}

	public int read() {
		return buf[pos++] & 0xFF;
	}

	public final int read(byte b[]) {
		int rlen = b.length;
		if (rlen > buf.length - pos) {
			rlen = buf.length - pos;
		}
		System.arraycopy(buf, pos, b, 0, rlen);
		pos += rlen;
		return rlen;
	}

	public final int read(byte b[], int off, int rlen) {
		if (rlen > buf.length - pos) {
			rlen = buf.length - pos;
		}
		System.arraycopy(buf, pos, b, off, rlen);
		pos += rlen;
		return rlen;
	}

	public final boolean readBoolean() {
		return buf[pos++] != 0;
	}

	public final byte readByte() {
		return buf[pos++];
	}

	public final char readChar() {
		int ch1 = buf[pos++] & 0xFF;
		int ch2 = buf[pos++] & 0xFF;
		return (char) ((ch2 << 8) | (ch1 << 0));
	}

	public final double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	public final int readInt() {
		int ch1 = buf[pos++] & 0xFF;
		int ch2 = buf[pos++] & 0xFF;
		int ch3 = buf[pos++] & 0xFF;
		int ch4 = buf[pos++] & 0xFF;
		return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
	}

	private int mark;

	public void mark() {
		mark = pos;
	}

	public void mark(int mark) {
		this.mark = mark;
	}

	public void reset() {
		pos = mark;
	}

	public final long readLong() {
		int p = this.pos;
		this.pos += 8;
		return (((long) (buf[p + 7]) << 56) + ((long) (buf[p + 6] & 255) << 48) + ((long) (buf[p + 5] & 255) << 40) + ((long) (buf[p + 4] & 255) << 32) + ((long) (buf[p + 3] & 255) << 24) + ((buf[p + 2] & 255) << 16) + ((buf[p + 1] & 255) << 8) + ((buf[p + 0] & 255) << 0));
	}

	// else if(o instanceof Double){
	// buf[len++ ]=17;
	// writeLong(Double.doubleToLongBits((Double) o));
	// }
	// else if(o instanceof double[]){
	// double ds[]=(double[]) o;
	// buf[len++ ]=18;
	// writeInt(ds.length);
	// for(double d:ds){
	// writeLong(Double.doubleToLongBits(d));
	// }
	// }

	public final short readShort() {
		int ch1 = buf[pos++] & 0xFF;
		int ch2 = buf[pos++] & 0xFF;
		return (short) ((ch2 << 8) + (ch1 << 0));
	}

	public final String readString() {
		int len = buf[pos++] & 0xFF;
		if (len == 255) {
			len = readInt();
		}
		if (cbuf == null || cbuf.length < len) {
			cbuf = new char[len];
		}
		char[] cs = cbuf;
		byte[] buf = this.buf;
		int pos = this.pos;
		for (int i = 0; i < len; i++) {
			cs[i] = (char) ((buf[pos] & 0xFF) | ((buf[pos + 1] & 0xFF) << 8));
			pos += 2;
		}
		this.pos = pos;
		return new String(cs, 0, len);
	}

	public final int readUnsignedByte() {
		return buf[pos++] & 0xFF;
	}

	public final int readUnsignedShort() {
		int ch1 = buf[pos++];
		int ch2 = buf[pos++];
		return (ch2 << 8) + (ch1 << 0);
	}

	public final int skipBytes(int n) {
		int end = pos + n;
		if (end > buf.length) {
			end = buf.length;
		}
		if (end < 0) {
			end = 0;
		}
		int r = end - pos;
		pos = end;
		return r;
	}

	public final void readFully(byte b[]) throws IOException {
		if (this.read(b, 0, b.length) != b.length) {
			throw new EOFException();
		}
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (this.read(b, off, len) != len) {
			throw new EOFException();
		}
	}

	public int available() {
		return buf.length - pos;
	}

	public final Object readObject() {
		int ID = buf[pos++];
		switch (ID) {
		case 0: {// null
			return null;
		}
		case 1: {// Integer
			return new Integer(readInt());
		}
		case 2: {// Long
			return new Long(readLong());
		}
		case 3: {// String
			return readString();
		}
		case 4: {// byte[]
			int len = readInt();
			byte[] r = new byte[len];
			read(r);
			return r;
		}
		case 5: {// short[]
			int len = readInt();
			short[] r = new short[len];
			for (int i = 0; i < len; i++) {
				r[i] = readShort();
			}
			return r;
		}
		case 6: {// int[]
			int len = readInt();
			int[] r = new int[len];
			for (int i = 0; i < len; i++) {
				r[i] = readInt();
			}
			return r;
		}
		case 7: {// long[]
			int len = readInt();
			long[] r = new long[len];
			for (int i = 0; i < len; i++) {
				r[i] = readLong();
			}
			return r;
		}
		case 8: {// List
			int len = readInt();
			ArrayList<Object> r = new ArrayList<Object>(len);
			for (int i = 0; i < len; i++) {
				r.add(readObject());
			}
			return r;
		}
		case 9: {// Map
			int len = readInt();
			HashMap<String, Object> r = new HashMap<String, Object>(len);
			for (int i = 0; i < len; i++) {
				r.put(readString(), readObject());
			}
			return r;
		}
		case 10: {// Object[]
			int len = readInt();
			Object[] r = new Object[len];
			Class<?> c = null;
			boolean same = true;
			for (int i = 0; i < len; i++) {
				r[i] = readObject();
				if (r[i] != null) {
					if (c == null) {
						c = r[i].getClass();
					} else if (r[i].getClass() != c) {
						same = false;
					}
				}
			}
			if (same && c != null) {
				Object tr = Array.newInstance(c, len);
				for (int i = 0; i < len; i++) {
					Array.set(tr, i, r[i]);
				}
				return tr;
			}
			return r;
		}
		case 11: {
			return new Boolean(readBoolean());
		}
		case 12: {
			int len = readInt();
			boolean[] r = new boolean[len];
			int pos = this.pos;
			byte[] buf = this.buf;
			for (int i = 0; i < len; i++) {
				r[i] = buf[pos++] != 0;
			}
			this.pos = pos;
			return r;
		}
		case 14: {
			return readDouble();
		}
		case 15: {
			int len = readByte();
			String[] ks = new String[len];
			Object[] vs = new Object[len];
			for (int i = 0; i < len; i++) {
				ks[i] = readString();
				vs[i] = readObject();
			}
			return new ParamMap(ks, vs);
		}
		case 16: {
			int len = readInt();
			char[] cs = new char[len];
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; i++) {
				cs[i] = (char) ((buf[pos] & 0xFF) | ((buf[pos + 1] & 0xFF) << 8));
				pos += 2;
			}
			this.pos = pos;
			return cs;
		}
		case 17: {
			long n = readLong();
			return Double.longBitsToDouble(n);
		}
		case 18: {
			int len = readInt();
			double[] ds = new double[len];
			for (int i = 0; i < len; i++) {
				long n = readLong();
				ds[i] = Double.longBitsToDouble(n);
			}
			return ds;
		}

		default:
			throw new RuntimeException("unknown data type:" + ID);
		}
	}
}
