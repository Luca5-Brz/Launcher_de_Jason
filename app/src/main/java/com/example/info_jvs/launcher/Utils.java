package com.example.info_jvs.launcher;

/**
 * Created by info-jvs on 24-03-17.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
//import org.apache.http.conn.util.InetAddressUtils;

public class Utils {

    /**
     * Convert byte array to hex string
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for(int idx=0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Get utf8 byte array.
     * @param str
     * @return  array of NULL if error was found
     */
    public static byte[] getUTF8Bytes(String str) {
        try { return str.getBytes("UTF-8"); } catch (Exception ex) { return null; }
    }

    /**
     * Load UTF8withBOM or any ansi text file.
     * @param filename
     * @return
     * @throws java.io.IOException
     */
    public static String loadFileAsString(String filename) throws java.io.IOException {
        final int BUFLEN=1024;
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
            byte[] bytes = new byte[BUFLEN];
            boolean isUTF8=false;
            int read,count=0;
            while((read=is.read(bytes)) != -1) {
                if (count==0 && bytes[0]==(byte)0xEF && bytes[1]==(byte)0xBB && bytes[2]==(byte)0xBF ) {
                    isUTF8=true;
                    baos.write(bytes, 3, read-3); // drop UTF8 bom marker
                } else {
                    baos.write(bytes, 0, read);
                }
                count+=read;
            }
            return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
        } finally {
            try{ is.close(); } catch(Exception ex){}
        }
    }

    /**
     * Returns MAC address of the given interface name.
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return  mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }



    /**
     //* Get IP address from first non-localhost interface
     //* @param ipv4  true=return ipv4, false=return ipv6
    // * @return  address or empty string
    // */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }


    public static boolean checkForInstalledApp(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            pm.getPackageInfo(pkgName, 0);
            // Log.d(TAG, pkgString + " is installed");
            return true;
        } catch (NameNotFoundException e) {
            // Log.d(TAG, pkgString + " is not installed");
            return false;
        }

    }

    public static String getAppName(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            String label = pm.getApplicationLabel(appInfo).toString();
            return label;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static String getAppVersionName(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            String ver = pkgInfo.versionName;

            return ver;
        } catch (NameNotFoundException e) {
            return "0";
        }
    }

    public static int getAppVersionCode(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            return pkgInfo.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public void DoSleep(Context c, int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e1) {

            e1.printStackTrace();
        }
    }



}
