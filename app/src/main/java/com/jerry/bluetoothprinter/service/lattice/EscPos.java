package com.jerry.bluetoothprinter.service.lattice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.shellinfo.wall.remote.LEDataOutputStream;


/**
 * ESC POS 鎸囦护闆嗘敮鎸?鐩墠閲囩敤娣峰悎妯″紡, 涔熷氨浼氬皢涓嶅悓鐨勬墦鍗版満鎸囦护鍦ㄤ笉鍐茬獊鐨勬儏鍐典笅鏀惧湪涓?捣; 鏍规嵁鍚庣画娴嬭瘯鎯呭喌,鍙兘闇?鎷嗗垎鍑轰笉鍚屾墦鍗版満;
 * 
 * @author houj
 * @ID
 * @time 2015骞?鏈?鏃?涓嬪崍5:25:33
 */
public class EscPos extends OutputStream {

	private final PrinterCanvas can;

	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	private int cmdType;
	private int preCmd;

	/**
	 * 
	 */
	private final IDotFont[] fonts;

	private int scanW = 1;
	private int scanH = 1;

	private IDotFont getFont(int w, int h) {
		if (w < 1) {
			w = 1;
		}
		if (h < 1) {
			h = 1;
		}
		if (w > 16) {
			w = 16;
		}
		if (h > 16) {
			h = 16;
		}
		for (int i = fonts.length; --i >= 0;) {
			IDotFont d = fonts[i];
			if (d == null) {
				continue;
			}
			int cdb = i + 1;
			if ((w % cdb) == 0 && (h % cdb) == 0) {
				// 鏁存暟鍊?
				return d.getScanIns(w / cdb, h / cdb);
			}
		}
		for (IDotFont d : fonts) {
			if (d != null) {
				return d;
			}
		}
		return null;
	}

	public EscPos(int w, IDotFont[] fonts) {
		this.fonts = fonts.clone();
		can = new PrinterCanvas(w);
		reset();
	}

	// private int scanW;
	// private int scanH;

	private void clearCmd() {
		cmdType = 0;
		preCmd = 0;
		baos.reset();
	}

	boolean isScanW;

	private static int checkScan(int n) {
		if (n <= 0) {
			return 1;
		}
		if (n >= 16) {
			return 16;
		}
		return n;
	}

	@Override
	public void write(int b) throws IOException {
		b &= 0xFF;
		//System.out.println(b);
		if (cmdType == 0) {
			// 姝ｅ父鎵撳嵃
			if (b == '\r' || b == '\n') {
				if (isScanW) {
					isScanW = false;
					can.setFont(getFont(1, 1));
				}
				baos.write('\n');
				flush();
			} else if (b == 0x1B) {
				flush();
				cmdType = 1;
				preCmd = b;
			} else if (b == 0x1D) {
				flush();
				cmdType = 1;
				preCmd = b;
			} else if (b < ' ') {
				//System.out.println("======" + b);
				// 蹇界暐杩欎釜瀛楄妭
			} else {
				baos.write(b);
			}
		} else {
			if (cmdType == 1) {
				cmdType = b;
			}
			baos.write(b);

			if (preCmd == 0x1B) {
				switch (cmdType) {
				case 0x0E: {// 鍦ㄤ竴琛屽唴璇ュ懡浠や箣鍚庣殑鎵?湁瀛楃鍧囦互姝ｅ父瀹藉害鐨?鍊嶆墦鍗?
					isScanW = true;
					can.setFont(getFont(2, 1));
					clearCmd();
					break;
				}
				case 0x14: {// 鎵ц姝ゅ懡浠ゅ悗锛屽瓧绗︽仮澶嶆甯稿搴︽墦鍗?
					if (isScanW) {
						isScanW = false;
						can.setFont(getFont(1, 1));
					}
					clearCmd();
					break;
				}
				case 0x21: {
					if (baos.size() >= 2) {
						if ((b & 0x10) != 0) {// 绾靛悜鏀惧ぇ
							if ((b & 0x20) != 0) {// 妯悜鏀惧ぇ
								can.setFont(getFont(2, 2));
							} else {
								can.setFont(getFont(1, 2));
							}
						} else {
							if ((b & 0x20) != 0) {
								can.setFont(getFont(2, 1));
							} else {
								can.setFont(getFont(1, 1));
							}
						}
						clearCmd();
					}
					break;
				}
				case 0x24: {// 璁惧畾浠庝竴琛岀殑寮?鍒板皢瑕佹墦鍗板瓧绗︾殑浣嶇疆涔嬮棿鐨勮窛绂?
					if (baos.size() >= 3) {
						byte[] bs = baos.toByteArray();
						int l = bs[1] & 0xFF;
						int h = bs[2] & 0xFF;
						if (h == 0x80) {
							h = 0;
						}
						can.setPoint((h << 8) + l);
						clearCmd();
					}
					break;
				}
				case 0x2A: {// 鍥惧舰鎵撳嵃鍛戒护
					if (baos.size() >= 5) {
						// TODO
						//System.out.println("todo image");
						clearCmd();
					}
					break;
				}
				case 0x31: {// [鐐滅厡鎵撳嵃鏈篯 琛岄棿璺?
					if (baos.size() >= 2) {
						can.setLineSpacingDot(b);
						clearCmd();
					}
					break;
				}
				case 0x32: {// 璁剧疆瀛楃琛岄棿璺濅负1/6鑻卞
					can.setLineSpacingDot(16);
					clearCmd();
					break;
				}
				case 0x33: {// 璁剧疆琛岄棿璺濅负n鐐硅锛坣/203鑻卞锛?
					if (baos.size() >= 2) {
						can.setLineSpacingDot(b / 2);
						clearCmd();
					}
					break;
				}
				case 0x40: {// 鍒濆鍖?
//					can.skipPointY(1);
					can.setFont(getFont(1, 1));
					isScanW = false;
					// scanW = 0;
					// scanH = 0;
					clearCmd();
					break;
				}
				case 0x45: {// 璁惧畾鎴栬В闄ょ矖浣撴墦鍗版ā寮忋?
					// TODO 娌℃湁绮椾綋
					if (baos.size() >= 2) {
						clearCmd();
					}
					break;
				}
				case 0x47: {// 鏈煡鍛戒护
					// TODO
					if (baos.size() >= 2) {
						//System.out.println("unknow cmd 0x47");
						clearCmd();
					}
					break;
				}
				case 0x4A: {// 鎵撳嵃骞惰蛋绾竛鐐硅
					if (baos.size() >= 2) {
						can.skipPointY(b);
						clearCmd();
					}
					break;
				}
				case 0x4D: {// 璁剧疆瀛楀簱
					// TODO 涓嶅悓瀛楀簱
					if (baos.size() >= 2) {
						//System.out.println("set font lib:"+b);
						clearCmd();
					}
					break;
				}
				case 0x55: {// [鐐滅厡鎵撳嵃鏈篯 瀹藉害鏀惧ぇ
					if (baos.size() >= 2) {
						scanW = checkScan(b);
						can.setFont(getFont(scanW, scanH));
						clearCmd();
					}
					break;
				}
				case 0x56: {// [鐐滅厡鎵撳嵃鏈篯 楂樺害鏀惧ぇ
					if (baos.size() >= 2) {
						scanH = checkScan(b);
						can.setFont(getFont(scanW, scanH));
						clearCmd();
					}
					break;
				}
				case 0x57: {// [鐐滅厡鎵撳嵃鏈篯 鏀惧ぇ
					if (baos.size() >= 2) {
						scanW = scanH = checkScan(b);
						can.setFont(getFont(scanW, scanH));
						clearCmd();
					}
					break;
				}
				case 0x61: {// 灞呬腑瀵归綈
					if (baos.size() >= 2) {
						if (b == 0) {
							can.setAlign(PrinterCanvas.ALIGN_LEFT);
						} else if (b == 1) {
							can.setAlign(PrinterCanvas.ALIGN_CENTER);
						} else if (b == 2) {
							can.setAlign(PrinterCanvas.ALIGN_RIGHT);
						}
						clearCmd();
					}
					break;
				}
				case 0x69: {// [鐐滅厡鎵撳嵃鏈篯 鍙嶇櫧鎵撳嵃
					if (baos.size() >= 2) {
						if (b == 0) {

						} else if (b == 1) {

						}
						clearCmd();
					}
					break;
				}
				// case 0x6D: {// 缂╂斁
				// if (baos.size() >= 2) {
				// clearCmd();
				// }
				// break;
				// }
				case 0x76: {// 鍚戜富鏈轰紶閫佹墦鍗版満鐘舵?
					// TODO
					//System.out.println("鍙栧緱鎵撳嵃鏈虹姸鎬?鏈疄鐜?");
					clearCmd();
					break;
				}

				default: {
					//System.out.println("unknow cmd:0x1B " + b);
					clearCmd();
					break;
				}
				}
			} else if (preCmd == 0x1D) {
				switch (cmdType) {
				case 0x21: {// 鑺濇煰鎵撳嵃鏈?楗夸簡鍚椾娇鐢ㄧ殑鎸囦护
					if (baos.size() >= 2) {
						int w = (b >> 4) + 1;
						int h = (b & 0xF) + 1;
						can.setFont(getFont(w, h));
						clearCmd();
					}
					break;
				}
				case 0x56: {// TODO 娌＄湅鍒版枃妗? 鐚滅殑 缇庡洟鎺掗槦
					if (baos.size() >= 3) {
						//System.out.println("unknow cmd:0x56");
						clearCmd();
					}
					break;
				}
				case 0x6B: { // TODO鎵撳嵃浜岀淮鐮? 娌＄湅鍒版枃妗? 鐚滅殑 缇庡洟鎺掗槦
					if (baos.size() >= 4) {
						// 绛夊埌0
						//System.out.println("unknow cmd:0x6b");
						if (b == 0) {
							clearCmd();
						}
					}
					break;
				}
				case 0x77: {// 璁剧疆鏉″舰鐮佸搴?
					if (baos.size() >= 2) {
						clearCmd();
					}
					break;
				}
				default: {
					System.out.println("unknow cmd:0x1D " + b);
					clearCmd();
					break;
				}
				}
			}
		}
	}

	// public static byte[] getTestData() {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// try {
	// baos.write("涓枃ABCabc123\n".getBytes("GBK"));// 姝ｅ父鏂囧瓧
	// {
	// byte[] bs = { 0x1B, 0x61, 1 };
	// baos.write(bs);
	// baos.write("灞呬腑\n".getBytes("GBK"));
	// byte[] bs2 = { 0x1B, 0x61, 0 };// 鎭㈠
	// baos.write(bs2);
	// }
	// {
	// //
	// baos.write("瀹?.getBytes("GBK"));
	// byte[] bs = { 0x1B, 0x0E };
	// baos.write(bs);
	// baos.write("瀛桝a1\n".getBytes("GBK"));
	// }
	// {
	// baos.write("楂?.getBytes("GBK"));
	// byte[] bs = { 0x1B, 0x21, 0x10 };
	// baos.write(bs);
	// baos.write("瀛桝a1\n".getBytes("GBK"));
	// }
	// {
	// {
	// byte[] bs = { 0x1B, 0x21, 0x0 };
	// baos.write(bs);
	// }
	// // 琛岄棿璺?
	// byte[] bs = { 0x1B, 0x33, 0x10 };
	// baos.write(bs);
	// baos.write("琛岄棿璺漒n".getBytes("GBK"));
	// baos.write("琛岄棿璺漒n".getBytes("GBK"));
	// byte[] bs1 = { 0x1B, 0x32 };
	// baos.write(bs1);
	// baos.write("琛岄棿璺漒n".getBytes("GBK"));
	// baos.write("琛岄棿璺漒n".getBytes("GBK"));
	// // baos.write(bs);
	// // baos.write("瀛桝a1".getBytes("GBK"));
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// return baos.toByteArray();
	// }

	public static final byte QsPrinter_Reset[] = { 27, 64 };
	public static final byte QsPrinter_LN[] = { 10 };
	public static final byte c[] = { 27, 87, 1 };
	public static final byte QsPrinter_DEF_SIZE[] = { 27, 87, 0 };// 榛樿澶у皬
	public static final byte QsPrinter_DEF_SIZE_2[] = { 27, 87, 2 };// 涓ゅ?澶у皬
	public static final byte e[] = { 27, 105, 0 };

	/**
	 * 鍙栧緱娴嬭瘯鎵撳嵃鏁版嵁,杩欐槸涓?粍鎵撳嵃鎸囦护搴忓垪
	 * 
	 * @return
	 */
	public static byte[] getTestData() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write("涓枃ABCabc123\n".getBytes("GBK"));// 姝ｅ父鏂囧瓧
			{
				byte[] bs = { 0x1B, 0x57, 1 };
				baos.write(bs);
				baos.write("涓枃ABCabc123\n".getBytes("GBK"));// 姝ｅ父鏂囧瓧
			}
			{
				byte[] bs = { 0x1B, 0x57, 2 };
				baos.write(bs);
				baos.write("涓枃ABCabc123\n".getBytes("GBK"));// 姝ｅ父鏂囧瓧
			}
			{
				byte[] bs = { 0x1B, 0x57, 3 };
				baos.write(bs);
				baos.write("涓枃ABCabc123\n".getBytes("GBK"));// 姝ｅ父鏂囧瓧
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}

	// public void write(byte b[], int off, int len) throws IOException {
	//
	// }

	public void flush() throws IOException {
		String s = new String(baos.toByteArray(), "GBK");
		// s += '\n';
		can.drawString(s);
		baos.reset();
	}

	/**
	 * 鍙栧緱鏈?粓寰楀埌鐨勬墦鍗版満浣嶅浘鏁版嵁
	 * 
	 * @return
	 */
	public byte[] getData() {
		try {
			flush();
		} catch (Exception e) {
		}
		return can.getData();
	}

	/**
	 * 澶嶄綅
	 */
	public void reset() {
		scanW = 1;
		scanH = 1;
		isScanW = false;
		can.reset();
		can.setLineSpacingDot(3);
		can.setFont(getFont(1, 1));
	}

	/**
	 * 鍙栧緱浣嶅浘鏁版嵁
	 * 
	 * @param data
	 * @param w
	 *            瀹藉害
	 * @return
	 */
	public static byte[] getBmpData(byte[] data, int w) {

		final int headLen = 14 + 40 + 8;
		final int allLen = headLen + data.length;
		LEDataOutputStream os = new LEDataOutputStream(headLen);

		{
			// tagBITMAPFILEHEADER缁撴瀯
			os.writeByte('B');
			os.writeByte('M');
			os.writeInt(os.bufSize());// BMP鍥惧儚鏂囦欢鐨勫ぇ灏?
			os.writeInt(0);// 鎬讳负0
			os.writeInt(headLen);// 鏁版嵁浣嶇疆
		}
		{
			// tagBITMAPFILEHEADER缁撴瀯
			os.writeInt(0x28);// 鏈粨鏋勭殑澶у皬锛屾牴鎹笉鍚岀殑鎿嶄綔绯荤粺鑰屼笉鍚岋紝鍦╓indows涓紝姝ゅ瓧娈电殑鍊兼?涓?8h瀛楄妭=40瀛楄妭
			os.writeInt(w);// BMP鍥惧儚鐨勫搴︼紝鍗曚綅鍍忕礌
			os.writeInt(data.length * 8 / w);// BMP鍥惧儚鐨勯珮搴︼紝鍗曚綅鍍忕礌
			os.writeShort(1);// biPlanes 0
			os.writeShort(1);// biBitCount
								// BMP鍥惧儚鐨勮壊娣憋紝鍗充竴涓儚绱犵敤澶氬皯浣嶈〃绀猴紝甯歌鏈?銆?銆?銆?6銆?4鍜?2锛屽垎鍒搴斿崟鑹层?16鑹层?256鑹层?16浣嶉珮褰╄壊銆?4浣嶇湡褰╄壊鍜?2浣嶅寮哄瀷鐪熷僵鑹?

			os.writeInt(0);// 鍘嬬缉鏂瑰紡锛?琛ㄧず涓嶅帇缂╋紝1琛ㄧずRLE8鍘嬬缉锛?琛ㄧずRLE4鍘嬬缉锛?琛ㄧず姣忎釜鍍忕礌鍊肩敱鎸囧畾鐨勬帺鐮佸喅瀹?
			os.writeInt(data.length);// biSizeImage
										// BMP鍥惧儚鏁版嵁澶у皬锛屽繀椤绘槸4鐨勫?鏁帮紝鍥惧儚鏁版嵁澶у皬涓嶆槸4鐨勫?鏁版椂鐢?濉厖琛ヨ冻
			os.writeInt(0);// biXPelsPerMeter 姘村钩鍒嗚鲸鐜囷紝鍗曚綅鍍忕礌/m
			os.writeInt(0);// biYPelsPerMeter 鍨傜洿鍒嗚鲸鐜囷紝鍗曚綅鍍忕礌/m
			os.writeInt(0);// biClrUsed
							// BMP鍥惧儚浣跨敤鐨勯鑹诧紝0琛ㄧず浣跨敤鍏ㄩ儴棰滆壊锛屽浜?56鑹蹭綅鍥炬潵璇达紝姝ゅ?涓?00h=256
			os.writeInt(0);// 閲嶈鐨勯鑹叉暟锛屾鍊间负0鏃舵墍鏈夐鑹查兘閲嶈锛屽浜庝娇鐢ㄨ皟鑹叉澘鐨凚MP鍥惧儚鏉ヨ锛屽綋鏄惧崱涓嶈兘澶熸樉绀烘墍鏈夐鑹叉椂锛屾鍊煎皢杈呭姪椹卞姩绋嬪簭鏄剧ず棰滆壊
		}
		{
			// 褰╄壊琛?璋冭壊鏉匡紙color table锛?
			os.writeInt(0);
			os.writeInt(0xFFFFFF);
		}
		int skip = w / 8;

		byte[] r = new byte[allLen];
		System.arraycopy(os.toByteArray(), 0, r, 0, os.size());

		int io=headLen;
		for (int i = data.length - skip; i >= 0; i -= skip,io+=skip) {
			System.arraycopy(data, i, r, io, skip);
		}

		return r;
	}
}
