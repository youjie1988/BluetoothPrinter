package com.jerry.bluetoothprinter.service.lattice;

/**
 * 
 * @author houj
 * @ID
 * @time 2015骞�5鏈�8鏃� 涓嬪崍2:32:30
 */
public class DotFontScan implements IDotFont {

	private final DotFont df;
	private final int sw;
	private final int sh;

	/**
	 * 閫氳繃鍘熷鐐归樀瀛椾綋鏋勯�犳媺浼稿瓧浣�.
	 * 
	 * @param df
	 * @param scanW
	 * @param scanH
	 */
	public DotFontScan(DotFont df, int scanW, int scanH) {
		this.df = df;
		this.sw = scanW;
		this.sh = scanH;
	}

	/**
	 * 鍙栧緱瀹藉害鎷変几
	 * 
	 * @return
	 */
	public int getScanW() {
		return sw;
	}

	/**
	 * 鍙栧緱楂樺害鎷変几
	 * 
	 * @return
	 */
	public int getScanH() {
		return sh;
	}

	/**
	 * 鍙栧緱鍩虹瀛椾綋
	 * 
	 * @return
	 */
	public DotFont getBaseFont() {
		return df;
	}

	@Override
	public boolean getCharBitMap(char c, BitMapInfo info) {
		boolean r = df.getCharBitMap(c, info);
		if (r) {
			info.h *= sh;
			info.w *= sw;
		}
		return r;
	}

	@Override
	public int getFontHeight() {
		return df.fh * sh;
	}

	@Override
	public int paintTo(char c, byte[] buf, int screenW, int x, int bottomY) {
		int pos = df.findAddr(c);
		if (pos < 0) {
			return x;
		}
		int w = df.dots[pos] & 0xFF;
		if (x + w * sw > screenW) {
			return x;
		}
		pos++;
		PrinterCanvas.copyScan(df.dots, pos, w, df.fh, buf, x, bottomY - df.fh * sh, screenW, sw, sh);
		return x + w * sw;
	}

	@Override
	public IDotFont getScanIns(int w, int h) {
		if (w <= 0 || h <= 0) {
			return null;
		}
		return new DotFontScan(df, sw * w, sh * h);
	}

}
