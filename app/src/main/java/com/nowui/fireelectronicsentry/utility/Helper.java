package com.nowui.fireelectronicsentry.utility;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Helper {

    public static final String WebUrl = "http://oa.herigbit.cn/OAFramework";
    //public static final String WebUrl = "http://leon.ngrok.cc/OA";

    public static final String AdminWebUrl = "http://www.herigbit.com/FSM";

    public static final int SystemType = 0;

    public static final boolean isAdmin = false;

    public static final String DatabaseName = "database.db";
    public static final int DatabaseVersion = 1;

    public static final long PostDelay = 60 * 1000 * 30;

    //Key
    public static final String KeyUrl = "url";
    public static final String KeyText = "text";
    public static final String KeyParameter = "parameter";
    public static final String KeyIsOpen = "isOpen";
    public static final String KeyMimeType = "mimeType";
    public static final String KeyName = "name";
    public static final String KeyIsLocal = "isLocal";
    public static final String KeyPath = "path";
    public static final String KeyAppSetting = "appSetting";
    public static final String KeyDepartmentId = "departmentId";
    public static final String KeyDepartmentName = "departmentName";
    public static final String KeyChoiceDepartmentId = "choiceDepartmentId";
    public static final String KeyChoiceDepartmentName = "choiceDepartmentName";
    public static final String KeyUDID = "udid";
    public static final String KeyUserId = "userId";
    public static final String KeyUserName = "userName";
    public static final String KeyLogo = "logo";
    public static final String KeyBaiduUserId = "baiduUserId";
    public static final String KeyBaiduChannelId = "baiduChannelId";
    public static final String KeyWeather = "weather";
    public static final String KeyUserType = "userType";

    //Event
    public static final String EventPull = "Pull";
    public static final String EventTitle = "Title";
    public static final String EventBack = "Back";
    public static final String EventBackCallback = "BackCallback";
    public static final String EventNormal = "Normal";
    public static final String EventNormalCallback = "NormalCallback";
    public static final String EventDownload = "Download";
    public static final String EventShowLoading = "ShowLoading";
    public static final String EventHideLoading = "HideLoading";
    public static final String EventShowAlert = "ShowAlert";
    public static final String EventInitMap = "InitMap";

    //Code
    public static final int CodeRequest = 0;
    public static final int CodeRequest1 = 1;
    public static final int CodeResult = 10;

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null)
            return true;

        if (obj instanceof CharSequence)
            return ((CharSequence) obj).length() == 0;

        if (obj instanceof Collection)
            return ((Collection<?>) obj).isEmpty();

        if (obj instanceof Map)
            return ((Map<?, ?>) obj).isEmpty();

        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            boolean empty = true;
            for (int i = 0; i < object.length; i++) {
                if (!isNullOrEmpty(object[i])) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
        return false;
    }

    public static String formatDateTime() {
        return dateTimeFormat.format(new Date());
    }

    public static Date formatDate(String datetime) {
        try {
            return dateTimeFormat.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date();
    }

    public static String getMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

        return dateFormat.format(calendar.getTime());
    }

    public static String getMonthLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMaximum(Calendar.DAY_OF_MONTH));
        return dateFormat.format(calendar.getTime());
    }

    public static int getDayOfMonth() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int day = calendar.getActualMaximum(Calendar.DATE);
        return day;
    }

    public static int getDayOfMonth(String datetime) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        try {
            calendar.setTime(dateFormat.parse(datetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int day = calendar.getActualMaximum(Calendar.DATE);
        return day;
    }

    public static int getDay() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int day = calendar.get(Calendar.DATE);
        return day;
    }

    public static int getDay(String datetime) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        try {
            calendar.setTime(dateFormat.parse(datetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int day = calendar.get(Calendar.DATE);
        return day;
    }

    public static int getDayOfWeek(String date) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        try {
            calendar.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getMonth() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int month = calendar.get(Calendar.MONTH);
        return month;
    }

    public static int getMonth(String datetime) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        try {
            calendar.setTime(dateFormat.parse(datetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int month = calendar.get(Calendar.MONTH);
        return month;
    }

    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeBase64(byte[] data) {
        try {
            return new String(Base64.encodeBase64(data), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeBase64ForFile(String path) {
        File file=new File(path);
        if(!file.exists()) {
            return "";
        }

        byte[] data = null;
        try {
            InputStream in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(Base64.encodeBase64(data));
    }

    public static Bitmap getBitmap(byte[] data, float degrees) {
        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap bMapRotate;
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(degrees);
        bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
        bMap = bMapRotate;

        return bMap;
    }

    public static void saveToSDCard(byte[] data, float degrees, String name) throws IOException {
        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Bitmap bMapRotate;
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postRotate(degrees);
        bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
        bMap = bMapRotate;

        System.out.println("width:" + bMap.getWidth());
        System.out.println("height:" + bMap.getHeight());

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        String filename = format.format(date) + ".jpg";
        filename = name + ".jpg";
        File fileFolder = new File(Environment.getExternalStorageDirectory() + "/FireElectronicSentry/");
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            fileFolder.mkdir();
        }
        File jpgFile = new File(fileFolder, filename);
        /*FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        outputStream.write(data); // 写入sd卡中
        outputStream.close(); // 关闭输出流*/

        /*try {
            FileOutputStream fos = new FileOutputStream(jpgFile);
            bMap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception error) {
            error.printStackTrace();
        }*/

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(jpgFile));
        bMap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
    }

    public static boolean isAppOnForeground(Context context, String packageName) {
        // Returns a list of application processes that are running on the device
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    public static  Camera.Size getOptimalPictureSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static String substring(String str, int len) {
        len = len * 3;
        String result = null;
        if (str != null) {
            byte[] a = str.getBytes();
            if (a.length <= len) {
                result = str;
            } else if (len > 0) {
                result = new String(a, 0, len);
                int length = result.length();
                if (str.charAt(length - 1) != result.charAt(length - 1)) {
                    if (length < 2) {
                        result = null;
                    } else {
                        result = result.substring(0, length - 1);
                    }
                }
            }
        }
        return result;
    }

    public static void show(String str) {
        int maxLogSize = 1000;

        for(int i = 0; i <= str.length() / maxLogSize; i++) {

            int start = i * maxLogSize;

            int end = (i+1) * maxLogSize;

            end = end > str.length() ? str.length() : end;

            Log.v("aaa", str.substring(start, end));

        }
    }

}
