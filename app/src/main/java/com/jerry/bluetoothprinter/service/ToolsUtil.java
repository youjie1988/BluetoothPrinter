package com.jerry.bluetoothprinter.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.shellinfo.wall.remote.ParamMap;

public class ToolsUtil {
	public static byte[] short2BytesHL(short n) {
		byte[] b = new byte[2];
		b[1] = (byte) (n & 0xff);
		b[0] = (byte) (n >> 8 & 0xff);
		return b;
	}

	/**
	 * 灏哹yte 杞崲鎴恠hort
	 * 
	 * @param b
	 * @return
	 */
	public static short bytes2Short(byte[] b) {
		return (short) (((b[0] << 8) | b[1] & 0xff));
	}

	/**
	 * 灏哹yte 杞崲鎴恑nt
	 * 
	 * @param bb
	 * @return
	 */
	public static int bytes2Int(byte[] bb) {
		return (int) ((((bb[0] & 0xff) << 24) | ((bb[1] & 0xff) << 16) | ((bb[2] & 0xff) << 8) | ((bb[3] & 0xff) << 0)));
	}

	public static int bytes2IntC(byte[] bb) {
		return (int) ((((bb[3] & 0xff) << 24) | ((bb[2] & 0xff) << 16) | ((bb[1] & 0xff) << 8) | ((bb[0] & 0xff) << 0)));
	}

	/**
	 * 灏唅nt杞负浣庡瓧鑺傚湪鍚庯紝楂樺瓧鑺傚湪鍓嶇殑byte鏁扮粍
	 * 
	 * @param
	 */
	public static byte[] int2BytesHL(int n) {
		byte[] b = new byte[4];
		b[3] = (byte) (n & 0xff);
		b[2] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n >> 16 & 0xff);
		b[0] = (byte) (n >> 24 & 0xff);
		return b;
	}

	/**
	 * 灏嗗瓧鑺傛暟缁勮浆鎹负瀛楃
	 * 
	 * @param bs
	 * @return
	 */
	public static String bytesToHexString(byte[] bs) {
		if (bs == null)
			return "";
		StringBuffer sb = new StringBuffer();
		String hex = "";
		for (int i = 0; i < bs.length; i++) {
			hex = Integer.toHexString(bs[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(' ');
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 鍥剧墖鍘昏壊,杩斿洖鐏板害鍥剧墖
	 * 
	 * @param bmpOriginal
	 *            浼犲叆鐨勫浘鐗�
	 * @return 鍘昏壊鍚庣殑鍥剧墖
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawColor(Color.WHITE);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/*
	 * 鏉″舰鐮�
	 */

	public static Bitmap createBarBitmap(String contents, int barType, int desiredWidth, int desiredHeight)
			throws WriterException {
		// 鐢熸垚涓�淮鏉＄爜,缂栫爜鏃舵寚瀹氬ぇ灏�涓嶈鐢熸垚浜嗗浘鐗囦互鍚庡啀杩涜缂╂斁,杩欐牱浼氭ā绯婂鑷磋瘑鍒け璐�
		// int imgwidth = 400;
		// int imgheight = imgwidth >> 2;
		BarcodeFormat format = BarcodeFormat.CODE_128;
		if (barType == 4) {
			format = BarcodeFormat.CODE_39;
		} else if (barType == 73) {
			format = BarcodeFormat.CODE_128;
		}
		BitMatrix matrix = new MultiFormatWriter().encode(contents, format, desiredWidth, desiredHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// 閫氳繃鍍忕礌鏁扮粍鐢熸垚bitmap,鍏蜂綋鍙傝�api
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	// public static Bitmap createBarBitmap(String contents,
	// int barType, int desiredWidth, int desiredHeight) {
	// final int WHITE = 0xFFFFFFFF;
	// final int BLACK = 0xFF000000;
	// MultiFormatWriter writer = new MultiFormatWriter();
	// BitMatrix result = null;
	// BarcodeFormat format = BarcodeFormat.CODE_39;
	// if(barType == 0) {
	// format = BarcodeFormat.CODE_39;
	// } else if(barType == 1) {
	// format = BarcodeFormat.CODE_128;
	// }
	// Log.i("WeiPos", "desiredWidth:" + desiredWidth + "  desiredHeight:" +
	// desiredHeight);
	// try {
	// result = writer.encode(contents, format, desiredWidth,
	// desiredHeight, null);
	// } catch (WriterException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// int width = result.getWidth();
	// int height = result.getHeight();
	// int[] pixels = new int[width * height];
	// // All are 0, or black, by default
	// for (int y = 0; y < height; y++) {
	// int offset = y * width;
	// for (int x = 0; x < width; x++) {
	// pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
	// }
	// }
	//
	// Bitmap bitmap = Bitmap.createBitmap(width, height,
	// Bitmap.Config.ARGB_8888);
	// bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	// return bitmap;
	// }

	/**
	 * 鐢ㄥ瓧绗︿覆鐢熸垚浜岀淮鐮佸浘鐗�
	 * 
	 * @param str
	 * @return
	 * @throws WriterException
	 */
	public static final Bitmap createQRBitmap(String str, int qrWidth, int qrHeight, boolean closing) {
		// 鐢熸垚浜岀淮鐭╅樀,缂栫爜鏃舵寚瀹氬ぇ灏�涓嶈鐢熸垚浜嗗浘鐗囦互鍚庡啀杩涜缂╂斁,杩欐牱浼氭ā绯婂鑷磋瘑鍒け璐�
		Bitmap bitmap = null;
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, qrWidth, qrHeight);
			int width = 0;
			int height = 0;
			int[] pixels = null;
			if (closing) {
				int[] rect = matrix.getEnclosingRectangle();
				width = rect[2];
				height = rect[3];
				int startx = rect[0];
				int starty = rect[1];
				// 浜岀淮鐭╅樀杞负涓�淮鍍忕礌鏁扮粍,涔熷氨鏄竴鐩存í鐫�帓浜�
				pixels = new int[width * height];
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						if (matrix.get(startx + x, starty + y)) {
							pixels[y * width + x] = 0xff000000;
						} else {
							pixels[y * width + x] = -1;
						}
					}
				}
			} else {
				width = matrix.getWidth();
				height = matrix.getHeight();
				pixels = new int[width * height];

				for (int y = 0; y < height; y++) {
					int offset = y * width;
					for (int x = 0; x < width; x++) {
						pixels[(offset + x)] = (matrix.get(x, y) ? -16777216 : -1);
					}
				}
			}
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			// 閫氳繃鍍忕礌鏁扮粍鐢熸垚bitmap,鍏蜂綋鍙傝�api
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 鑾峰緱鐐归樀浣嶅浘
	 * 
	 * @param v
	 * @return
	 */
	public static Bitmap getViewBitmap(View v) {
		Bitmap bitmap = null;
		if (v != null) {
			int w = v.getMeasuredWidth();
			int h = v.getMeasuredHeight();
			if (w > 0 && h > 0) {
				bitmap = Bitmap.createBitmap(w, h, Config.RGB_565);
				Canvas c = new Canvas(bitmap);
				v.draw(c);
				c = null;
			}
		}
		return bitmap;
	}


	/**
	 * 閲嶈鐏睆璁℃椂(60绉掔伃灞�锛屽鏋滃睆骞曟槸鍗婁寒鐘舵�锛屽睆骞曞皢浼氬彉鎴愬叏浜�
	 * 
	 * @param context
	 */
	public static final void resetScreenOffTime(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		pm.userActivity(SystemClock.uptimeMillis(), false);
		pm = null;
	}


	/**
	 * Bitmap 杞琤yte[]
	 * 
	 * @param bm
	 * @return
	 */
	public static final byte[] Bitmap2Bytes(Bitmap bm) {
		if(bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 灏嗗僵鑹插浘杞崲涓洪粦鐧藉浘
	 * 
	 * @param
	 * @return 杩斿洖杞崲濂界殑浣嶅浘
	 */
	public static Bitmap convertToBlackWhite(Bitmap bmp) {
		int width = bmp.getWidth(); // 鑾峰彇浣嶅浘鐨勫
		int height = bmp.getHeight(); // 鑾峰彇浣嶅浘鐨勯珮
		int[] pixels = new int[width * height]; // 閫氳繃浣嶅浘鐨勫ぇ灏忓垱寤哄儚绱犵偣鏁扮粍

		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];

				int red = ((grey & 0x00FF0000) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);

				grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}
		Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);

		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
		return newBmp;
	}

	/**
	 * 閲嶅惎璁惧锛岄渶瑕佺郴缁焤oot鏉冮檺
	 * 
	 * @param context
	 */
	public static void reStart(Context context) {

		try {
			// 闇�閰嶇疆android:sharedUserId="android.uid.system"
			Intent i = new Intent(Intent.ACTION_REBOOT);
			i.putExtra("nowait", 1);
			i.putExtra("interval", 1);
			i.putExtra("window", 0);
			context.sendBroadcast(i);
			System.out.println("---------reboot ok-------");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("---------reboot error-------");
			e.printStackTrace();
			// 鏉�璇ュ簲鐢ㄨ繘绋�
			try {
				Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}

		}

	}

	/**
	

	@SuppressLint("DefaultLocale")
	private static String lookWangUIVersion(int otaInner, String uiTempVersion) {
		int local = getLocalWangUIVersion(uiTempVersion);
		int tryLast = getLastTryedWangUIVersion(uiTempVersion);

		// TODO:
		// 2016.01.27
		// POS绔煫姝ｏ紝鍦ㄥ畨瑁呰繃绋嬩腑鏂垨鍑洪敊鐨勬儏鍐典笅锛屾湁鍙兘瀵艰嚧鐗堟湰娣蜂贡涓斾笉鏄撳療瑙�
		// 杩欑鎯呭喌姝ｈ鎿嶄綔涓嶆槗鍙戠敓锛屼絾鑻ラ敊浜嗗氨闅炬煡
		// 宸叉彁杩囷紝浣嗛壌浜庣洓鏈殑鎿嶄綔绠�崟鎬т笌鏈嶅姟鍣ㄧ鏁版嵁鐨勪竴鑷存�鑰冭檻锛岃瑕佹眰缁х画杩欐牱鍋�
		int judgeVersion = Math.max(local, otaInner);
		if (tryLast <= judgeVersion) {
			return "" + judgeVersion;
		}
		return String.format("%d.%d", judgeVersion, tryLast);
	}

	public static int getLocalWangUIVersion(String uiVersion) {
		int ver = getIntPart(uiVersion,0);
		return ver;
	}

	public static int getLastTryedWangUIVersion(String uiVersion) {
		int ver = getIntPart(uiVersion,1);
		return ver;
	}

	private static int getIntPart(String uiVersion,int index) {
		if ( UtilFunctions.isEmptyString(uiVersion) ) {
			return 0;
		}
		String[] parts = uiVersion.split("[.]");
		if ( index < 0 || index >= parts.length ) {
			return 0;
		}
		int part = 0;
		try {
			part = Integer.parseInt(parts[index]);
		} catch( Exception e ) {
		}

		return part;
	}

	public static String getDeviceName() {
		int type = BuildConfiger.getDeviceType();
		switch (type) {
			case Constant.POS_DTYPE_POS2:
				return ("WPOS2");
			case Constant.POS_DTYPE_POS2S:
				return ("WPOS2S");
			case Constant.POS_DTYPE_POS3:
				return ("WPOS-3");
			case Constant.POS_DTYPE_TAB:
				return ("WPOS-TAB");
			case Constant.POS_DTYPE_ONE_PLUS:
				return ("WPOS-3");
			case Constant.POS_DTYPE_MINI:
				return ("WPOS-MINI");
            case Constant.POS_DTYPE_WMI_LITE:
                return ("WPOS-MINI-LITE");
            case Constant.POS_DTYPE_SR236:
                return ("WPOS-MINI-SR236");
			case Constant.POS_DTYPE_NET5:
				return ("WPOS-NET5");
			case Constant.POS_DTYPE_MINI2:
				return ("WPOS-MINI-2");
		}
		Log.w("MGOD",String.format("INVALID DEVICE TYPE %d.!",type));
		return "WPOS";
	}

	/**
	 * 灏唈son 鏁扮粍杞崲涓篗ap 瀵硅薄
	 *
	 * @param param
	 * @return
	 */
	public static JSONObject parseMap2JSON(ParamMap param) {
		try {
			String[] keyIter = param.keys();
			String key;
			Object value;
			JSONObject json = new JSONObject();
			for (int i = 0; i < keyIter.length; i++) {
				key = keyIter[i];
				value = param.get(key);
				json.put(key, value);
			}
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}


	
	


	private static final double EARTH_RADIUS = 6378137.0;
	/**
	 * 杩斿洖鍗曚綅鏄背
	 *
	 * @param longitude1
	 * @param latitude1
	 * @param longitude2
	 * @param latitude2  double lo1 = 108.90, la1 = 34.1;// 绗竴涓粡绾害 double lo2 =
	 *                   115.4648060, la2 = 38.8738910;// 绗簩涓粡绾害
	 * @return
	 */
	public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {

		System.out.println("oldLongitude = " + longitude1 + ", oldLatitude = " + latitude1 + ", newLongitude = "
				+ longitude2 + ", newLatitude = " + latitude2);

		double Lat1 = rad(latitude1);
		double Lat2 = rad(latitude2);
		double a = Lat1 - Lat2;
		double b = rad(longitude1) - rad(longitude2);
		double s = 2 * Math.asin(Math
				.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 鏂囦欢杞寲涓哄瓧鑺傛暟缁�
	 */
	public static byte[] getBytesFromFile(File f) {
		if (f == null) {
			return null;
		}
		try {
			FileInputStream stream = new FileInputStream(f);
			ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = stream.read(b)) != -1)
				out.write(b, 0, n);
			stream.close();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Reads a line from the specified file.
	 *
	 * @param filename the file to read from
	 * @return the first line, if any.
	 * @throws IOException if the file couldn't be read
	 */
	private static String readLine(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
		try {
			return reader.readLine();
		} finally {
			reader.close();
		}
	}

	public static String getSimIMEI(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String IMEI = mTelephonyManager.getDeviceId();
		Log.i("getSimIMEI", "IMEI :" + IMEI);
		return mTelephonyManager.getDeviceId();
	}

	public static String getSimIMSI(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String IMSI = mTelephonyManager.getSubscriberId();
		Log.i("getSimIMSI", "IMSI :" + IMSI);
		return mTelephonyManager.getSubscriberId();
	}

	public static String getSimMSISDN(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String MSISDN = mTelephonyManager.getLine1Number();
		Log.i("getSimMSISDN", "MSISDN :" + MSISDN);
		return mTelephonyManager.getLine1Number();
	}

	public static String getSimICCID(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String ICCID = mTelephonyManager.getSimSerialNumber();
		Log.i("getSimICCID", "ICCID :" + ICCID);
		return mTelephonyManager.getSimSerialNumber();
	}


	public static void cancelNotify(Context context, String tag, int id) {
		NotificationManager nfm = (NotificationManager) context.getApplicationContext().getSystemService(
				Context.NOTIFICATION_SERVICE);
		if (nfm != null) {
			nfm.cancel(tag, id);
		}
	}

	public static Bitmap getBitmap384(Bitmap b, float left) {
		Bitmap bitmap = null;
		Paint  mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBitPaint.setFilterBitmap(true);
		mBitPaint.setDither(true);
		if (b != null) {
			int w = b.getWidth();
			int h = b.getHeight();
			if (w > 0 && h > 0) {
				bitmap = Bitmap.createBitmap(384, h, Config.RGB_565);
				Canvas c = new Canvas(bitmap);
				mBitPaint.setColor(Color.WHITE);
				c.drawRect(0,0,384,h, mBitPaint);
				c.drawBitmap(b, left, 0, mBitPaint);
				//v.draw(c);
				c = null;
			}
		}
		return bitmap;
	}

	public static void writeByteDataToFile(String path ,byte[] data){
		if(data == null){
			return;
		}
		File file = null;
		OutputStream out = null;
		BufferedOutputStream bufOut = null;
		try {
			file = new File(path);
			out = new FileOutputStream(file);
			bufOut = new BufferedOutputStream(out,data.length);
			bufOut.write(data);
			bufOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(bufOut!=null){
				try {
					bufOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out !=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static final String TAG = ToolsUtil.class.getSimpleName();
	private static final String DIR_NAME_HONEYWELL= "WangPosService";
	private static final String DIR_NAME_HONEYWELL_DATA = "data";
	public static String getHoneyWellDataPath(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File directory = Environment.getExternalStorageDirectory();
			return directory+File.separator+DIR_NAME_HONEYWELL+File.separator+DIR_NAME_HONEYWELL_DATA;
		}
		return null;
	}

	public static final String WHITE = "10";
	public static final String RED = "01";
	public static final String BOTH_ON = "11";
	public static final String BOTH_OFF = "00";
	public static void controlRedAndWhiteLight(String value,Context context) {
		Log.i(TAG, "controlRedAndWhiteLight " + value);
		try {
			PowerManager powerManager = (PowerManager)context .getSystemService(Context.POWER_SERVICE);
			Method controlLeds = powerManager.getClass().getMethod("setPropForControlLeds", String.class);
			controlLeds.invoke(powerManager, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 鑾峰彇璺緞鎵�湪鍒嗗尯鐨勫彲鐢ㄧ┖闂村ぇ灏�
	 * @param
	 * @return
	 */
	public static long getExternalStorage(){
		try {
			File file = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(file.getPath());
			long blockSize = sf.getBlockSizeLong();
			long availCount = sf.getAvailableBlocksLong();
			return blockSize*availCount;
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		return 0;
	}
}
