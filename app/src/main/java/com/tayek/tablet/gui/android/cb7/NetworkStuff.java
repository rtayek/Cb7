package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import android.media.*;
import android.net.wifi.*;
import android.widget.*;

import com.tayek.io.*;

import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

import static com.tayek.io.IO.*;
public class NetworkStuff {
    NetworkStuff(MainActivity mainActivity) {
        this.mainActivity=mainActivity;
    }
    void checkWifi() {
        WifiManager wifiManager=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            p("enabling wifi.");
            boolean ok=wifiManager.setWifiEnabled(true);
            if(ok)
                p("wifi was enabled.");
            else
                p("wifi was not enabled!");
        }
        List<WifiConfiguration> list=wifiManager.getConfiguredNetworks();
        if(list!=null)
            for(WifiConfiguration wifiConfiguration : list) {
                InetAddress inetAddress=null;
                p("configured: "+wifiConfiguration.SSID+", status: "+wifiConfiguration.status+": "+WifiConfiguration.Status.strings[wifiConfiguration.status]+", networkId: "+wifiConfiguration.networkId);
                if(!wifiConfiguration.SSID.equals(tabletWifiSsid))
                    switch(wifiConfiguration.status) {
                        case WifiConfiguration.Status.CURRENT:
                            p(wifiConfiguration.SSID+": is current, try to diable.");
                            boolean ok=wifiManager.disableNetwork(wifiConfiguration.networkId);
                            if(ok)
                                p(wifiConfiguration.SSID+" was disabled.");
                            else
                                p(wifiConfiguration.SSID+" was not disabled!");
                            break;
                        case WifiConfiguration.Status.DISABLED:
                            p(wifiConfiguration.SSID+" is disabled.");
                            break;
                        case WifiConfiguration.Status.ENABLED:
                            if(true||!wifiConfiguration.SSID.equals("\"TRENDnet651\"")) {
                                p(wifiConfiguration.SSID+": is enabled, try to diable.");
                                ok=wifiManager.disableNetwork(wifiConfiguration.networkId);
                                if(ok)
                                    p(wifiConfiguration.SSID+" was disabled.");
                                else
                                    p(wifiConfiguration.SSID+" was not disabled!");
                            } else
                                p("ignoring: "+wifiConfiguration.SSID);
                            break;
                        default:
                            l.severe("unknown status: "+wifiConfiguration.status);
                    }
                else
                    switch(wifiConfiguration.status) {
                        case WifiConfiguration.Status.CURRENT:
                            p("current connection is: "+tabletWifiSsid);
                            break;
                        case WifiConfiguration.Status.DISABLED:
                            p(wifiConfiguration.SSID+" is disabled, try to enable");
                            p(wifiConfiguration.SSID+" disconnecting first.");
                            boolean ok=wifiManager.disconnect();
                            if(ok) {
                                p("says we disconnected, try to enable.");
                                ok=wifiManager.enableNetwork(wifiConfiguration.networkId,true);
                                if(ok) {
                                    p(wifiConfiguration.SSID+" was enabled, trying to connect.");
                                    ok=wifiManager.reconnect();
                                    if(ok) {
                                        p(wifiConfiguration.SSID+" says is connected.");
                                        if((inetAddress=getIpAddressFromWifiManager())!=null)
                                            p("inetAddress: "+inetAddress);
                                        else
                                            p("inetAddress is null!");
                                    } else
                                        p(wifiConfiguration.SSID+" says was not connected!");
                                } else
                                    p(wifiConfiguration.SSID+" was not enabled!");
                            } else
                                p("says disconnect failed!"); break;
                        case WifiConfiguration.Status.ENABLED:
                            p(wifiConfiguration.SSID+" is enabled, trying to connect.");
                            p(wifiConfiguration.SSID+" disconnecting first.");
                            ok=wifiManager.disconnect();
                            if(ok) {
                                p("says we disconnected, try to reconnect.");
                                ok=wifiManager.reconnect();
                                if(ok) {
                                    p(wifiConfiguration.SSID+" says is connected.");
                                    if((inetAddress=getIpAddressFromWifiManager())!=null)
                                        p("inetAddress: "+inetAddress);
                                    else
                                        p("inetAddress is null!");
                                } else
                                    p(wifiConfiguration.SSID+" says was not connected!");
                            } else
                                p("says disconnect failed!"); break;
                        default:
                            l.severe("unknown status: "+wifiConfiguration.status);
                    }
            }
    }
    InetAddress getIpAddressFromWifiManager() {
        WifiManager wifiMan=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
        p("isWifiEnabled() returns: "+wifiMan.isWifiEnabled());
        WifiInfo wifiInf=wifiMan.getConnectionInfo();
        p("wifi info: "+wifiInf);
        int ipAddress=wifiInf.getIpAddress();
        String ipAddressString=null;
        if(ipAddress!=0) {
            // if(ipAddress.isL)
            p("wifi ip address string: "+ipAddress);
            if(ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
                ipAddress=Integer.reverseBytes(ipAddress);
            p("ipAddress string: "+Integer.toHexString(ipAddress));
            byte[] ipByteArray=BigInteger.valueOf(ipAddress).toByteArray();
            try {
                ipAddressString=InetAddress.getByAddress(ipByteArray).getHostAddress();
            } catch(UnknownHostException ex) {
                l.warning("Unable to get host address for: "+Integer.toHexString(ipAddress));
            }
        }
        InetAddress inetAddress=null;
        try {
            inetAddress=InetAddress.getByName(ipAddressString);
        } catch(UnknownHostException e) {
        }
        return inetAddress;
    }
    void setupToast() {
        ((Toaster.Android_)Toaster.toaster).setCallback(new IO.Callback<String>() {
            @Override
            public void call(final String string) {
                if(false)
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity,string,Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    ; //p("toast was: "+string);
            }
        });
    }
    final MainActivity mainActivity; // make this just activity!
    boolean gotWifiUpOrFail;
}
