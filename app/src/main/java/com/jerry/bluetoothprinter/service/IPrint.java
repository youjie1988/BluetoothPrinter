package com.jerry.bluetoothprinter.service;

import android.view.View.MeasureSpec;

/**
 * 鎵撳嵃鎺ュ彛
 * 
 * @author hedong
 * 
 */
public interface IPrint {
	/**
	 * 鎵撳嵃浜嬩欢甯搁噺锛屾湭鐭ヤ簨浠?
	 */
	public static final int EVENT_UNKNOW = 0;
	/**
	 * 鎵撳嵃浜嬩欢甯搁噺锛岀己绾?
	 */
	public static final int EVENT_NO_PAPER = 1;
	/**
	 * 鎵撳嵃浜嬩欢甯搁噺锛屽崱绾?
	 */
	public static final int EVENT_PAPER_JAM = 2;
	/**
	 * 鎵撳嵃浜嬩欢甯搁噺锛屾墦鍗版垚鍔?
	 */
	public static final int EVENT_OK = 3;
	/**
	 * 鎵撳嵃浜嬩欢甯搁噺锛屾墦鍗版満楂樻俯
	 */
	public static final int EVENT_HIGH_TEMP=4;
	/**
	 * 杩炴帴鎵撳嵃鏈烘垚鍔燂紝宸茬粡杩炴帴鍒版墦鍗版満
	 */
	public static final int EVENT_CONNECTED = 5;
	/**
	 * 杩炴帴鎵撳嵃鏈哄け璐?
	 */
	public static final int EVENT_CONNECT_FAILD = 6;
	/**
	 * 鎵撳嵃鏈虹姸鎬佹甯?
	 */
	public static final int EVENT_STATE_OK = 7;
	/**
	 * 鎵撳嵃澶辫触
	 */
	public static final int EVENT_PRINT_FAILD=8;

	//1+鎵撳嵃鏈虹姸鎬?鍙傛暟閿欒
	public static final int EVENT_ONEPLUS_NOT_EXECUTE=10;

	//1+ 鎵撳嵃鏈虹姸鎬佹棤娉曟墽琛?
	public static final int EVENT_ONEPLUS_FLAG_ERROR =10;

	/**
	 * 鎵撳嵃浜嬩欢鐩戝惉鎺ュ彛
	 * 
	 * @since 1.0
	 * @author Happy
	 * 
	 */
	public interface OnEventListener {
		/**
		 * 浜嬩欢鏂规硶
		 * 
		 * @see IPrint#EVENT_UNKNOW
		 * @see IPrint#EVENT_NO_PAPER
		 * @see IPrint#EVENT_PAPER_JAM
		 * @see IPrint#EVENT_OK
		 * @see IPrint#EVENT_HIGH_TEMP
		 * @see IPrint#EVENT_CONNECTED
		 * @see IPrint#EVENT_CONNECT_FAILD
		 * @see IPrint#EVENT_STATE_OK
		 * @param what
		 *            浜嬩欢绫诲瀷
		 * @param info
		 *            浜嬩欢娑堟伅
		 */
		public void onEvent(final int what, final String info);
	}
	
	/**
	 * 瀹氫綅绛栫暐鏋氫妇鍊?
	 * 
	 * @since 1.0
	 * @author Happy
	 * 
	 */
	public enum Gravity {
		LEFT, CENTER, RIGHT
	}
	
	/**
	 * 鎵撳嵃鏈虹姸鎬佹甯?
	 */
	public static final byte STATE_OK = 0;
	/**
	 * 鎵撳嵃鏈虹姸鎬佹棤绾?
	 */
	public static final byte STATE_NO_PAPER = 0x01;
	/**
	 * 鎵撳嵃鏈虹姸鎬侀珮娓?
	 */
	public static final byte STATE_HIGH_TEMP = 0x02;

	/**
	 * 鎵撳嵃鏈烘寜浜嗚蛋绾搁敭
	 */
	public static final byte STATE_PRESS_KEY = 0x04;
	public static final byte CMD_TEST = 0x10;
	/**
	 * 杩涚焊鍛戒护
	 */
	public static final byte CMD_FEED = 0x11;
	/**
	 * 鎵撳嵃鍛戒护
	 */
	public static final byte CMD_PRINT = 0x12;
	/**
	 * 涓?鐨勫儚绱犲搴?
	 */
	public static final int ROW_WIDTH = 384;
	/**
	 * 姣忔鏈?鍙戦?缁欐墦鍗版満鐨勫唴瀹规暟鎹暱搴?涓嶅寘鍚ご灏俱?鍛戒护銆侀暱搴︽弿杩板瓧鑺?
	 */
	public static final int MAX_DATA_LEN = 1152;
	
	public static final int SPEC_WIDTH = MeasureSpec.makeMeasureSpec(ROW_WIDTH, MeasureSpec.EXACTLY);
	public static final int SPEC_HEIGHT = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
	
	/**
	 * 缁堟褰撳墠鍔ㄤ綔鍛戒护锛堢粓姝㈡墦鍗版祴璇曪紝杩涚焊鎿嶄綔锛屾鍦ㄦ墦鍗扮殑鎿嶄綔绛夛級
	 */
	public static final byte OTHER_CMD_STOP = 0x10;
	/**
	 * 鎵撳嵃娴嬭瘯椤?
	 */
	public static final byte OTHER_CMD_TEST = 0x11;
	/**
	 * 璇㈤棶鎵撳嵃鏈虹姸鎬?
	 */
	public static final byte OTHER_CMD_STATE = 0x12;
	/**
	 * 鍏朵粬鎸囦护鏁版嵁鍖?
	 */
	public static final byte[] otherCmdSendData = new byte[] { 0x00, 0x03, 0x11, 0x10, 0x11};

	public static final String TAG_IMAGE = "image";
	public static final String TAG_QRCODE = "qrcode";
	public static final String TAG_BARCODE = "barcode";

	/**
	 * 鎵撳嵃鏈虹姸鎬佹甯?
	 */
	public static final int ONEPLUS_STATE_OK = 0x00;
	/**
	 * 鎵撳嵃鏈虹姸鎬佹棤绾?
	 */
	public static final int ONEPLUS_STATE_NO_PAPER = 0x8A;
	/**
	 * 鎵撳嵃鏈虹姸鎬侀珮娓?
	 */
	public static final int ONEPLUS_STATE_HIGH_TEMP = 0x8B;

	//0x01锛氬弬鏁伴敊璇?
	public static final int ONEPLUS_STATE_NOT_EXECUTE = 0x06;

	//0x06锛氫笉鍙墽琛?
	public static final int ONEPLUS_STATE_FLAG_ERROR = 0x01;
}
