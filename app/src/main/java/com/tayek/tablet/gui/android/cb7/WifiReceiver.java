package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.util.*;
import static com.tayek.io.IO.*;
public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context,Intent intent) {
        mContext=context;
        p("wifi receiver got action: "+intent.getAction());
    }
    private Context mContext;
}
