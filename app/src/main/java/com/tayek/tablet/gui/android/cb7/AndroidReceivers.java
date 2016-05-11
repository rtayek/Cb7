package com.tayek.tablet.gui.android.cb7;
import android.content.*;
import static com.tayek.io.IO.p;
class AndroidReceivers {
    // http://stackoverflow.com/questions/29458927/classnotfoundexception-unable-to-instantiate-broadcastreceiver
    //  set multiDexEnabled = false
    static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
                if(startActivity) {
                    Intent activityIntent=new Intent(context,MainActivity.class);
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(activityIntent);
                }
        }
        boolean startActivity=true; // turn on when done with testing!
    }
    static class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            mContext=context;
            p("wifi receiver got action: "+intent.getAction());
        }
        private Context mContext;
    }
}