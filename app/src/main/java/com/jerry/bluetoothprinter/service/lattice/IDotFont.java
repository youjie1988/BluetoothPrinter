package com.jerry.bluetoothprinter.service.lattice;

/**
 * 鐐归樀瀛椾綋
 * 
 * @author houj
 * @ID
 * @time 2014骞�12鏈�22鏃� 涓婂崍11:09:59
 */
public interface IDotFont {

	/**
	 * 鍙栧緱瀛楃瀛椾綋淇℃伅
	 * 
	 * @param c
	 * @param info
	 * @return
	 */
	public boolean getCharBitMap(char c, BitMapInfo info);

	/**
	 * 鍙栧緱瀛椾綋楂樺害
	 * 
	 * @return
	 */
	public int getFontHeight();

	/**
	 * 鍙栧緱瀛椾綋鎷変几瀹炰緥
	 * 
	 * @param w
	 *            瀹藉害鍊嶆暟
	 * @param h
	 *            楂樺害鍊嶆暟
	 * @return 鏂扮殑瀛椾綋
	 */
	public IDotFont getScanIns(int w, int h);

	/**
	 * 缁樺埗瀛楃
	 * 
	 * @param c
	 * @param buf
	 * @param screenW
	 * @param x
	 * @param bottomY
	 * @return
	 */
	public int paintTo(char c, byte[] buf, int screenW, int x, int bottomY);
}
