package com.jerry.bluetoothprinter.service.lattice;

/**
 * 鐐归樀瀛椾綋缁勫悎.
 * 
 * @author houj
 * @ID
 * @time 2014骞�12鏈�21鏃� 涓嬪崍7:19:47
 */
public class DotFontGroup implements IDotFont {
	private final IDotFont[] ds;
	private final int fh;

	public DotFontGroup(IDotFont[] ds) {
		this.ds = ds;
		fh = ds[0].getFontHeight();
		for (IDotFont d : ds) {
			if (d.getFontHeight() != fh) {
				throw new RuntimeException("font size!= .");
			}
		}
	}

	/**
	 * 鍙栧緱瀛椾綋缁勫悎瀛愬厓绱�.
	 * @return
	 */
	public IDotFont[] getItems() {
		return ds.clone();
	}

	public boolean getCharBitMap(char c, BitMapInfo info) {
		for (IDotFont d : ds) {
			if (d.getCharBitMap(c, info)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DotFontGroup:");
		for (IDotFont d : ds) {
			sb.append(d).append(" ;");
		}
		return sb.toString();
	}

	@Override
	public int getFontHeight() {
		return fh;
	}

	@Override
	public int paintTo(char c, byte[] buf, int screenW, int x, int bottomY) {
		for (IDotFont d : ds) {
			int r = d.paintTo(c, buf, screenW, x, bottomY);
			if (r > x) {
				return r;
			}
		}
		return x;
	}

	@Override
	public IDotFont getScanIns(int w, int h) {
		IDotFont[] ds = this.ds.clone();
		for (int i = 0; i < ds.length; i++) {
			ds[i] = ds[i].getScanIns(w, h);
		}
		return new DotFontGroup(ds);
	}
}
