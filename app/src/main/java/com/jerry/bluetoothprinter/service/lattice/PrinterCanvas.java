package com.jerry.bluetoothprinter.service.lattice;

import java.util.Arrays;

/**
 * 鐐归樀鎵撳嵃鐢诲竷.
 * 
 * @author houj
 * @ID 0B6C152211DB7EBD4A7A235821A11195
 * @time 2014骞�12鏈�21鏃� 涓嬪崍7:01:37
 */
public class PrinterCanvas {
	private byte[] bs;
	// 鍥剧墖瀹藉害
	private final int width;
	private final int widthByte;

	/**
	 * 褰撳墠浣嶇疆
	 */
	private int curX;
	private int curY;
	/**
	 * 褰撳墠琛屽簳杈�
	 */
	private int curLineBottom;

	private IDotFont curFont;// 褰撳墠瀛椾綋
	private int preLineGap;// 琛岀粨鏉熸椂闇�瑕佹坊鍔犵殑闂撮殭
	private double lineSpacing;
	private int dot;

	/**
	 * 閫氳繃鐢诲竷瀹藉害鏋勯�犵敾甯�.
	 * 
	 * @param w
	 */
	public PrinterCanvas(int w) {
		this.width = w;
		this.widthByte = w / 8;
		bs = new byte[w * 100 / 8];
		reset();
	}

	/**
	 * 鐢诲竷澶嶄綅璇风┖.
	 */
	public void reset() {
		curFont = null;
		curX = 0;
		curY = 5;
		curLineBottom = 5;
		lineSpacing = 0.0;
		preLineGap = 0;
		dot = 0;
		align = ALIGN_LEFT;
		Arrays.fill(bs, (byte) 0);// 鏁版嵁娓呴浂
	}

	/**
	 * 璁剧疆瀛椾綋.
	 * 
	 * @param df
	 */
	public void setFont(IDotFont font) {
		IDotFont pre = curFont;
		this.curFont = font;
		if (pre == null) {
			newLine(false);
		} else if (font.getFontHeight() > (curLineBottom - curY)) {
			// 鏂扮殑瀛椾綋瑕侀珮涓�浜�,闇�瑕佹妸鍚屼竴琛屽墠闈㈢殑鏁版嵁鍚戜笅绉诲姩涓�鐐�
			int add = widthByte * (font.getFontHeight() - (curLineBottom - curY));
			if (curLineBottom * widthByte + add > bs.length) {
				// 缂撳瓨鍖哄お灏�,闇�瑕佸鍔�
				bs = Arrays.copyOf(bs, bs.length * 2);
			}
			System.arraycopy(bs, widthByte * curY, bs, widthByte * curY + add, widthByte * (curLineBottom - curY));
			Arrays.fill(bs, curY * widthByte, curY * widthByte + add, (byte) 0);
			curLineBottom = curY + font.getFontHeight();
		}
		// if (pre == null || font.getFontHeight() != pre.getFontHeight()) {
		// newLine(false);
		// }
	}

	/**
	 * 寮�濮嬫柊鐨勪竴琛�
	 * 
	 * @param force
	 *            鏄惁寮哄埗鎹㈣, 涓嶅己鍒舵崲琛屾椂,濡傛灉浣嶇疆鍦ㄨ棣�,灏变笉鎹㈣
	 */
	private void newLine(boolean force) {
		int mv = 0;
		curX = (curX + 7) >> 3;
		if (force || curX != 0) {
			if (align == ALIGN_CENTER) {
				// 灞呬腑绉诲姩
				mv = (widthByte - curX) / 2;
			} else if (align == ALIGN_RIGHT) {
				// 鍙冲榻愮Щ鍔�
				mv = widthByte - curX;
			}
			if (mv != 0) {
				int st = curY * widthByte;
				int end = curLineBottom * widthByte;
				while (st < end) {
					System.arraycopy(bs, st, bs, st + mv, curX);
					Arrays.fill(bs, st, st + mv, (byte) 0);
					st += widthByte;
				}
			}
			curX = 0;
			curY = curLineBottom;
			curY += preLineGap + dot;
		}
		if (curFont != null) {
			preLineGap = (int) (curFont.getFontHeight() * lineSpacing + .5);
			curY += preLineGap + dot;
			curLineBottom = curY + curFont.getFontHeight();
		}
		if (curLineBottom * widthByte >= bs.length) {
			// 缂撳瓨鍖轰笉瓒�,鎵╁ぇ缂撳瓨鍖�
			bs = Arrays.copyOf(bs, bs.length * 2);
		}
	}

	/**
	 * 璁剧疆琛岄棿璺�.
	 * 
	 * @param dh
	 */
	public void setLineSpacing(double dh) {
		lineSpacing = dh / 2;
	}

	/**
	 * 璁剧疆鐐规牸寮忕殑琛岄棿璺�
	 * 
	 * @param dot
	 */
	public void setLineSpacingDot(int dot) {
		if (dot >= 0 && dot <= 256) {
			this.dot = dot;
		}
	}

	private final BitMapInfo info = new BitMapInfo();

	/**
	 * 澶嶅埗浣嶅浘
	 * 
	 * @param ss
	 * @param pos
	 * @param fw
	 * @param fh
	 * @param ds
	 * @param dx
	 * @param dy
	 * @param dw
	 */
	static void copy(byte[] ss, int pos, int fw, int fh, byte[] ds, int dx, int dy, int dw) {
		int is = pos * 8;
		int n = 0;
		for (int y = 0; y < fh; y++) {
			int id = (dy + y) * dw + dx;
			for (int x = 0; x < fw; x++, is++, id++) {
				n = (ss[is >> 3] >> (is & 7)) & 1;
				ds[id >> 3] |= (n << (7 - (id & 7)));
			}
		}
	}

	/**
	 * 鎷変几澶嶅埗浣嶅浘
	 * 
	 * @param ss
	 * @param pos
	 * @param fw
	 * @param fh
	 * @param ds
	 * @param dx
	 * @param dy
	 * @param dw
	 * @param scanW
	 * @param scanH
	 */
	static void copyScan(byte[] ss, int pos, int fw, int fh, byte[] ds, int dx, int dy, int dw, int scanW, int scanH) {
		int is = pos * 8;
		int n = 0;
		for (int y = 0; y < fh; y++) {
			int id = (dy + y * scanH) * dw + dx;
			for (int x = 0; x < fw; x++, is++) {
				n = (ss[is >> 3] >> (is & 7)) & 1;
				for (int psy = 0; psy < scanH; psy++) {
					int tid = id + psy * dw + x * scanW;
					for (int psx = 0; psx < scanW; psx++) {
						ds[(tid + psx) >> 3] |= (n << (7 - ((tid + psx) & 7)));
					}
				}
			}
		}
	}

	/**
	 * 璺宠繃鎸囧畾鏁扮洰鐨勭偣,涔熷氨鏄┖鐧藉尯;
	 * 
	 * @param n
	 */
	public void skipPoint(int n) {
		if (n > 0) {
			this.curX += n;
			if (this.curX < 0 || this.curX >= this.width) {
				newLine(false);
			}
		}
	}

	public void setPoint(int n) {
		if (n >= 0) {
			if (n >= this.width) {
				newLine(false);
			} else {
				this.curX = n;
			}
		}
	}

	/**
	 * 璺宠繃鎸囧畾鏁扮洰鐨勭偣,涔熷氨鏄┖鐧藉尯;
	 * 
	 * @param n
	 */
	public void skipPointY(int n) {
		if (n > 0 && n <= 2000) {
			newLine(false);
			this.curY += n;
			this.curLineBottom += n;
			if (this.curLineBottom * widthByte < bs.length) {
				int len = this.curLineBottom * widthByte;
				if (len < bs.length * 2) {
					len = bs.length * 2;
				}
				bs = Arrays.copyOf(bs, len);
			}
		}
	}

	/**
	 * 缁樺埗瀛楃.
	 * 
	 * @param s
	 */
	public void drawString(String s) {
		if (curFont == null) {
			System.err.println("not set font.");
			return;
		}
		s = s.replace("\r\n", "\n");
		s = s.replace('\r', '\n');
		BitMapInfo info = this.info;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\n') {
				// 鎹㈣绗︽崲琛�
				newLine(true);
			} else {
				if (curFont.getCharBitMap(c, info)) {
					// 瀛楃鎵惧埌浜�
					if (curX + info.w > this.width) {
						newLine(false);
					}
					// 寮�濮嬬粯鍥�,涓嬪榻�
					curFont.paintTo(c, this.bs, this.width, this.curX, this.curLineBottom);
					// copy(info.bs, info.pos, info.w, info.h, this.bs,
					// this.curX, this.curY, this.width);
					curX += info.w;
				}
			}
		}
	}

	/**
	 * 鍙栧緱缁樺浘鏁版嵁
	 * 
	 * @return
	 */
	public byte[] getData() {
		int len = curLineBottom * widthByte;
		return Arrays.copyOf(bs, len);
	}

	/**
	 * 璁＄畻瀛楃鏁�,姹夊瓧绠椾袱涓瓧绗�,ASCII瀛楃绠椾竴涓�
	 * 
	 * @param s
	 * @return
	 */
	public static int charCount(String s) {
		int n = s.length();
		int r = n;
		for (int i = 0; i < n; i++) {
			if (s.charAt(i) >= 0x80) {
				r++;
			}
		}
		return r;
	}

	/**
	 * 缁樺埗浣嶅浘
	 * 
	 * @param img
	 *            鍥剧墖鐐归樀鏁版嵁
	 * @param len
	 *            鏁版嵁闀垮害
	 */
	public void drawBitImage(byte[] img, int len) {
		if (this.curX != 0) {
			newLine(false);
		}
		int h = len / widthByte;
		len = widthByte * h;
		int curPos = curLineBottom * widthByte;

		if (curPos + len >= this.bs.length) {
			// 缂撳瓨鍖轰笉瓒�
			int need = Math.max(curPos + len, bs.length * 2);
			bs = Arrays.copyOf(bs, need);
		}
		System.arraycopy(img, 0, this.bs, curPos, len);
		curLineBottom += h;
		curX = 1;
		newLine(true);
	}

	public final static int ALIGN_LEFT = 0;
	public final static int ALIGN_CENTER = 1;
	public final static int ALIGN_RIGHT = 2;

	private int align;

	public void setAlign(int n) {
		align = n;
	}

}
