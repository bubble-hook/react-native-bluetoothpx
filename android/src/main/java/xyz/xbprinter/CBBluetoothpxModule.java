package xyz.xbprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Looper;

import android.os.Environment;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;

import android.graphics.Paint.Style;
import android.graphics.Typeface;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import net.glxn.qrgen.android.QRCode;

public class CBBluetoothpxModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private BluetoothPrinterCore printerCore = null;
    private IPrinterManager printerManager = null;

    public CBBluetoothpxModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private void sendEvent(String eventName, WritableMap params) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private void sendEvent(String eventName, String value) {
        WritableMap result = Arguments.createMap();
        result.putString(eventName, value);
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, result);
    }

    @Override
    public String getName() {
        return "CBBluetoothpx";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void getBluetoothList(Callback callback) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        WritableMap result = Arguments.createMap();
        WritableArray devices = Arguments.createArray();

        if (bluetoothAdapter == null) {
            // device doesn't support bluetooth
        } else {

            // bluetooth is off, ask user to on it.
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            }
            // Do whatever you want to do with your bluetoothAdapter
            Set<BluetoothDevice> all_devices = bluetoothAdapter.getBondedDevices();
            if (all_devices.size() > 0) {
                for (BluetoothDevice currentDevice : all_devices) {
                    if(currentDevice != null){
                        WritableMap device = Arguments.createMap();
                        device.putString("deviceName", currentDevice.getName());
                        device.putString("deviceAddress", currentDevice.getAddress());
                        devices.pushMap(device);
                    }
                }
            }
        }
        result.putArray("devices", devices);
        callback.invoke(result);
    }

    @ReactMethod
    public void connect(String deviceAddress) throws Exception {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);

        if (printerManager != null) {
            printerManager.disconnect();    
        }
        printerManager = new ZJPrinterManager(this.reactContext.getCurrentActivity(), mHandler);

        printerManager.connect(deviceAddress, "");
        printerManager.setPaperWidth(384);

    }

    @ReactMethod
    public void addText(String text, double position, int align) throws Exception {
        // if ((Double.compare(position, 0) <= 0) || (Double.compare(100, position) <
        // 0))
        // throw new Exception("position must be between 0 and 100");
        printerManager.addText(text, position, align);
    }

    @ReactMethod
    public void flushText() throws Exception {

        printerManager.flushText();
    }

    @ReactMethod
    public void addFeedLine(int line) throws Exception {
        printerManager.addFeedLine(line,35);
    }

    @ReactMethod
    public void addCmdFeedLine() throws Exception {
        printerManager.addCmdFeedLine();
    }

    @ReactMethod
    public void addFeedLineWithMultiple(int line,int multiple) throws Exception {
        printerManager.addFeedLine(line,multiple);
    }

    @ReactMethod
    public void addBarcode(String txt) throws Exception {
        //printerManager.addBarcode(text);
        printerManager.addBarcode(txt, PrinterConst.ALIGN_CENTER);
    }

    @ReactMethod
    public void addQRcode(String text) throws Exception {
        //printerManager.addBarcode(text);
        printerManager.addQRcode(text, PrinterConst.ALIGN_CENTER);
    }

    @ReactMethod
    public void sendData() throws Exception {
        printerManager.sendData();
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MsgTypes.MESSAGE_STATE_CHANGE:
                    Bundle bundle = msg.getData();
                    int status = bundle.getInt("state");

                    switch (msg.arg1) {
                        case PrinterConst.STATE_CONNECTED:
                            sendEvent("printerStateChange", "connected");
                            // sendBroadcast("state","connected");
                            break;
                        case PrinterConst.STATE_CONNECTING:
                            // sendBroadcast("state", "connecting");
                            sendEvent("printerStateChange", "connecting");
                            break;
                        case PrinterConst.STATE_LISTEN:
                            // sendBroadcast("connecting");
                            sendEvent("printerStateChange", "connecting");
                            break;
                        case PrinterConst.STATE_IDLE:
                            // sendBroadcast("nothing");
                            sendEvent("printerStateChange", "nothing");
                            break;
                        case PrinterConst.STATE_DISCONNECTED:
                            // sendBroadcast("state", "disconnected");
                            sendEvent("printerStateChange", "disconnected");
                            break;
                        case PrinterConst.STATE_CONNECT_FAILED:
                            // sendBroadcast("state", "connect-failed");
                            sendEvent("printerStateChange", "connect-failed");
                            break;
                        case PrinterConst.STATE_CONNECT_LOST:
                            sendEvent("printerStateChange", "connect lost");
                            // sendBroadcast("connect lost");
                            break;
                        case PrinterConst.STATE_COVER_OPEN:
                            // sendBroadcast("warning","cover is open");
                            break;
                        case PrinterConst.STATE_PAPER_EMPTY:
                            // sendBroadcast("warning","paper is empty");
                            break;
                        case PrinterConst.STATE_PAPER_JAM:
                            // sendBroadcast("warning","paper jam");
                            break;
                        case PrinterConst.STATE_BATTERY_LOW:
                            // sendBroadcast("warning","battery low");
                            break;
                        case PrinterConst.STATE_FEATURE_NOT_SUPPORTED:
                            // sendBroadcast("warning","feature not supported");
                            break;
                        case PrinterConst.STATE_PRINT_SUCCESS:
                            // sendBroadcast("success","print-success");
                            break;
                    }
                    break;

            }
        }
    };

}

interface IPrinterManager {
    boolean connect(String address, String connectionType) throws Exception;

    boolean disconnect() throws Exception;

    boolean isConnected() throws Exception;

    boolean setPaperWidth(int width) throws Exception;

    boolean setPrintMode(int mode) throws Exception;

    boolean setBold(boolean bold) throws Exception;

    void addImage(Bitmap image) throws Exception;

    void addImage(Bitmap image, int align) throws Exception;

    void addBarcode(String text) throws Exception;

    void addBarcode(String text, int align) throws Exception;

    void addQRcode(String text) throws Exception;

    void addQRcode(String text, int align) throws Exception;

    void addText(String text, double position) throws Exception;

    void addText(String text, double position, int align, boolean... inline) throws Exception;

    void flushText() throws Exception;

    void addFeedLine(int line,int multiple) throws Exception;

    void addCmdFeedLine() throws Exception;

    void addPageEnd() throws Exception;

    void addCut() throws Exception;

    void openDrawer(int pin) throws Exception;

    void sendData() throws Exception;
}

class PrintingUtil {
    // UNICODE 0x23 = #
    public static final byte[] UNICODE_TEXT = new byte[] { 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23 };

    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000",
            "1001", "1010", "1011", "1100", "1101", "1110", "1111" };

    private static int _lineHeight = 35;

    private static boolean _textBold = false;

    public static void setTextBold(boolean bold) {
        _textBold = bold;
    }

    public static String convertText(String text, int limit, int side) {
        int length = text.length();

        if (length > limit) {
            text = text.substring(0, limit - 1);
        } else {
            for (int i = 0; i < (limit - length); i++) {
                if (side == 1) {
                    text = " " + text;
                } else {
                    text = text + " ";
                }
            }
        }

        return text;
    }

    public static String setFormatStringForPrinting(String text, int limit, int align) {
        int length = 0;
        String newStr = "";
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int ascii = (int) character;
            if (!(ascii == 3633 || (ascii >= 3636 && ascii <= 3642) || (ascii >= 3655 && ascii <= 3662))) {
                length++;
            }
            if (length <= limit) {
                newStr = newStr + character;
            }
        }

        if (length > limit) {
            newStr = newStr.substring(0, limit - 1);
        } else {
            int space = limit - length;
            for (int i = 0; i < (space); i++) {
                if (align == PrinterConst.ALIGN_RIGHT) {
                    newStr = " " + newStr;
                } else if (align == PrinterConst.ALIGN_LEFT) {
                    newStr = newStr + " ";
                }
            }
        }

        return newStr;
    }

    public static String thaiR(String text, int limit) {
        int count = 0;
        String newStr = "";
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int ascii = (int) character;
            if (!(ascii == 3633 || (ascii >= 3636 && ascii <= 3642) || (ascii >= 3655 && ascii <= 3662))) {
                count++;
            }
            if (count <= limit) {
                newStr = newStr + character;
            }
        }

        for (int i = 0; i < (limit - count); i++) {
            newStr = newStr + " ";
        }

        return newStr;
    }

    public static int countThaiR(String text) {
        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int ascii = (int) character;
            if (!(ascii == 3633 || (ascii >= 3636 && ascii <= 3642) || (ascii >= 3655 && ascii <= 3662))) {
                count++;
            }
        }

        return count;
    }

    public static String convertReal(String str) {
        String TempStr = "";

        if (str.indexOf(",") > 0) {
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                String strCh = "";
                strCh = strCh + ch;
                if (!strCh.equals(",")) {
                    TempStr = TempStr + strCh;
                }
            }
        } else {
            TempStr = str;
        }

        return TempStr;
    }

    public static byte[] decodeBitmap(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); // binaryString list
        StringBuffer sb;

        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer.toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            // return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            // return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    public static List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<String>();
        for (String binaryStr : list) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    public static String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    public static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<byte[]>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        byte[] bytes = sysCopy(commandList);
        return bytes;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static Bitmap scaleBitmap(Bitmap bm) {
        int maxWidth = 100;
        int maxHeight = 100;
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.i("Pictures", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int) (width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }

        Log.i("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public static String getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = df.format(c.getTime());

        return date;
    }

    public static String convertStrDateToPatternDateThaiYear(String dateStr, String oldDattern, String newPattern) {
        try {
            DateFormat oldPt = new SimpleDateFormat(oldDattern);
            DateFormat newPt = new SimpleDateFormat(newPattern);
            Date newDate = null;

            newDate = oldPt.parse(dateStr);
            Calendar calender = Calendar.getInstance();
            calender.setTime(newDate);
            calender.add(Calendar.YEAR, 543);

            return newPt.format(calender.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Bitmap formattingPrinterGraphicMode(int paperWidth, Bitmap line, String text, double position,
            int align) {
        int textWidth = getTextWidth(text);
        Float pt = findPosition(paperWidth, (float) position - 1, align, textWidth);

        if (line == null) {
            line = Bitmap.createBitmap(paperWidth, _lineHeight, Bitmap.Config.ARGB_8888);
            createCanvas(line, "", paperWidth);
        }

        Bitmap canvasBitmap = Bitmap.createBitmap(textWidth, _lineHeight, Bitmap.Config.ARGB_8888);
        createCanvas(canvasBitmap, text, paperWidth);

        Canvas comboImage = new Canvas(line);
        comboImage.drawBitmap(line, 0f, 0f, null);
        comboImage.drawBitmap(canvasBitmap, pt, 0f, null);

        return line;
    }

    public static Bitmap formattingPrinterGraphicModeOutLine(int paperWidth, String text, double position, int align) {
        Layout.Alignment alignment = null;

        if (align == PrinterConst.ALIGN_LEFT) {
            alignment = Layout.Alignment.ALIGN_NORMAL;
        } else if (align == PrinterConst.ALIGN_CENTER) {
            alignment = Layout.Alignment.ALIGN_CENTER;
        } else if (align == PrinterConst.ALIGN_RIGHT) {
            alignment = Layout.Alignment.ALIGN_OPPOSITE;
        }

        TextPaint paint = createTextPaint(23);

        StaticLayout layout = new StaticLayout(text, 0, text.length(), paint, paperWidth, alignment, 1.1F, 0.0F, true,
                TextUtils.TruncateAt.END, paperWidth);

        int textHeight = layout.getHeight() + 5;

        Bitmap canvasBitmap = Bitmap.createBitmap(paperWidth, textHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.setBitmap(canvasBitmap);
        canvas.drawColor(-1);
        canvas.translate(0.0F, 5.0F);
        layout.draw(canvas);
        canvas.save();
        canvas.restore();

        return canvasBitmap;
    }

    public static Bitmap addRowGraphicMode(Bitmap line1, Bitmap line2, int width) {
        // int textWidth = getTextWidth(text);
        // Float pt = findPosition(paperWidth, (float) position - 1, align, textWidth);
        //
        // if (line == null) {
        // line = Bitmap.createBitmap(paperWidth, _lineHeight, Bitmap.Config.ARGB_8888);
        // createCanvas(line, "", paperWidth);
        // }
        //
        // Bitmap canvasBitmap = Bitmap.createBitmap(textWidth, _lineHeight,
        // Bitmap.Config.ARGB_8888);
        // createCanvas(canvasBitmap, text, paperWidth);

        int height1 = line1 != null ? line1.getHeight() : 0;
        int height2 = line2 != null ? line2.getHeight() : 0;
        int height = height1 + height2;

        Bitmap canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(canvasBitmap);
        if (line1 != null) {
            comboImage.drawBitmap(line1, 0f, 0f, null);
        }

        if (line2 != null) {
            comboImage.drawBitmap(line2, 0f, height1, null);
        }

        return canvasBitmap;
    }

    public static Bitmap generateImage(Bitmap image, int width, int align) {
        int height = image != null ? image.getHeight() : 0;

        Bitmap canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        createCanvas(canvasBitmap, "", width);

        Canvas comboImage = new Canvas(canvasBitmap);

        if (align == PrinterConst.ALIGN_CENTER) {
            Float position = ((float) (width) / 2) - ((float) image.getWidth() / 2);
            comboImage.drawBitmap(image, position, 0f, null);
        } else if (align == PrinterConst.ALIGN_RIGHT) {
            Float position = (float) width - (float) image.getWidth();
            comboImage.drawBitmap(image, position, 0f, null);
        } else {
            comboImage.drawBitmap(image, 0f, 0f, null);
        }

        return canvasBitmap;
    }

    public static Canvas createCanvas(Bitmap btm, String text, int paperWidth) {
        StaticLayout layout = createStaticLayout(text, paperWidth);

        Canvas canvas = new Canvas(btm);
        canvas.setBitmap(btm);
        canvas.drawColor(-1);
        canvas.translate(0.0F, 5.0F);
        layout.draw(canvas);
        canvas.save();
        canvas.restore();

        return canvas;
    }

    private static StaticLayout createStaticLayout(String text, int paperWidth) {
        //TextPaint paint = createTextPaint(23);
        TextPaint paint = createTextPaint(17);

        return new StaticLayout(text, 0, text.length(), paint, paperWidth, Layout.Alignment.ALIGN_NORMAL, 1.1F, 0.0F,
                true, TextUtils.TruncateAt.END, paperWidth);
    }

    private static TextPaint createTextPaint(int textSize) {
        TextPaint paint = new TextPaint();
        paint.setColor(-16777216);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(false);

        paint.setTypeface(Typeface.create(_textBold ? Typeface.DEFAULT_BOLD : Typeface.SANS_SERIF,
                _textBold ? Typeface.BOLD : Typeface.NORMAL));

        return paint;
    }

    private static int getTextWidth(String text) {
        Rect bounds = new Rect();
        TextPaint paint = createTextPaint(23);
        paint.getTextBounds(text, 0, text.length(), bounds);

        return bounds.width() + 3;
    }

    private static int getTextHeight(String text, int align, int width) {
        Rect bounds = new Rect();
        TextPaint paint = createTextPaint(23);

        Layout.Alignment alignment = null;

        if (align == PrinterConst.ALIGN_LEFT) {
            alignment = Layout.Alignment.ALIGN_NORMAL;
        } else if (align == PrinterConst.ALIGN_CENTER) {
            alignment = Layout.Alignment.ALIGN_CENTER;
        } else if (align == PrinterConst.ALIGN_RIGHT) {
            alignment = Layout.Alignment.ALIGN_OPPOSITE;
        }

        StaticLayout layout = new StaticLayout(text, 0, text.length(), paint, width, alignment, 1.1F, 0.0F, true,
                TextUtils.TruncateAt.END, width);

        return layout.getHeight();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String formattingPrinter(int paperWidth, int characterSize, String line, String text, double position,
            int align) {
        int pt = findPosition(paperWidth, characterSize, position);
        return replaceWordInString(line, text, align, pt, characterSize);
    }

    public static String replaceWordInString(String textLine, String word, int align, int position, int characterSize) {
        String newTextLine = "";
        int textLineLength = countStringInoreThaiSpecialCharacter(textLine);
        int wordLength = countStringInoreThaiSpecialCharacter(word);
        int beginPosition = 0;
        int endPosition = 0;
        int cutOffPosition = 0;
        if (textLine == null && textLine.length() == 0) {
            for (int i = 0; i < characterSize; i++) {
                textLine += " ";
            }
        } else if (textLineLength < characterSize) {
            for (int i = textLineLength; i < characterSize; i++) {
                textLine += " ";
            }
        }

        if (align == PrinterConst.ALIGN_LEFT) {
            beginPosition = countStringRealPositionThaiSpecialCharacter(textLine, position,
                    PrinterConst.COUNT_STRING_TYPE_1, characterSize);

            newTextLine = textLine.substring(0, beginPosition) + word;

            endPosition = countStringRealPositionThaiSpecialCharacter(textLine,
                    countStringInoreThaiSpecialCharacter(newTextLine), PrinterConst.COUNT_STRING_TYPE_1, characterSize);

            newTextLine = newTextLine + textLine.substring(endPosition, textLine.length());

            cutOffPosition = countStringRealPositionThaiSpecialCharacter(newTextLine, characterSize,
                    PrinterConst.COUNT_STRING_TYPE_2, characterSize);

            newTextLine = newTextLine.substring(0, cutOffPosition + 1);

        } else if (align == PrinterConst.ALIGN_RIGHT) {
            beginPosition = countStringRealPositionThaiSpecialCharacter(textLine, position,
                    PrinterConst.COUNT_STRING_TYPE_1, characterSize) + 1;

            newTextLine = word + textLine.substring(beginPosition, textLine.length());

            if (wordLength > position) {
                endPosition = countStringInoreThaiSpecialCharacter(newTextLine)
                        - countStringInoreThaiSpecialCharacter(textLine);
                cutOffPosition = countStringRealPositionThaiSpecialCharacter(newTextLine, endPosition,
                        PrinterConst.COUNT_STRING_TYPE_2, characterSize) + 1;
                newTextLine = newTextLine.substring(cutOffPosition, newTextLine.length());

            } else {
                int countStr = countStringInoreThaiSpecialCharacter(newTextLine);
                if (characterSize > countStr) {
                    endPosition = countStringRealPositionThaiSpecialCharacter(textLine, characterSize - countStr,
                            PrinterConst.COUNT_STRING_TYPE_2, characterSize) + 1;
                } else {
                    endPosition = countStringRealPositionThaiSpecialCharacter(textLine, characterSize - countStr,
                            PrinterConst.COUNT_STRING_TYPE_2, characterSize);
                }

                newTextLine = textLine.substring(0, endPosition) + newTextLine;
            }
        } else if (align == PrinterConst.ALIGN_CENTER) {
            int mod = wordLength % 2;
            int subPosition = (wordLength / 2) + mod;
            int leftPosition = (position - subPosition) + 1;

            if (leftPosition < 0) {
                leftPosition = (position - subPosition);

                beginPosition = countStringRealPositionThaiSpecialCharacter(word, leftPosition * -1,
                        PrinterConst.COUNT_STRING_TYPE_1, characterSize) + 1;
                word = word.substring(beginPosition, word.length());
            }

            beginPosition = countStringRealPositionThaiSpecialCharacter(textLine, leftPosition,
                    PrinterConst.COUNT_STRING_TYPE_1, characterSize);

            newTextLine = textLine.substring(0, beginPosition) + word;

            endPosition = countStringRealPositionThaiSpecialCharacter(textLine,
                    countStringInoreThaiSpecialCharacter(newTextLine), PrinterConst.COUNT_STRING_TYPE_1, characterSize);

            newTextLine = newTextLine + textLine.substring(endPosition, textLine.length());

            cutOffPosition = countStringRealPositionThaiSpecialCharacter(newTextLine, characterSize,
                    PrinterConst.COUNT_STRING_TYPE_2, characterSize);

            newTextLine = newTextLine.substring(0, cutOffPosition + 1);

        }

        return newTextLine;
    }

    public static int countStringRealPositionThaiSpecialCharacter(String text, int position, int countType,
            int characterSize) {
        int count = 0;
        int realPosition = 0;
        boolean getPosition = false;

        if (0 < position && position <= characterSize) {
            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                int ascii = (int) character;

                if (!(ascii == 3633 || (ascii >= 3636 && ascii <= 3642) || (ascii >= 3655 && ascii <= 3662))) {
                    count++;

                    if (getPosition) {
                        realPosition = i - 1;
                        break;
                    }

                    if (position == count) {
                        if (countType == PrinterConst.COUNT_STRING_TYPE_1) {
                            realPosition = i;
                            break;

                        } else if (countType == PrinterConst.COUNT_STRING_TYPE_2) {
                            getPosition = true;
                        }
                    }
                }

                if (i == text.length() - 1) {
                    realPosition = i;
                }

            }
        }

        return realPosition;
    }

    public static int countStringInoreThaiSpecialCharacter(String text) {
        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            int ascii = (int) character;
            if (!(ascii == 3633 || (ascii >= 3636 && ascii <= 3642) || (ascii >= 3655 && ascii <= 3662))) {
                count++;
            }
        }

        return count;
    }

    public static int findPosition(int width, int size, double position) {
        double ptd = (width * position / 100);

        double ptdPercent = ptd / width * 100;

        double ptiDouble = size * ptdPercent / 100;
        int pti = (int) (size * ptdPercent / 100);

        if (ptiDouble - pti > 0.5f) {
            pti = pti + 1;
        }

        if (pti == 0) {
            pti = 1;
        }

        return pti;
    }

    public static float findPosition(int width, float position) {
        return findPosition(width, position, PrinterConst.ALIGN_RIGHT, 0);
    }

    public static float findPosition(int width, float position, int align, int textWidth) {
        float pt = (width * position / 100);

        if (align == PrinterConst.ALIGN_RIGHT) {
            pt = pt - textWidth;
        } else if (align == PrinterConst.ALIGN_CENTER) {
            pt = pt - (textWidth / 2);
        }

        return pt;
    }

    private static Layout.Alignment convertToAlignFormat(int format) {
        switch (format) {
            case PrinterConst.ALIGN_LEFT:
                return Layout.Alignment.ALIGN_NORMAL;
            case PrinterConst.ALIGN_CENTER:
                return Layout.Alignment.ALIGN_CENTER;
            case PrinterConst.ALIGN_RIGHT:
                return Layout.Alignment.ALIGN_OPPOSITE;
            // default 128?
            default:
                return Layout.Alignment.ALIGN_NORMAL;
        }
    }
}

class PrinterConst {
    // Constants that indicate the current connection state
    public static final int STATE_IDLE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote device
    public static final int STATE_DISCONNECTED = 4; // now connected to a remote device
    public static final int STATE_CONNECT_FAILED = 5; // now connect failed
    public static final int STATE_CONNECT_LOST = 6;
    public static final int STATE_COVER_OPEN = 7; // cover open
    public static final int STATE_PAPER_EMPTY = 8;
    public static final int STATE_PAPER_JAM = 9;
    public static final int STATE_BATTERY_LOW = 10;
    public static final int STATE_FEATURE_NOT_SUPPORTED = 11;
    public static final int STATE_PRINT_SUCCESS = 12;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    public static final String TCP = "TCP";
    public static final String BLUETOOTH = "BLUETOOTH";
    public static final String USB = "USB";

    public static final int ZIJIANG = 0;
    public static final int EPSON = 1;
    public static final int MINIPOS = 2;
    public static final int CODESOFT = 3;
    public static final int ZEBRA = 4;

    public static final int[] PRINTER_MODEL_ID_ARRAY = new int[] { ZIJIANG, EPSON, MINIPOS, CODESOFT, ZEBRA };
    public static final String[] PRINTER_MODEL_NAME_ARRAY = new String[] { "ZIJIANG", "EPSON", "MINIPOS", "CODESOFT",
            "ZEBRA" };

    public static final int PAPER_WIDTH_58MM = 2; // 384
    public static final int PAPER_WIDTH_80MM = 3; // 576

    public static final int TEXT_MODE = 0;
    public static final int GRAPHIC_MODE = 1;

    public static final int COUNT_STRING_TYPE_1 = 1;
    public static final int COUNT_STRING_TYPE_2 = 2;

    public static final int DRAWER_PIN_2 = 2;
    public static final int DRAWER_PIN_5 = 5;

}

class MsgTypes {

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String INFO = "info";
    public static final String STATE = "state";
    public static final String READ = "read";
    public static final String WRITE = "write";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_INFO = 6;
}

class PrinterCommands {
    public static final byte HT = 0x9;
    public static final byte LF = 0x0A;
    public static final byte CR = 0x0D;
    public static final byte ESC = 0x1B;
    public static final byte DLE = 0x10;
    public static final byte GS = 0x1D;
    public static final byte FS = 0x1C;
    public static final byte STX = 0x02;
    public static final byte US = 0x1F;
    public static final byte CAN = 0x18;
    public static final byte CLR = 0x0C;
    public static final byte EOT = 0x04;

    public static final byte[] INIT = { 27, 64 };
    public static byte[] FEED_LINE = { 10 };

    public static byte[] SELECT_FONT_A = { 20, 33, 0 };

    public static byte[] SET_BAR_CODE_HEIGHT = { 29, 104, 100 };
    public static byte[] PRINT_BAR_CODE_1 = { 29, 107, 2 };
    public static byte[] SEND_NULL_BYTE = { 0x00 };

    public static byte[] SELECT_PRINT_SHEET = { 0x1B, 0x63, 0x30, 0x02 };
    public static byte[] FEED_PAPER_AND_CUT = { 0x1D, 0x56, 66, 0x00 };

    public static byte[] SELECT_CYRILLIC_CHARACTER_CODE_TABLE = { 0x1B, 0x74, 0x11 };

    // public static byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33, -128, 0};
    public static byte[] SELECT_BIT_IMAGE_MODE = { 0x1B, 0x2A, 33, (byte) 255, 3 };
    public static byte[] SET_LINE_SPACING_24 = { 0x1B, 0x33, 24 };
    public static byte[] SET_LINE_SPACING_30 = { 0x1B, 0x33, 30 };

    public static byte[] TRANSMIT_DLE_PRINTER_STATUS = { 0x10, 0x04, 0x01 };
    public static byte[] TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = { 0x10, 0x04, 0x02 };
    public static byte[] TRANSMIT_DLE_ERROR_STATUS = { 0x10, 0x04, 0x03 };
    public static byte[] TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = { 0x10, 0x04, 0x04 };

    public static final byte[] ESC_FONT_COLOR_DEFAULT = new byte[] { 0x1B, 'r', 0x00 };
    public static final byte[] FS_FONT_ALIGN = new byte[] { 0x1C, 0x21, 1, 0x1B, 0x21, 1 };
    public static final byte[] ESC_ALIGN_LEFT = new byte[] { 0x1b, 'a', 0x00 };
    public static final byte[] ESC_ALIGN_RIGHT = new byte[] { 0x1b, 'a', 0x02 };
    public static final byte[] ESC_ALIGN_CENTER = new byte[] { 0x1b, 'a', 0x01 };
    public static final byte[] ESC_CANCEL_BOLD = new byte[] { 0x1B, 0x45, 0 };

    /*********************************************/
    public static final byte[] ESC_HORIZONTAL_CENTERS = new byte[] { 0x1B, 0x44, 20, 28, 00 };
    public static final byte[] ESC_CANCLE_HORIZONTAL_CENTERS = new byte[] { 0x1B, 0x44, 00 };
    /*********************************************/

    public static final byte[] ESC_ENTER = new byte[] { 0x1B, 0x4A, 0x40 };
    public static final byte[] PRINTE_TEST = new byte[] { 0x1D, 0x28, 0x41 };
}

class PrinterCommand {

    /**
     * 打印机初始化
     *
     * @return
     */
    public static byte[] POS_Set_PrtInit() {

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_Init });

        return data;
    }

    /**
     * 打印并换行
     *
     * @return
     */
    public static byte[] POS_Set_LF() {
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.LF });

        return data;
    }

    /**
     * 打印并走纸 (0~255)
     *
     * @param feed
     * @return
     */
    public static byte[] POS_Set_PrtAndFeedPaper(int feed) {
        if (feed > 255 | feed < 0)
            return null;

        Command.ESC_J[2] = (byte) feed;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_J });

        return data;
    }

    /**
     * 打印自检页
     *
     * @return
     */
    public static byte[] POS_Set_PrtSelfTest() {

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.US_vt_eot });
        return data;
    }

    /**
     * 蜂鸣指令
     *
     * @param m 蜂鸣次数
     * @param t 每次蜂鸣的时间
     * @return
     */
    public static byte[] POS_Set_Beep(int m, int t) {

        if ((m < 1 || m > 9) | (t < 1 || t > 9))
            return null;

        Command.ESC_B_m_n[2] = (byte) m;
        Command.ESC_B_m_n[3] = (byte) t;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_B_m_n });
        return data;
    }

    /**
     * 切刀指令(走纸到切刀位置并切纸)
     *
     * @param cut 0~255
     * @return
     */
    public static byte[] POS_Set_Cut(int cut) {
        if (cut > 255 | cut < 0)
            return null;

        Command.GS_V_m_n[3] = (byte) cut;
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_V_m_n });
        return data;
    }

    /**
     * 钱箱指令
     *
     * @param nMode
     * @param nTime1
     * @param nTime2
     * @return
     */
    public static byte[] POS_Set_Cashbox(int nMode, int nTime1, int nTime2) {

        if ((nMode < 0 || nMode > 1) | nTime1 < 0 | nTime1 > 255 | nTime2 < 0 | nTime2 > 255)
            return null;
        Command.ESC_p[2] = (byte) nMode;
        Command.ESC_p[3] = (byte) nTime1;
        Command.ESC_p[4] = (byte) nTime2;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_p });
        return data;
    }

    /**
     * 设置绝对打印位置
     *
     * @param absolute
     * @return
     */
    public static byte[] POS_Set_Absolute(int absolute) {
        if (absolute > 65535 | absolute < 0)
            return null;

        Command.ESC_Relative[2] = (byte) (absolute % 0x100);
        Command.ESC_Relative[3] = (byte) (absolute / 0x100);

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_Relative });
        return data;
    }

    /**
     * 设置相对打印位置
     *
     * @param relative
     * @return
     */
    public static byte[] POS_Set_Relative(int relative) {
        if (relative < 0 | relative > 65535)
            return null;

        Command.ESC_Absolute[2] = (byte) (relative % 0x100);
        Command.ESC_Absolute[3] = (byte) (relative / 0x100);

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_Absolute });
        return data;
    }

    /**
     * 设置左边距
     *
     * @param left
     * @return
     */
    public static byte[] POS_Set_LeftSP(int left) {
        if (left > 255 | left < 0)
            return null;

        Command.GS_LeftSp[2] = (byte) (left % 100);
        Command.GS_LeftSp[3] = (byte) (left / 100);

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_LeftSp });
        return data;
    }

    /**
     * 设置对齐模式
     *
     * @param align
     * @return
     */
    public static byte[] POS_S_Align(int align) {
        if ((align < 0 || align > 2) | (align < 48 || align > 50))
            return null;

        byte[] data = Command.ESC_Align;
        data[2] = (byte) align;
        return data;
    }

    /**
     * 设置打印区域宽度
     *
     * @param width
     * @return
     */
    public static byte[] POS_Set_PrintWidth(int width) {
        if (width < 0 | width > 255)
            return null;

        Command.GS_W[2] = (byte) (width % 100);
        Command.GS_W[3] = (byte) (width / 100);

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_W });
        return data;
    }

    /**
     * 设置默认行间距
     *
     * @return
     */
    public static byte[] POS_Set_DefLineSpace() {

        byte[] data = Command.ESC_Two;
        return data;
    }

    /**
     * 设置行间距
     *
     * @param space
     * @return
     */
    public static byte[] POS_Set_LineSpace(int space) {
        if (space < 0 | space > 255)
            return null;

        Command.ESC_Three[2] = (byte) space;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_Three });
        return data;
    }

    /**
     * 选择字符代码页
     *
     * @param page
     * @return
     */
    public static byte[] POS_Set_CodePage(int page) {
        if (page > 255)
            return null;

        Command.ESC_t[2] = (byte) page;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_t });

        return data;
    }

    /**
     * 打印文本文档
     *
     * @param pszString    要打印的字符串
     * @param encoding     打印字符对应编码
     * @param codepage     设置代码页(0--255)
     * @param nWidthTimes  倍宽(0--4)
     * @param nHeightTimes 倍高(0--4)
     * @param nFontType    字体类型(只对Ascii码有效)(0,1 48,49)
     */
    public static byte[] POS_Print_Text(String pszString, String encoding, int codepage, int nWidthTimes,
            int nHeightTimes, int nFontType) {

        if (codepage < 0 || codepage > 255 || pszString == null || "".equals(pszString) || pszString.length() < 1) {
            return null;
        }

        byte[] pbString = null;
        try {
            pbString = pszString.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30 };
        byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03 };
        Command.GS_ExclamationMark[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);

        Command.ESC_t[2] = (byte) codepage;

        Command.ESC_M[2] = (byte) nFontType;

        if (codepage == 0) {
            byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_ExclamationMark, Command.ESC_t,
                    Command.FS_and, Command.ESC_M, pbString });

            return data;
        } else {
            byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_ExclamationMark, Command.ESC_t,
                    Command.FS_dot, Command.ESC_M, pbString });

            return data;
        }
    }

    /**
     * 加粗指令(最低位为1有效)
     *
     * @param bold
     * @return
     */
    public static byte[] POS_Set_Bold(int bold) {

        Command.ESC_E[2] = (byte) bold;
        Command.ESC_G[2] = (byte) bold;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_E, Command.ESC_G });
        return data;
    }

    /**
     * 设置倒置打印模式(当最低位为1时有效)
     *
     * @param brace
     * @return
     */
    public static byte[] POS_Set_LeftBrace(int brace) {

        Command.ESC_LeftBrace[2] = (byte) brace;
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_LeftBrace });
        return data;
    }

    /**
     * 设置下划线
     *
     * @param line
     * @return
     */
    public static byte[] POS_Set_UnderLine(int line) {

        if ((line < 0 || line > 2))
            return null;

        Command.ESC_Minus[2] = (byte) line;
        Command.FS_Minus[2] = (byte) line;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_Minus, Command.FS_Minus });
        return data;
    }

    /**
     * 选择字体大小(倍高倍宽)
     *
     * @param size
     * @return
     */
    public static byte[] POS_Set_FontSize(int size1, int size2) {
        if (size1 < 0 | size1 > 7 | size2 < 0 | size2 > 7)
            return null;

        byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
        byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        Command.GS_ExclamationMark[2] = (byte) (intToWidth[size1] + intToHeight[size2]);
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_ExclamationMark });
        return data;
    }

    /**
     * 设置反显打印
     *
     * @param inverse
     * @return
     */
    public static byte[] POS_Set_Inverse(int inverse) {

        Command.GS_B[2] = (byte) inverse;

        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.GS_B });

        return data;
    }

    /**
     * 设置旋转90度打印
     *
     * @param rotate
     * @return
     */
    public static byte[] POS_Set_Rotate(int rotate) {
        if (rotate < 0 || rotate > 1)
            return null;
        Command.ESC_V[2] = (byte) rotate;
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_V });
        return data;
    }

    /**
     * 选择字体字型
     *
     * @param font
     * @return
     */
    public static byte[] POS_Set_ChoseFont(int font) {
        if (font > 1 | font < 0)
            return null;

        Command.ESC_M[2] = (byte) font;
        byte[] data = Other.byteArraysToBytes(new byte[][] { Command.ESC_M });
        return data;

    }

    // ***********************************以下函数为公开函数***********************************************************//

    /**
     * 二维码打印函数
     *
     * @param str                   打印二维码数据
     * @param nVersion              二维码类型
     * @param nErrorCorrectionLevel 纠错级别
     * @param nMagnification        放大倍数
     * @return
     */
    public static byte[] getBarCommand(String str, int nVersion, int nErrorCorrectionLevel, int nMagnification) {

        if (nVersion < 0 | nVersion > 19 | nErrorCorrectionLevel < 0 | nErrorCorrectionLevel > 3 | nMagnification < 1
                | nMagnification > 8) {
            return null;
        }

        byte[] bCodeData = null;
        try {
            bCodeData = str.getBytes("GBK");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[bCodeData.length + 7];

        command[0] = 27;
        command[1] = 90;
        command[2] = ((byte) nVersion);
        command[3] = ((byte) nErrorCorrectionLevel);
        command[4] = ((byte) nMagnification);
        command[5] = (byte) (bCodeData.length & 0xff);
        command[6] = (byte) ((bCodeData.length & 0xff00) >> 8);
        System.arraycopy(bCodeData, 0, command, 7, bCodeData.length);

        return command;
    }

    public static byte[] intArrayToByteArray(int[] Iarr) {
        byte[] bytes = new byte[Iarr.length];
        for (int i = 0; i < Iarr.length; i++) {
            bytes[i] = (byte) (Iarr[i] & 0xFF);
        }
        return bytes;
    }

    /**
     * 打印一维条码
     *
     * @param str              打印条码字符
     * @param nType            条码类型(65~73)
     * @param nWidthX          条码宽度
     * @param nHeight          条码高度
     * @param nHriFontType     HRI字型
     * @param nHriFontPosition HRI位置
     * @return
     */
    public static byte[] getCodeBarCommand(String str, int nType, int nWidthX, int nHeight, int nHriFontType,
            int nHriFontPosition) {

        if (nType < 0x41 | nType > 0x49 | nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255 | str.length() == 0)
            return null;

        byte[] bCodeData = null;
        try {
            bCodeData = str.getBytes("GBK");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[bCodeData.length + 16];

        command[0] = 29;
        command[1] = 119;
        command[2] = ((byte) nWidthX);
        command[3] = 29;
        command[4] = 104;
        command[5] = ((byte) nHeight);
        command[6] = 29;
        command[7] = 102;
        command[8] = ((byte) (nHriFontType & 0x01));
        command[9] = 29;
        command[10] = 72;
        command[11] = ((byte) (nHriFontPosition & 0x03));
        command[12] = 29;
        command[13] = 107;
        command[14] = ((byte) nType);
        command[15] = (byte) (byte) bCodeData.length;
        System.arraycopy(bCodeData, 0, command, 16, bCodeData.length);

        return command;
    }

    /**
     * 设置打印模式(选择字体(font:A font:B),加粗,字体倍高倍宽(最大4倍高宽))
     *
     * @param str        打印的字符串
     * @param bold       加粗
     * @param font       选择字型
     * @param widthsize  倍宽
     * @param heigthsize 倍高
     * @return
     */
    public static byte[] POS_Set_Font(String str, int bold, int font, int widthsize, int heigthsize) {

        if (str.length() == 0 | widthsize < 0 | widthsize > 4 | heigthsize < 0 | heigthsize > 4 | font < 0 | font > 1)
            return null;

        byte[] strData = null;
        try {
            strData = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte[] command = new byte[strData.length + 9];

        byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30 };// 最大四倍宽
        byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03 };// 最大四倍高

        command[0] = 27;
        command[1] = 69;
        command[2] = ((byte) bold);
        command[3] = 27;
        command[4] = 77;
        command[5] = ((byte) font);
        command[6] = 29;
        command[7] = 33;
        command[8] = (byte) (intToWidth[widthsize] + intToHeight[heigthsize]);

        System.arraycopy(strData, 0, command, 9, strData.length);
        return command;
    }
    // **********************************************************************************************************//

}

class BluetoothPrinterCore {
    private static final String TAG = "btPrintFile";
    private static final boolean D = true;

    private Context _context = null;
    private String _btMAC = "";
    private String _sFile = "";

    private final BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice = null;

    private Handler mHandler = null;
    private int mState;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothPrinterCore(Context context, Handler handler) {
        _context = context;
        mHandler = handler;
        mState = PrinterConst.STATE_IDLE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothPrinterCore(String sBTmac, String sFileName) {
        _btMAC = sBTmac;
        _sFile = sFileName;
        mState = PrinterConst.STATE_IDLE;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void start() {
        log("start");
        addText("start()");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(PrinterConst.STATE_IDLE);
        addText("start done.");
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        log("stop");
        addText("stop()");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(PrinterConst.STATE_DISCONNECTED);
        addText("stop() done.");
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        addText(MsgTypes.STATE, state);
    }

    public String printESCP() {
        String message = "**   ใบตรวจสอบรายการ   **\r\n" + "**   บะหมี่กุ้งกรุ๊งกริ๊งเนื้อคั่วน้ำผึ้ง **\r\n"
                + "หมายเลขโต๊ะ" + PrintingUtil.convertText("golf", 6, 1) + "\r\n" + getDateTime()
                + PrintingUtil.convertText("20" + "/" + "่มะละคั่วซ้ำ", 16, 1) + "\r\n"
                + "--------------------------------\r\n" + "ชื่อสินค้า         จำนวน    จำนวนเงิน\r\n"
                + "--------------------------------\r\n" + PrintingUtil.thaiR("ก๋วยเตี๋ยวไก่มะละคั่วซ้ำ", 14)
                + PrintingUtil.convertText("20", 6, 1) + PrintingUtil.convertText("400", 12, 1) + "\r\n"
                + "--------------------------------\r\n" + PrintingUtil.thaiR("บะหมี่กุ้งกรุ๊งกริ๊ง", 14)
                + PrintingUtil.convertText("20", 6, 1) + PrintingUtil.convertText("400", 12, 1) + "\r\n"
                + "--------------------------------\r\n";

        return message;
    }

    public String printCustom(String text) {
        String message = text + "\r\n" + text + "\r\n" + text + "\r\n";

        return message;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        addText("connecting to " + device);
        mDevice = device;
        // Cancel any thread attempting to make a connection
        if (mState == PrinterConst.STATE_CONNECTING) {
            addText("already connected. Disconnecting first");
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        addText("new connect thread started");
        setState(PrinterConst.STATE_CONNECTING);
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            Log.i("golf", "ConnectThread");
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_SPP);
                // tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                Log.i("errors", "ddd");
                BluetoothPrinterCore.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothPrinterCore.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * 
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * 
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.i("golf", "connected");
        if (D)
            Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MsgTypes.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MsgTypes.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(PrinterConst.STATE_CONNECTED);

        Log.i("golf", "setState" + getState());
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(PrinterConst.STATE_CONNECT_LOST);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * 
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != PrinterConst.STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(PrinterConst.STATE_CONNECT_FAILED);
    }

    void log(String msg) {
        if (D)
            Log.d(TAG, msg);
    }

    void addText(String s) {
        Message msg = mHandler.obtainMessage(MsgTypes.MESSAGE_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(MsgTypes.INFO, "INFO: " + s);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    void addText(String msgType, int state) {
        // Give the new state to the Handler so the UI Activity can update
        MsgTypes type;
        Message msg;
        Bundle bundle = new Bundle();
        if (msgType.equals(MsgTypes.STATE)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_STATE_CHANGE);// mHandler.obtainMessage(_Activity.MESSAGE_DEVICE_NAME);
        } else if (msgType.equals(MsgTypes.DEVICE_NAME)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_DEVICE_NAME);
        } else if (msgType.equals(MsgTypes.INFO)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_INFO);
        } else if (msgType.equals(MsgTypes.TOAST)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_TOAST);
        } else if (msgType.equals(MsgTypes.READ)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_READ);
        } else if (msgType.equals(MsgTypes.WRITE)) {
            msg = mHandler.obtainMessage(MsgTypes.MESSAGE_WRITE);
        } else {
            msg = new Message();
        }

        bundle.putInt(msgType, state);
        msg.setData(bundle);
        msg.arg1 = state;
        mHandler.sendMessage(msg);
    }

    private String getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String date = df.format(c.getTime());

        return date;
    }
}

class ZJPrinterManager implements IPrinterManager {
    private Activity _context = null;
    private Handler _handler = null;
    private BluetoothPrinterCore printerCore = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private int _paperWidth = 0;
    private int _printMode = 0;
    private int _characterSize = 32;
    private boolean _textBold = false;

    private List<HashMap<String, Object>> textList = new ArrayList<HashMap<String, Object>>();
    private List<byte[]> printerCommandList = new ArrayList<byte[]>();

    private static final String THAI = "CP874";

    public ZJPrinterManager(Activity context, Handler handler) {
        this._context = context;
        this._handler = handler;
    }

    @Override
    public boolean connect(String address, String connectionType) throws Exception {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (device == null) {
            return false;
        }
        printerCore = new BluetoothPrinterCore(_context, _handler);
        if (printerCore == null) {
            return false;
        }

        printerCore.connect(device);

        return true;
    }

    @Override
    public boolean disconnect() throws Exception {
        if (printerCore != null) {
            printerCore.stop();
        }
        printerCore = null;
        return true;
    }

    @Override
    public boolean isConnected() throws Exception {
        if (printerCore.getState() == PrinterConst.STATE_CONNECTED) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setPaperWidth(int width) throws Exception {
        this._paperWidth = 384;

        if (width == PrinterConst.PAPER_WIDTH_58MM) {
            this._characterSize = 32;
        } else if (width == PrinterConst.PAPER_WIDTH_80MM) {
            this._characterSize = 32;
        }

        return true;
    }

    @Override
    public boolean setPrintMode(int mode) throws Exception {
        this._printMode = mode;

        return true;
    }

    @Override
    public boolean setBold(boolean bold) throws Exception {
        printerCommandList.add(PrinterCommand.POS_Set_Bold(bold ? 1 : 0));
        PrintingUtil.setTextBold(bold);

        return true;
    }

    @Override
    public void addImage(Bitmap image) throws Exception {
        addImage(image, PrinterConst.ALIGN_LEFT);
    }

    @Override
    public void addImage(Bitmap image, int align) throws Exception {
        addTextAlign(align);
        byte[] data = PrintPicture.POS_PrintBMP(image, _paperWidth, 0);
        printerCommandList.add(data);
    }

    @Override
    public void addBarcode(String text) throws Exception {
        addBarcode(text, PrinterConst.ALIGN_LEFT);
        
    }

    @Override
    public void addBarcode(String text, int align) throws Exception {
        addTextAlign(align);
        printerCommandList.add(PrinterCommand.POS_Print_Text("\r\n", THAI, 255, 0, 0, 0));
        byte[] bytes = PrinterCommand.getCodeBarCommand(text, 72, 3, 60, 1, 2);
        printerCommandList.add(bytes);
    }

    @Override
    public void addQRcode(String text) throws Exception {
    
        addQRcode(text, PrinterConst.ALIGN_LEFT);
    }

    @Override
    public void addQRcode(String text, int align) throws Exception {
        addTextAlign(align);
        // printerCommandList.add(PrinterCommand.POS_Print_Text("\r\n", THAI, 255, 0, 0, 0));
        // byte[] bytes = PrinterCommand.getBarCommand(text, 0, 3, 6);
        Bitmap myBitmap = QRCode.from(text).withSize(384, 384).bitmap();
        byte[] data = PrintPicture.POS_PrintBMP(myBitmap, _paperWidth, 0);
        printerCommandList.add(data);
       // printerCommandList.add(bytes);

    }

    @Override
    public void addText(String text, double position) throws Exception {
        addText(text, position, PrinterConst.ALIGN_LEFT);
    }

    @Override
    public void addText(String text, double position, int align, boolean... inline) throws Exception {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("text", text);
        hashMap.put("position", position);
        hashMap.put("align", align);
        hashMap.put("inline", inline);
        textList.add(hashMap);
    }

    @Override
    public void flushText() throws Exception {
        // graphic mode
        Log.i("flushText", "flushText");
        Bitmap line = null;

        for (HashMap<String, Object> hashMap : textList) {
            String text = hashMap.get("text").toString();
            Double position = (Double) hashMap.get("position");
            int align = (int) hashMap.get("align");
            boolean inline[] = (boolean[]) hashMap.get("inline");

            if (inline != null && inline.length > 0 && inline[0] == false) {
                line = PrintingUtil.formattingPrinterGraphicModeOutLine(this._paperWidth, text, position, align);
            } else {
                line = PrintingUtil.formattingPrinterGraphicMode(this._paperWidth, line, text, position, align);

            }
        }

        if(line != null){
            byte[] bytes = PrintPicture.POS_PrintBMP(line, this._paperWidth, 0);
            printerCommandList.add(bytes);
        }
   

        textList.clear();

    }

    @Override
    public void addCmdFeedLine() throws Exception {
        printerCommandList.add(PrinterCommands.FEED_LINE);
    }

    @Override
    public void addFeedLine(int line,int multiple) throws Exception {
        if(multiple == 0){
            multiple = 35;
        }
        printerCommandList.add(PrinterCommand.POS_Set_PrtAndFeedPaper(multiple * line));
        printerCommandList.add(Command.GS_V_m_n);
    }

    @Override
    public void addPageEnd() throws Exception {
        printerCommandList.add(PrinterCommand.POS_Set_PrtAndFeedPaper(90));
        printerCommandList.add(Command.GS_V_m_n);
    }

    @Override
    public void addCut() throws Exception {

    }

    @Override
    public void openDrawer(int pin) throws Exception {

    }

    @Override
    public void sendData() throws Exception {
        for (int i = 0; i < printerCommandList.size(); i++) {
            printerCore.write(printerCommandList.get(i));
        }

        printerCommandList.clear();
    }

    public void addTextAlign(int align) throws Exception {
        if (align == PrinterConst.ALIGN_CENTER) {
            Command.ESC_Align[2] = 0x01; // Align center
            printerCommandList.add(Command.ESC_Align);
        } else if (align == PrinterConst.ALIGN_LEFT) {
            Command.ESC_Align[2] = 0x00; // Align left
            printerCommandList.add(Command.ESC_Align);
        } else if (align == PrinterConst.ALIGN_RIGHT) {
            Command.ESC_Align[2] = 0x02; // Align right
            printerCommandList.add(Command.ESC_Align);
        }
    }
}

class Command {

    private static final byte ESC = 0x1B;
    private static final byte FS = 0x1C;
    private static final byte GS = 0x1D;
    private static final byte US = 0x1F;
    private static final byte DLE = 0x10;
    private static final byte DC4 = 0x14;
    private static final byte DC1 = 0x11;
    private static final byte SP = 0x20;
    private static final byte NL = 0x0A;
    private static final byte FF = 0x0C;
    public static final byte PIECE = (byte) 0xFF;
    public static final byte NUL = (byte) 0x00;

    // 打印机初始化
    public static byte[] ESC_Init = new byte[] { ESC, '@' };

    /**
     * 打印命令
     */
    // 打印并换行
    public static byte[] LF = new byte[] { NL };

    // 打印并走纸
    public static byte[] ESC_J = new byte[] { ESC, 'J', 0x00 };
    public static byte[] ESC_d = new byte[] { ESC, 'd', 0x00 };

    // 打印自检页
    public static byte[] US_vt_eot = new byte[] { US, DC1, 0x04 };

    // 蜂鸣指令
    public static byte[] ESC_B_m_n = new byte[] { ESC, 'B', 0x00, 0x00 };

    // 切刀指令
    public static byte[] GS_V_n = new byte[] { GS, 'V', 0x00 };
    public static byte[] GS_V_m_n = new byte[] { GS, 'V', 'B', 0x00 };
    public static byte[] GS_i = new byte[] { ESC, 'i' };
    public static byte[] GS_m = new byte[] { ESC, 'm' };

    /**
     * 字符设置命令
     */
    // 设置字符右间距
    public static byte[] ESC_SP = new byte[] { ESC, SP, 0x00 };

    // 设置字符打印字体格式
    public static byte[] ESC_ExclamationMark = new byte[] { ESC, '!', 0x00 };

    // 设置字体倍高倍宽
    public static byte[] GS_ExclamationMark = new byte[] { GS, '!', 0x00 };

    // 设置反显打印
    public static byte[] GS_B = new byte[] { GS, 'B', 0x00 };

    // 取消/选择90度旋转打印
    public static byte[] ESC_V = new byte[] { ESC, 'V', 0x00 };

    // 选择字体字型(主要是ASCII码)
    public static byte[] ESC_M = new byte[] { ESC, 'M', 0x00 };

    // 选择/取消加粗指令
    public static byte[] ESC_G = new byte[] { ESC, 'G', 0x00 };
    public static byte[] ESC_E = new byte[] { ESC, 'E', 0x00 };

    // 选择/取消倒置打印模式
    public static byte[] ESC_LeftBrace = new byte[] { ESC, '{', 0x00 };

    // 设置下划线点高度(字符)
    public static byte[] ESC_Minus = new byte[] { ESC, 45, 0x00 };

    // 字符模式
    public static byte[] FS_dot = new byte[] { FS, 46 };

    // 汉字模式
    public static byte[] FS_and = new byte[] { FS, '&' };

    // 设置汉字打印模式
    public static byte[] FS_ExclamationMark = new byte[] { FS, '!', 0x00 };

    // 设置下划线点高度(汉字)
    public static byte[] FS_Minus = new byte[] { FS, 45, 0x00 };

    // 设置汉字左右间距
    public static byte[] FS_S = new byte[] { FS, 'S', 0x00, 0x00 };

    // 选择字符代码页
    public static byte[] ESC_t = new byte[] { ESC, 't', 0x00 };

    /**
     * 格式设置指令
     */
    // 设置默认行间距
    public static byte[] ESC_Two = new byte[] { ESC, 50 };

    // 设置行间距
    public static byte[] ESC_Three = new byte[] { ESC, 51, 0x00 };

    // 设置对齐模式
    public static byte[] ESC_Align = new byte[] { ESC, 'a', 0x00 };

    // 设置左边距
    public static byte[] GS_LeftSp = new byte[] { GS, 'L', 0x00, 0x00 };

    // 设置绝对打印位置
    // 将当前位置设置到距离行首（nL + nH x 256）处。
    // 如果设置位置在指定打印区域外，该命令被忽略
    public static byte[] ESC_Relative = new byte[] { ESC, '$', 0x00, 0x00 };

    // 设置相对打印位置
    public static byte[] ESC_Absolute = new byte[] { ESC, 92, 0x00, 0x00 };

    // 设置打印区域宽度
    public static byte[] GS_W = new byte[] { GS, 'W', 0x00, 0x00 };

    /**
     * 状态指令
     */
    // 实时状态传送指令
    public static byte[] DLE_eot = new byte[] { DLE, 0x04, 0x00 };

    // 实时弹钱箱指令
    public static byte[] DLE_DC4 = new byte[] { DLE, DC4, 0x00, 0x00, 0x00 };

    // 标准弹钱箱指令
    public static byte[] ESC_p = new byte[] { ESC, 'F', 0x00, 0x00, 0x00 };

    /**
     * 条码设置指令
     */
    // 选择HRI打印方式
    public static byte[] GS_H = new byte[] { GS, 'H', 0x00 };

    // 设置条码高度
    public static byte[] GS_h = new byte[] { GS, 'h', (byte) 0xa2 };

    // 设置条码宽度
    public static byte[] GS_w = new byte[] { GS, 'w', 0x00 };

    // 设置HRI字符字体字型
    public static byte[] GS_f = new byte[] { GS, 'f', 0x00 };

    // 条码左偏移指令
    public static byte[] GS_x = new byte[] { GS, 'x', 0x00 };

    // 打印条码指令
    public static byte[] GS_k = new byte[] { GS, 'k', 'A', FF };

    // 二维码相关指令
    public static byte[] GS_k_m_v_r_nL_nH = new byte[] { ESC, 'Z', 0x03, 0x03, 0x08, 0x00, 0x00 };

}

class PrintPicture {

    /**
     * 打印位图函数 此函数是将一行作为一个图片打印，这样处理不容易出错
     *
     * @param mBitmap
     * @param nWidth
     * @param nMode
     * @return
     */
    public static byte[] POS_PrintBMP(Bitmap mBitmap, int nWidth, int nMode) {
        // 先转黑白，再调用函数缩放位图

        

        int width = ((nWidth + 7) / 8) * 8;
        int height = mBitmap.getHeight() * width / mBitmap.getWidth();
        height = ((height + 7) / 8) * 8;

        Bitmap rszBitmap = mBitmap;
        if (mBitmap.getWidth() != width) {
            rszBitmap = Other.resizeImage(mBitmap, width, height);
        }

        Bitmap grayBitmap = Other.toGrayscale(rszBitmap);

        byte[] dithered = Other.thresholdToBWPic(grayBitmap);

        byte[] data = Other.eachLinePixToCmd(dithered, width, nMode);

        return data;
    }

    public static byte[] POS_PrintBMP(Bitmap mBitmap, int nWidth, int nHeight, int nMode) {

        Bitmap rszBitmap = Other.resizeImage(mBitmap, nWidth, nHeight);

        return Other.eachLinePixToCmd(Other.thresholdToBWPic(Other.toGrayscale(rszBitmap)), nWidth, nMode);
    }

    /**
     * 使用下传位图打印图片 先收完再打印
     *
     * @param bmp
     * @return
     */
    public static byte[] Print_1D2A(Bitmap bmp) {

        /*
         * 使用下传位图打印图片 先收完再打印
         */
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        byte data[] = new byte[1024 * 10];
        data[0] = 0x1D;
        data[1] = 0x2A;
        data[2] = (byte) ((width - 1) / 8 + 1);
        data[3] = (byte) ((height - 1) / 8 + 1);
        byte k = 0;
        int position = 4;
        int i;
        int j;
        byte temp = 0;
        for (i = 0; i < width; i++) {

            System.out.println("进来了...I");
            for (j = 0; j < height; j++) {
                System.out.println("进来了...J");
                if (bmp.getPixel(i, j) != -1) {
                    temp |= (0x80 >> k);
                } // end if
                k++;
                if (k == 8) {
                    data[position++] = temp;
                    temp = 0;
                    k = 0;
                } // end if k
            } // end for j
            if (k % 8 != 0) {
                data[position++] = temp;
                temp = 0;
                k = 0;
            }

        }
        System.out.println("data" + data);

        if (width % 8 != 0) {
            i = height / 8;
            if (height % 8 != 0)
                i++;
            j = 8 - (width % 8);
            for (k = 0; k < i * j; k++) {
                data[position++] = 0;
            }
        }
        return data;
    }

}

class Other {
    public byte[] buf;
    public int index;
    private static final int WIDTH_80 = 576;
    private static final int WIDTH_58 = 384;
    private static int[] p0 = new int[] { 0, 128 };
    private static int[] p1 = new int[] { 0, 64 };
    private static int[] p2 = new int[] { 0, 32 };
    private static int[] p3 = new int[] { 0, 16 };
    private static int[] p4 = new int[] { 0, 8 };
    private static int[] p5 = new int[] { 0, 4 };
    private static int[] p6 = new int[] { 0, 2 };
    private static final byte[] chartobyte = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 10, 11, 12,
            13, 14, 15 };

    public Other(int length) {
        this.buf = new byte[length];
        this.index = 0;
    }

    public static StringBuilder RemoveChar(String str, char c) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();

        for (int i = 0; i < length; ++i) {
            char tmp = str.charAt(i);
            if (tmp != c) {
                sb.append(tmp);
            }
        }

        return sb;
    }

    public static boolean IsHexChar(char c) {
        return c >= 48 && c <= 57 || c >= 97 && c <= 102 || c >= 65 && c <= 70;
    }

    public static byte HexCharsToByte(char ch, char cl) {
        byte b = (byte) (chartobyte[ch - 48] << 4 & 240 | chartobyte[cl - 48] & 15);
        return b;
    }

    public static byte[] HexStringToBytes(String str) {
        int count = str.length();
        byte[] data = null;
        if (count % 2 == 0) {
            data = new byte[count / 2];

            for (int i = 0; i < count; i += 2) {
                char ch = str.charAt(i);
                char cl = str.charAt(i + 1);
                if (!IsHexChar(ch) || !IsHexChar(cl)) {
                    data = null;
                    break;
                }

                if (ch >= 97) {
                    ch = (char) (ch - 32);
                }

                if (cl >= 97) {
                    cl = (char) (cl - 32);
                }

                data[i / 2] = HexCharsToByte(ch, cl);
            }
        }

        return data;
    }

    public void UTF8ToGBK(String Data) {
        try {
            byte[] bs = Data.getBytes("GBK");
            int DataLength = bs.length;

            for (int i = 0; i < DataLength; ++i) {
                this.buf[this.index++] = bs[i];
            }
        } catch (UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }

    }

    public static byte[] StringTOGBK(String data) {
        byte[] buffer = null;

        try {
            buffer = data.getBytes("GBK");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
        }

        return buffer;
    }

    public static Bitmap createAppIconText(String txt, float size, boolean is58mm, int hight, Alignment alignment,
            int fonStyle) {
        Bitmap canvasBitmap;
        int width;
        Canvas canvas;
        TextPaint paint;
        StaticLayout layout;
        if (is58mm) {
            canvasBitmap = Bitmap.createBitmap(384, hight, Config.ARGB_8888);
            width = canvasBitmap.getWidth();
            canvas = new Canvas(canvasBitmap);
            canvas.setBitmap(canvasBitmap);
            canvas.drawColor(-1);
            paint = new TextPaint();
            paint.setColor(-16777216);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setFakeBoldText(false);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, fonStyle));
            layout = new StaticLayout(txt, 0, txt.length(), paint, width, alignment, 1.1F, 0.0F, true, TruncateAt.END,
                    width);
            canvas.translate(0.0F, 5.0F);

            layout.draw(canvas);
            canvas.save();
            canvas.restore();
            return canvasBitmap;
        } else {
            canvasBitmap = Bitmap.createBitmap(576, hight, Config.ARGB_8888);
            width = canvasBitmap.getWidth();
            canvas = new Canvas(canvasBitmap);
            canvas.setBitmap(canvasBitmap);
            canvas.drawColor(-1);
            paint = new TextPaint();
            paint.setColor(-16777216);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setFakeBoldText(false);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, fonStyle));
            layout = new StaticLayout(txt, 0, txt.length(), paint, width, Alignment.ALIGN_NORMAL, 1.1F, 0.0F, true,
                    TruncateAt.END, width);
            canvas.translate(0.0F, 5.0F);
            layout.draw(canvas);
            canvas.save();
            canvas.restore();
            return canvasBitmap;
        }
    }

    public static Bitmap createAppIconTextBothBeside(String txtLeft, String txtRight, int leftWidth, int rightWidth,
            float size, boolean is58mm, int height, int fonStyle) {
        Bitmap canvasBitmapLeft;
        Bitmap canvasBitmapRight;
        Bitmap btm = Bitmap.createBitmap(384, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(btm);

        int width;
        Canvas canvas;
        TextPaint paint;
        StaticLayout layout;

        if (is58mm) {
            canvasBitmapLeft = Bitmap.createBitmap(leftWidth, height, Config.ARGB_8888);
            width = canvasBitmapLeft.getWidth();
            canvas = new Canvas(canvasBitmapLeft);
            canvas.setBitmap(canvasBitmapLeft);
            canvas.drawColor(-1);
            paint = new TextPaint();
            paint.setColor(-16777216);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setFakeBoldText(false);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, fonStyle));
            layout = new StaticLayout(txtLeft, 0, txtLeft.length(), paint, width, Alignment.ALIGN_NORMAL, 1.1F, 0.0F,
                    true, TruncateAt.END, width);
            canvas.translate(0.0F, 5.0F);

            layout.draw(canvas);
            canvas.save();
            canvas.restore();

            canvasBitmapRight = Bitmap.createBitmap(rightWidth, height, Config.ARGB_8888);
            width = canvasBitmapRight.getWidth();
            canvas = new Canvas(canvasBitmapRight);
            canvas.setBitmap(canvasBitmapRight);
            canvas.drawColor(-1);
            paint = new TextPaint();
            paint.setColor(-16777216);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setFakeBoldText(false);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, fonStyle));
            layout = new StaticLayout(txtRight, 0, txtRight.length(), paint, width, Alignment.ALIGN_OPPOSITE, 1.1F,
                    0.0F, true, TruncateAt.END, width);
            canvas.translate(0.0F, 5.0F);

            layout.draw(canvas);
            canvas.save();
            canvas.restore();

            comboImage.drawBitmap(canvasBitmapLeft, 0f, 0f, null);
            comboImage.drawBitmap(canvasBitmapRight, canvasBitmapLeft.getWidth(), 0f, null);

            return btm;
        } else {
            canvasBitmapRight = Bitmap.createBitmap(576, height, Config.ARGB_8888);
            // width = canvasBitmap.getWidth();
            // canvas = new Canvas(canvasBitmap);
            // canvas.setBitmap(canvasBitmap);
            // canvas.drawColor(-1);
            // paint = new TextPaint();
            // paint.setColor(-16777216);
            // paint.setTextSize(size);
            // paint.setAntiAlias(true);
            // paint.setStyle(Style.FILL);
            // paint.setFakeBoldText(false);
            // layout = new StaticLayout(txt, 0, txt.length(), paint, width,
            // Alignment.ALIGN_NORMAL, 1.1F, 0.0F, true, TruncateAt.END, width);
            // canvas.translate(0.0F, 5.0F);
            // layout.draw(canvas);
            // canvas.save(31);
            // canvas.restore();
            return canvasBitmapRight;
        }
    }

    public static Bitmap createAppIconTextMultiBlockInline(String[] arrTxt, int[] arrWidth, float[] arrSize,
            boolean is58mm, Alignment[] arrAlignment, int fonStyle, int includeHeight, boolean inline,
            int heightInline) {
        if (is58mm) {
            Bitmap[] canvasBitmap = new Bitmap[arrTxt.length];
            Canvas canvas;
            TextPaint paint;
            StaticLayout[] layout = new StaticLayout[arrTxt.length];
            int maxHeight = 0;

            int prevImageWidth = 0;

            for (int i = 0; i < arrTxt.length; i++) {
                paint = new TextPaint();
                paint.setColor(-16777216);
                paint.setTextSize(arrSize[i]);
                paint.setAntiAlias(true);
                paint.setStyle(Style.FILL);
                paint.setFakeBoldText(false);

                paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, fonStyle));
                layout[i] = new StaticLayout(arrTxt[i], 0, arrTxt[i].length(), paint, arrWidth[i], arrAlignment[i],
                        1.1F, 0.0F, true, TruncateAt.END, arrWidth[i]);

                maxHeight = layout[i].getHeight() > maxHeight ? layout[i].getHeight() : maxHeight;
            }

            if (inline) {
                maxHeight = heightInline;
            } else {
                maxHeight = maxHeight + includeHeight;
            }

            Bitmap btm = Bitmap.createBitmap(384, maxHeight, Bitmap.Config.ARGB_8888);
            Canvas comboImage = new Canvas(btm);

            for (int i = 0; i < arrTxt.length; i++) {
                canvasBitmap[i] = Bitmap.createBitmap(arrWidth[i], maxHeight, Config.ARGB_8888);
                canvas = new Canvas(canvasBitmap[i]);
                canvas.setBitmap(canvasBitmap[i]);
                canvas.drawColor(-1);
                canvas.translate(0.0F, 5.0F);
                layout[i].draw(canvas);
                canvas.save();
                canvas.restore();

                comboImage.drawBitmap(canvasBitmap[i], prevImageWidth, 0f, null);
                prevImageWidth = prevImageWidth + canvasBitmap[i].getWidth();

            }

            return btm;
        } else {
            Bitmap btm = Bitmap.createBitmap(384, 50, Bitmap.Config.ARGB_8888);

            return btm;
        }
    }

    public static Bitmap convertTextToBitmapMultiBlock(String[] arrTxt, int[] arrWidth, float fontSize,
            Alignment[] arrAlignment, Typeface typeface, int fonStyle, int includeHeight, boolean inline,
            int heightInline) {

        Bitmap[] canvasBitmap = new Bitmap[arrTxt.length];
        Canvas canvas;
        TextPaint paint;
        StaticLayout[] layout = new StaticLayout[arrTxt.length];
        int maxWidth = 0;
        int maxHeight = 0;

        int prevImageWidth = 0;

        for (int i = 0; i < arrTxt.length; i++) {
            paint = new TextPaint();
            paint.setColor(-16777216);
            paint.setTextSize(fontSize);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
            paint.setFakeBoldText(false);

            paint.setTypeface(Typeface.create(typeface, fonStyle));

            layout[i] = new StaticLayout(arrTxt[i], 0, arrTxt[i].length(), paint, arrWidth[i], arrAlignment[i], 1.1F,
                    0.0F, true, TruncateAt.END, arrWidth[i]);

            maxHeight = layout[i].getHeight() > maxHeight ? layout[i].getHeight() : maxHeight;

            maxWidth += arrWidth[i];
        }

        if (inline) {
            maxHeight = heightInline;
        } else {
            maxHeight = maxHeight + includeHeight;
        }

        Bitmap btm = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(btm);

        for (int i = 0; i < arrTxt.length; i++) {
            canvasBitmap[i] = Bitmap.createBitmap(arrWidth[i], maxHeight, Config.ARGB_8888);
            canvas = new Canvas(canvasBitmap[i]);
            canvas.setBitmap(canvasBitmap[i]);
            canvas.drawColor(-1);
            canvas.translate(0.0F, 5.0F);
            layout[i].draw(canvas);
            canvas.save();
            canvas.restore();

            comboImage.drawBitmap(canvasBitmap[i], prevImageWidth, 0f, null);
            prevImageWidth = prevImageWidth + canvasBitmap[i].getWidth();
        }

        return btm;

    }

    public static byte[] byteArraysToBytes(byte[][] data) {
        int length = 0;

        for (int i = 0; i < data.length; ++i) {
            length += data[i].length;
        }

        byte[] send = new byte[length];
        int k = 0;

        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < data[i].length; ++j) {
                send[k++] = data[i][j];
            }
        }

        return send;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (float) w / (float) width;
        float scaleHeight = (float) h / (float) height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    public static void saveMyBitmap(Bitmap mBitmap, String name) {
        File f = new File(Environment.getExternalStorageDirectory().getPath(), name);

        try {
            f.createNewFile();
        } catch (IOException var7) {
            ;
        }

        FileOutputStream fOut = null;

        try {
            fOut = new FileOutputStream(f);
            mBitmap.compress(CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException var5) {
            ;
        } catch (IOException var6) {
            ;
        }

    }

    public static byte[] thresholdToBWPic(Bitmap mBitmap) {
        int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
        byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
        mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        format_K_threshold(pixels, mBitmap.getWidth(), mBitmap.getHeight(), data);
        return data;
    }

    private static void format_K_threshold(int[] orgpixels, int xsize, int ysize, byte[] despixels) {
        int graytotal = 0;

        int k = 0;

        int i;
        int j;
        int gray;
        for (i = 0; i < ysize; ++i) {
            for (j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                graytotal += gray;
                ++k;
            }
        }

        int grayave = graytotal / ysize / xsize;
        k = 0;

        for (i = 0; i < ysize; ++i) {
            for (j = 0; j < xsize; ++j) {
                gray = orgpixels[k] & 255;
                if (gray > grayave) {
                    despixels[k] = 0;
                } else {
                    despixels[k] = 1;
                }

                ++k;
            }
        }

    }

    public static void overWriteBitmap(Bitmap mBitmap, byte[] dithered) {
        int ysize = mBitmap.getHeight();
        int xsize = mBitmap.getWidth();
        int k = 0;

        for (int i = 0; i < ysize; ++i) {
            for (int j = 0; j < xsize; ++j) {
                if (dithered[k] == 0) {
                    mBitmap.setPixel(j, i, -1);
                } else {
                    mBitmap.setPixel(j, i, -16777216);
                }

                ++k;
            }
        }

    }

    public static byte[] eachLinePixToCmd(byte[] src, int nWidth, int nMode) {
        int nHeight = src.length / nWidth;
        int nBytesPerLine = nWidth / 8;
        byte[] data = new byte[nHeight * (8 + nBytesPerLine)];

        int k = 0;

        for (int i = 0; i < nHeight; ++i) {
            int offset = i * (8 + nBytesPerLine);
            data[offset + 0] = 29;
            data[offset + 1] = 118;
            data[offset + 2] = 48;
            data[offset + 3] = (byte) (nMode & 1);
            data[offset + 4] = (byte) (nBytesPerLine % 256);
            data[offset + 5] = (byte) (nBytesPerLine / 256);
            data[offset + 6] = 1;
            data[offset + 7] = 0;

            for (int j = 0; j < nBytesPerLine; ++j) {
                data[offset + 8 + j] = (byte) (p0[src[k]] + p1[src[k + 1]] + p2[src[k + 2]] + p3[src[k + 3]]
                        + p4[src[k + 4]] + p5[src[k + 5]] + p6[src[k + 6]] + src[k + 7]);
                k += 8;
            }
        }

        return data;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}
