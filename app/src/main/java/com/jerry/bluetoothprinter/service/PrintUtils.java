package com.jerry.bluetoothprinter.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;


public class PrintUtils {

    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    private static final int LEFT_LENGTH = 20;

    private static final int RIGHT_LENGTH = 12;

    /**
     * 左侧汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 8;

    /**
     * 小票打印菜品的名称，上限调到8个字
     */
    public static final int MEAL_NAME_MAX_LENGTH = 8;

    private static OutputStream outputStream = null;
    
    /**
     * 复位打印机
     */
    public static final byte[] RESET = {0x1b, 0x40};

    /**
     * 左对齐
     */
    public static final byte[] ALIGN_LEFT = {0x1b, 0x61, 0x00};

    /**
     * 中间对齐
     */
    public static final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};

    /**
     * 右对齐
     */
    public static final byte[] ALIGN_RIGHT = {0x1b, 0x61, 0x02};

    /**
     * 选择加粗模式
     */
    public static final byte[] BOLD = {0x1b, 0x45, 0x01};

    /**
     * 取消加粗模式
     */
    public static final byte[] BOLD_CANCEL = {0x1b, 0x45, 0x00};

    /**
     * 宽高加倍
     */
    public static final byte[] DOUBLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x11};

    /**
     * 宽加倍
     */
    public static final byte[] DOUBLE_WIDTH = {0x1d, 0x21, 0x10};

    /**
     * 高加倍
     */
    public static final byte[] DOUBLE_HEIGHT = {0x1d, 0x21, 0x01};

    /**
     * 字体不放大
     */
    public static final byte[] NORMAL = {0x1d, 0x21, 0x00};

    /**
     * 设置默认行间距
     */
    public static final byte[] LINE_SPACING_DEFAULT = {0x1b, 0x32};

    public static OutputStream getOutputStream() {
        return outputStream;
    }

    public static void setOutputStream(OutputStream stream) {
        outputStream = stream;
    }
    
    public static void initPrinter() throws IOException {
    	if(outputStream != null) {
	    	outputStream.write(0x1b);
	    	outputStream.write(0x40);
    	}
    }
    
    /*
     * 设置对齐方式 0x00 居左， 0x01 居中， 0x02, 居右
     */
    public static void setAlign(int align) throws IOException {
    	if(outputStream != null) {
	    	outputStream.write(0x1b);
	    	outputStream.write(0x61);
	    	outputStream.write(align);
    	}
    }
    
	 /**
     * 选择加粗模式
	 * @throws IOException 
     */
    public static void setBold(int bold) throws IOException {
        if(outputStream != null) {
	    	outputStream.write(0x1b);
	    	outputStream.write(0x45);
	    	outputStream.write(bold);
    	}
    }
    
    
    /*
     * 0x00字体不放大， 0x01高加倍, 0x10宽加倍， 0x11高宽加倍
     */
    public static void setTextSize(int size) throws IOException {
    	 if(outputStream != null) {
 	    	outputStream.write(0x1d);
 	    	outputStream.write(0x21);
 	    	outputStream.write(size);
     	}
    }
    
    /**
	  * 设置行间距
	  */
	 protected static void setLineSpace(int space) throws IOException {
         if(outputStream != null) {
             outputStream.write(0x1B);
             outputStream.write(0x33);
             outputStream.write(space);
         }
	 }

	 public static void setCashBox() throws IOException {
         if(outputStream != null) {
             outputStream.write(0x1B);
             outputStream.write(0x70);
             outputStream.write(0);
             outputStream.write(1000);
             outputStream.write(2000);
         }
     }


    /**
     * 打印文字
     *
     * @param text 要打印的文字
     */
    public static void printText(String text) {
        try {
            byte[] data = text.getBytes("gbk");
            outputStream.write(data, 0, data.length);
        } catch (IOException e) {
            //Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    public static void printImage(Bitmap bitmap) throws IOException {
    	Log.i("WeiPos", "printImage ");
		 if(outputStream != null) {
			 Bitmap bitmap02 = compressPic(bitmap);
			 byte[] buffer = draw2PxPoint(bitmap02);
			 outputStream.write(buffer);
			 outputStream.flush();
		 }
    }
    
    
    public static void printBarCode(String content, int barType, int width, int height) throws Exception {
    	Bitmap b = createBarBitmap(content, barType, width, height);
    	Log.i("WeiPos", "printBarCode  b:" + b);
    	printImage(b);
    }
    
    public static void printQRCode(String content, int width, int height) throws IOException {
    	Log.i("WeiPos", "printQRCode");
    	Bitmap b = ToolsUtil.createQRBitmap(content, width, height, false);
    	Log.i("WeiPos", "printQRCode b:" + b);
    	printImage(b);
    }
    
    public static void printLine(int lineNum) throws IOException { 
	     for (int i = 0; i < lineNum; i++) {  
	    	 outputStream.write(" \n".getBytes("gbk"));  
	     }  
	 }
    
    public void addRastBitImage(Bitmap bitmap, int nWidth, int nMode) throws IOException
    {
      if (bitmap != null) {
        int width = (nWidth + 7) / 8 * 8;
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap grayBitmap = GpUtils.toGrayscale(bitmap);
        Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width, height);
        byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
        byte[] command = new byte[8];
        height = src.length / width;
        command[0] = 29;
        command[1] = 118;
        command[2] = 48;
        command[3] = (byte)(nMode & 0x1);
        command[4] = (byte)(width / 8 % 256);
        command[5] = (byte)(width / 8 / 256);
        command[6] = (byte)(height % 256);
        command[7] = (byte)(height / 256);
        byte[] codecontent = GpUtils.pixToEscRastBitImageCmd(src);
//        for (int k = 0; k < codecontent.length; ++k)
//        	outputStream.write((Byte.valueOf(codecontent[k])));
        outputStream.write(codecontent);
      }
      else {
        Log.d("BMP", "bmp.  null ");
      }
    }
    /**
     * 设置打印格式
     *
     * @param command 格式指令
     */
    public static void selectCommand(byte[] command) {
        try {
            outputStream.write(command);
            outputStream.flush();
        } catch (IOException e) {
            //Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 打印两列
     *
     * @param leftText  左侧文字
     * @param rightText 右侧文字
     * @return
     */
    @SuppressLint("NewApi")
    public static String printTwoData(String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        // 计算两侧文字中间的空格
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }

    /**
     * 打印三列
     *
     * @param leftText   左侧文字
     * @param middleText 中间文字
     * @param rightText  右侧文字
     * @return
     */
    @SuppressLint("NewApi")
    public static String printThreeData(String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 计算左侧文字和中间文字的空格长度
        int marginBetweenLeftAndMiddle = LEFT_LENGTH - leftTextLength - middleTextLength / 2;

        for (int i = 0; i < marginBetweenLeftAndMiddle; i++) {
            sb.append(" ");
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }

        // 打印的时候发现，最右边的文字总是偏右一个字符，所以需要删除一个空格
        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

    /**
     * 获取数据长度
     *
     * @param msg
     * @return
     */
    @SuppressLint("NewApi")
    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }

    /**
     * 格式化菜品名称，最多显示MEAL_NAME_MAX_LENGTH个数
     *
     * @param name
     * @return
     */
    public static String formatMealName(String name) {
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        if (name.length() > MEAL_NAME_MAX_LENGTH) {
            return name.substring(0, 8) + "..";
        }
        return name;
    }
    
    public static Bitmap createBarBitmap(String contents, int barType, int desiredWidth, int desiredHeight)
            throws WriterException {
        // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
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
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
	
    /**
     * 用字符串生成二维码图片
     *
     * @param str
     * @return
     * @throws WriterException
     */
    public static final Bitmap createQRBitmap(String str, int qrWidth, int qrHeight, boolean closing) {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
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
                // 二维矩阵转为一维像素数组,也就是一直横着排了
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
            // 通过像素数组生成bitmap,具体参考api
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }


    /**
     * 对图片进行压缩（去除透明度）
     */
    public static Bitmap compressPic(Bitmap bitmap) {
        // 获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 指定调整后的宽度和高度
        int newWidth = 384;
        int newHeight = height;
        if (width > newWidth) {
            float multiple = (float) newWidth / (float) width;
            newHeight = (int) (height * multiple);
        } else {
            return bitmap;
        }
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight,
                Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmap, new Rect(0, 0, width, height),
                new Rect(0, 0, newWidth, newHeight), null);
        return targetBmp;
    }

    public static byte[] draw2PxPoint(Bitmap bmp) {
        // 用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        // 整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        // 但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        // 所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 2000;
        byte[] data = new byte[size];
        int k = 0;
        // 设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            // 打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); // nL
            data[k++] = (byte) (bmp.getWidth() / 256); // nH
            // 对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                // 每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    // 每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;// 换行
        }
        return data;
    }


    /**
     * 图片二值化
     */
    private static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            // 取高两位
            int red = (pixel & 0x00ff0000) >> 16;
            // 取中两位
            int green = (pixel & 0x0000ff00) >> 8;
            // 取低两位
            int blue = pixel & 0x000000ff;
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                //黑色是1
                b = 1;
            } else {
                //白色是0
                b = 0;
            }
            return b;
        }
        return 0;
    }

    private static int RGB2Gray(int r, int g, int b) {
        return (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);
    }
 
}
