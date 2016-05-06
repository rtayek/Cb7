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
    void retry() {
        WifiManager wifiMan=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf;
        int ipAddress;
        List<WifiConfiguration> wifiConfigurations=wifiMan.getConfiguredNetworks();
        if(wifiConfigurations==null) {
            return;
        }
        l.info("wifi configurations (all): "+wifiConfigurations.size());
        for(WifiConfiguration wifiConfiguration : wifiConfigurations) {
            l.info("wifi configuration status is disabled: "+(wifiConfiguration.status==WifiConfiguration.Status.DISABLED));
            //l.info("wifi configuration toString(): "+wifiConfiguration.toString());
            if(wifiConfiguration.toString().contains(tabletRouterPrefix)) {
                //p("IP config: "+wifiConfiguration.getIpConfiguration());
                //l.info("found our network: "+wifiConfiguration.toString());
                int networkId=wifiConfiguration.networkId;
                l.info("found our network id: "+networkId);
                boolean ok=wifiMan.disconnect();
                l.info("enabling our network: "+networkId);
                // https://code.google.com/p/android-developer-preview/issues/detail?id=2218
                ok=wifiMan.enableNetwork(networkId,true);
                ok=wifiMan.reconnect();
                l.info("reconnect returns: "+ok);
                if(ok) {
                    wifiMan=(WifiManager)mainActivity.getSystemService(Context.WIFI_SERVICE);
                    wifiInf=wifiMan.getConnectionInfo();
                    p("wifi inf: "+wifiInf);
                    ipAddress=wifiInf.getIpAddress();
                    l.info("wifi ip adress: "+ipAddress);
                }
                break;
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
                if(true)
                    retry();
            }
        } else
            retry();
        InetAddress inetAddress=null;
        try {
            inetAddress=InetAddress.getByName(ipAddressString);
        } catch(UnknownHostException e) {
        }
        return inetAddress;
    }
    public class IpAddress implements Callable<InetAddress> {
        @Override
        public InetAddress call() throws Exception {
            System.out.println("calling: getIpAddressFromWifiManager()");
            InetAddress inetAddress=getIpAddressFromWifiManager();
            System.out.println("getIpAddressFromWifiManager() returns: "+inetAddress);
            return inetAddress;
        }
    }
    public class IpAddressCallable implements Runnable {
        @Override
        public void run() {
            int n=20;
            for(int i=1;i<=n;i++) {
                System.out.println("ipAddressCallable: i="+i);
                Future<InetAddress> future=mainActivity.runner.gui.executorService.submit(new IpAddress());
                while(!future.isDone())
                    try {
                        Thread.sleep(200);
                    } catch(InterruptedException e) {
                        System.out.println("1 caught: "+e);
                    }
                try {
                    InetAddress inetAddress=future.get();
                    if(inetAddress!=null) {
                        System.out.println("future.get() returns: "+inetAddress);
                        //Toast.makeText(MainActivity.this,"wifi ip address is: "+ipAddress,Toast.LENGTH_LONG).show(); // throws: Can't create handler inside thread that has not called Looper.prepare()
                        gotWifiUpOrFail=true;
                        break;
                    }
                } catch(InterruptedException e) {
                    System.out.println("2 caught: "+e);
                } catch(ExecutionException e) {
                    System.out.println("3 caught: "+e);
                }
                try {
                    Thread.sleep(1_000);
                } catch(InterruptedException e) {
                    System.out.println("4 caught: "+e);
                }
                System.out.println("---------------------------------");
            }
            gotWifiUpOrFail=true;
        }
    }
    void setupAudioPlayer() {
        ((Audio.Factory.FactoryImpl.AndroidAudio)Audio.audio).setCallback(new IO.Callback<Audio.Sound>() {
            @Override
            public void call(Audio.Sound sound) {
                Integer id=id(sound);
                l.info("playing sound: "+sound+", id: "+id);
                if(id!=null) {
                    MediaPlayer mediaPlayer=MediaPlayer.create(mainActivity,id);
                    mediaPlayer.start();
                } else
                    l.warning("id for sound: "+sound+" is null!");
            }
            Integer id(Audio.Sound sound) {
                switch(sound) {
                    case electronic_chime_kevangc_495939803:
                        return R.raw.electronic_chime_kevangc_495939803;
                    case glass_ping_go445_1207030150:
                        return R.raw.glass_ping_go445_1207030150;
                    case store_door_chime_mike_koenig_570742973:
                        return R.raw.store_door_chime_mike_koenig_570742973;
                    default:
                        l.warning(""+" "+"default where!");
                        return null;
                }
            }
        });
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
    void waitForWifi() {
        p("waiting for wifi.");
        new Thread(new IpAddressCallable()).start();
        int n=10;
        for(int i=1;i<=n&&!gotWifiUpOrFail;i++)
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println("5 caught: "+e);
            }
    }

    final MainActivity mainActivity; // make this just activity!
    boolean gotWifiUpOrFail;
}
