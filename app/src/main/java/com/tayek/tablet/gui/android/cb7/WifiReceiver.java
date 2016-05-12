package com.tayek.tablet.gui.android.cb7;
import android.content.*;

import static com.tayek.io.IO.p;
public class WifiReceiver extends BroadcastReceiver {
    public WifiReceiver() {
        super();
    }
    @Override
    public void onReceive(Context context,Intent intent) {
        mContext=context;
        p("wifi receiver got action: "+intent.getAction());
    }
    private Context mContext;
}