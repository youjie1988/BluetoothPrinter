/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.shellinfo.wall.cache;

import java.util.ArrayList;
import java.util.Hashtable;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public final class CacheService {
	public static String mPackageName = "cn.shellinfo.pos";

	public static final String getCachePath(final String subFolderName) {
		return Environment.getExternalStorageDirectory() + "/Android/data/" + mPackageName + "/cache/" + subFolderName;
	}

	public static DiskCache sDataCache = null;
	public static DiskCache sImageCache = null;

	public static void start(String packageName) {
		mPackageName=packageName;
		if (sDataCache == null)
			sDataCache = new DiskCache("data-cache_1");
		if (sImageCache == null)
			sImageCache = new DiskCache("wall-image");
	}

	public static void clearAll() {
		if (sDataCache != null) {
			sDataCache.deleteAll();
			sDataCache.flush();
		}
		if (sImageCache != null) {
			sImageCache.deleteAll();
			sImageCache.flush();
		}
	}

	public static void stop() {
		if (sDataCache != null) {
			sDataCache.close();
			sDataCache = null;
		}
		if (sImageCache != null) {
			sImageCache.close();
			sImageCache = null;
		}
	}

	/**
	 * 淇濆瓨鏁版嵁鍒扮紦瀛�
	 * 
	 * @param dataCacheKey
	 * @param dataList
	 */
	public static final void saveDataList2Cache(long dataCacheKey, ArrayList<? extends Parcelable> dataList) {
		if (dataCacheKey != 0 && CacheService.sDataCache != null) {
			CacheService.sDataCache.delete(dataCacheKey);
			CacheService.sDataCache.flush();
			if (dataList.size() > 0) {
				Parcel parcel = Parcel.obtain();
				parcel.writeList(dataList);
				byte[] bytes = parcel.marshall();
				if (bytes != null && bytes.length > 0) {
					CacheService.sDataCache.put(dataCacheKey, bytes, 0);
					CacheService.sDataCache.flush();
				}
				parcel.recycle();
			}
//			else{
//				CacheService.sDataCache.deleteAll();
//				CacheService.sDataCache.flush();
//			}
		}
	}

	/**
	 * 浠庣紦瀛樹腑璇诲彇鏁版嵁
	 * 
	 * @return
	 */
	public static final ArrayList<Parcelable> loadCachedDataList(long dataCacheKey) {
		if (CacheService.sDataCache==null||!CacheService.sDataCache.isDataAvailable(dataCacheKey, 0)) {
			return null;
		}
		byte[] contactsData = CacheService.sDataCache.get(dataCacheKey, 0);
		if (contactsData != null) {
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(contactsData, 0, contactsData.length);
			parcel.setDataPosition(0);
			ArrayList<Parcelable> dataList = parcel.readArrayList(CacheService.class.getClassLoader());
			parcel.recycle();
			return dataList;
		}
		return null;
	}
	
	/**
	 * 淇濆瓨map鏁版嵁鍒扮紦瀛�
	 * @param dataCacheKey
	 * @param map
	 */
	public static final void saveDataMap2Cache(long dataCacheKey,Hashtable<String, ? extends Parcelable> map){
		if (dataCacheKey != 0 && CacheService.sDataCache != null) {
			CacheService.sDataCache.delete(dataCacheKey);
			CacheService.sDataCache.flush();
			if(map.size()>0){
				Parcel parcel = Parcel.obtain();
				parcel.writeMap(map);
				byte[] bytes = parcel.marshall();
				if (bytes != null && bytes.length > 0) {
					CacheService.sDataCache.put(dataCacheKey, bytes, 0);
					CacheService.sDataCache.flush();
				}
				parcel.recycle();
			}
		}
	}
	
	/**
	 * 浠庣紦瀛樹腑璇诲彇鏁版嵁
	 * @param dataCacheKey
	 * @return
	 */
	public static final Hashtable<String,Parcelable> loadCachedDataMap(long dataCacheKey){
		if (CacheService.sDataCache==null||!CacheService.sDataCache.isDataAvailable(dataCacheKey, 0)) {
			return null;
		}
		byte[] contactsData = CacheService.sDataCache.get(dataCacheKey, 0);
		if (contactsData != null) {
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(contactsData, 0, contactsData.length);
			parcel.setDataPosition(0);
			Hashtable<String, Parcelable> map=new Hashtable<String, Parcelable>();
			parcel.readMap(map, CacheService.class.getClassLoader());
			parcel.recycle();
			return map;
		}
		return null;
	}
	
	/**
	 * 淇濆瓨搴忓垪鍖栧璞�
	 * 
	 * @param dataCacheKey
	 * @param parcelable
	 */
	public static final void saveParcelableData2Cache(long dataCacheKey,
			Parcelable parcelable) {

		if (dataCacheKey != 0 && CacheService.sDataCache != null) {
			CacheService.sDataCache.delete(dataCacheKey);
			CacheService.sDataCache.flush();
			if (parcelable == null) {
				return;
			}
			Parcel parcel = Parcel.obtain();
			parcel.writeParcelable(parcelable, 0);
			byte[] bytes = parcel.marshall();
			if (bytes != null && bytes.length > 0) {
				CacheService.sDataCache.put(dataCacheKey, bytes, 0);
				CacheService.sDataCache.flush();
			}
			parcel.recycle();
		}
	}

	/**
	 * 浠庣紦瀛樹腑璇诲彇搴忓垪鍖栧璞�
	 * 
	 * @return
	 */
	public static final Parcelable loadCachedParcelableData(long dataCacheKey) {
		if (CacheService.sDataCache == null
				|| !CacheService.sDataCache.isDataAvailable(dataCacheKey, 0)) {
			return null;
		}
		byte[] contactsData = CacheService.sDataCache.get(dataCacheKey, 0);
		if (contactsData != null) {
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(contactsData, 0, contactsData.length);
			parcel.setDataPosition(0);
			Parcelable parcelable = parcel.readParcelable(CacheService.class
					.getClassLoader());
			parcel.recycle();
			return parcelable;
		}
		return null;
	}

	
	private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;
	private static long[] CRCTable = new long[256];
	private static boolean init = false;
	private static final long POLY64REV = 0x95AC9329AC4BC9B5L;

	/**
	 * A function thats returns a 64-bit crc for string
	 * 
	 * @param in
	 *            : input string
	 * @return 64-bit crc value
	 */
	public static final long Crc64Long(String in) {
		if (in == null || in.length() == 0) {
			return 0;
		}
		// http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
		long crc = INITIALCRC, part;
		if (!init) {
			for (int i = 0; i < 256; i++) {
				part = i;
				for (int j = 0; j < 8; j++) {
					int value = ((int) part & 1);
					if (value != 0)
						part = (part >> 1) ^ POLY64REV;
					else
						part >>= 1;
				}
				CRCTable[i] = part;
			}
			init = true;
		}
		int length = in.length();
		for (int k = 0; k < length; ++k) {
			char c = in.charAt(k);
			crc = CRCTable[(((int) crc) ^ c) & 0xff] ^ (crc >> 8);
		}
		return crc;
	}

}
