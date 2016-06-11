package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.utilities.*;

import java.util.*;
import java.util.concurrent.*;

import static com.tayek.io.IO.*;
class Gui implements Observer, View.OnClickListener, Tablet.HasATablet {
    Gui(MainActivity mainActivity,Group group,MessageReceiver.Model model) {
        this.mainActivity=mainActivity;
        this.et=mainActivity.et;
        this.group=group;
        this.model=model;
        colors=model.colors;
        areWeQuitting=false;
    }
    @Override
    public void update(Observable o,Object hint) {
        if(model.equals(o))
            guiAdapterABC.update(o,hint);
        else
            System.out.println("not our model: "+o);
    }
    @Override
    public void setTablet(Tablet tablet) {
        this.tablet=tablet;
    }
    @Override
    public Tablet tablet() {
        return tablet;
    }
    @Override
    public void setStatusText(final String text) {
        p("set status: "+text);
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lineStatus.setText(text);
            }
        });
    }
    void alert(String string,boolean cancelable) {
        AlertDialog.Builder alert=new AlertDialog.Builder(mainActivity);
        alert.setTitle(string);
        alert.setMessage(string);
        alert.setCancelable(cancelable);
        alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int whichButton) {
                //Your action here
            }
        });
        l.info("showing alert.");
        alert.show();
    }
    private Button getButton(int size,String string,float fontsize,int rows,int columns,int i,int x,int y) {
        return getButton(size,size,string,fontsize,rows,columns,i,x,y);
    }
    private Button getButton(int width,int depth,String string,float fontsize,int rows,int columns,int i,int x,int y) {
        Button button;
        RelativeLayout.LayoutParams params;
        button=new Button(mainActivity);
        button.setId(model.buttons+i); // id is index!
        button.setTextSize(fontsize/4);
        button.setGravity(Gravity.CENTER);
        params=new RelativeLayout.LayoutParams(width,depth);
        params.leftMargin=x;
        params.topMargin=y;
        //p("other: "+i+", left margin="+params.leftMargin+", top margin="+params.topMargin);
        button.setLayoutParams(params);
        button.setText(string);
        button.setBackgroundColor(colors.aColor(colors.whiteOn));
        button.setOnClickListener(mainActivity);
        return button;
    }
    RelativeLayout builGui() {
        DisplayMetrics metrics=new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        System.out.println(metrics);
        //final int size=225;
        final int size=metrics.widthPixels/8; // size of a large square button
        final float fontsize=size*.30f;
        System.out.println(size+" "+(metrics.widthPixels*1./7));
        Point point = new Point();
        mainActivity.getWindowManager().getDefaultDisplay().getRealSize(point);
        p("real size is: "+point);
        //final int x0=size/4, y0=75;
        final int x0=size/4, y0=size/4;
        p("x0="+x0+", y0="+y0);
        RelativeLayout relativeLayout=new RelativeLayout(mainActivity);
        RelativeLayout.LayoutParams params=null;
        final int rows=colors.rows;
        final int columns=colors.columns;
        buttons=new Button[colors.n]; // colors is intimatley tied to the mark 1 model!
        for(int i=0;i<rows*columns;i++) {
            Button button=new Button(mainActivity);
            button.setId(i); // id is index!
            button.setTextSize(fontsize);
            button.setGravity(Gravity.CENTER);
            params=new RelativeLayout.LayoutParams(size,size);
            params.leftMargin=(int)(x0+i%columns*1.2*size);
            params.topMargin=(int)(y0+i/columns*size*1.2);
            //p("button: "+i+", left margin="+params.leftMargin+", top margin="+params.topMargin);
            button.setLayoutParams(params);
            if(i/columns%2==0)
                button.setText(""+(char)('0'+(i+1)));
            button.setBackgroundColor(colors.aColor(i,false));
            button.setOnClickListener(mainActivity);
            buttons[i]=button;
            relativeLayout.addView(button);
        }
        Button button=new Button(mainActivity);
        button.setId(rows*columns); // id is index!
        button.setTextSize(fontsize);
        button.setGravity(Gravity.CENTER);
        params=new RelativeLayout.LayoutParams(size,size);
        params.leftMargin=(int)(x0+(columns+1.2)*size);
        params.topMargin=y0;
        p("reset: left margin="+params.leftMargin+", top margin="+params.topMargin);
        button.setLayoutParams(params);
        button.setText("R");
        button.setBackgroundColor(colors.aColor(rows*columns,false));
        button.setOnClickListener(mainActivity);
        buttons[rows*columns]=button;
        relativeLayout.addView(button);
        status=new Button[group.keys().size()];
        int i=0;
        for(String key : group.keys()) {
            double x=x0+i*1.2*size/3;
            double y=y0+(3-.5)*size*1.2;
            button=getButton(size/3,""+Integer.valueOf(i+1),fontsize,rows,columns,i,(int)x,(int)y);
            status[i]=button;
            String tabletId=group.required(key).id;
            String shortId=group.required(key).shortId();
            indexToTabletId.put(i,tabletId);
            relativeLayout.addView(button);
            i++;
        }
        i=group.keys().size(); // for locating the wifi and router status buttons
        double xs=x0;
        double ys=y0+2*size*1.2;
        lineStatus=getButton(6*size,size/3,"w",fontsize,rows,columns,i,(int)xs,(int)ys);
        relativeLayout.addView(lineStatus);
        //yr+=1.2*size/3;
        double xr=x0+i*1.2*size/3;
        double yr=y0+(3-.5)*size*1.2;
        // set layout params?
        // this is done ny getButton()
        serverStatus=getButton(size/3,"t",fontsize,rows,columns,i,(int)xr,(int)yr);
        // should make i unique, but not so important sice we normally ignore a click on status buttons.
        relativeLayout.addView(serverStatus);
        xr+=1.2*size/3;
        wifiStatus=getButton(size/3,"w",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(wifiStatus);
        xr+=1.2*size/3;
        routerStatus=getButton(size/3,"r",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(routerStatus);
        xr+=1.2*size/3;
        singleStatus=getButton(size/3,"s",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(singleStatus);
        xr+=1.2*size/3;
        xr=metrics.widthPixels-size/3+10;
        yr=metrics.heightPixels-size/3+10-size;
        hidden=getButton(size/3," ",fontsize,rows,columns,hiddenButonIndex-/*hack*/model.buttons,(int)xr,(int)yr);
        hidden.setBackgroundColor(colors.background|0xff000000);
        p("hidden button is at: "+xr+", "+yr);
        relativeLayout.addView(hidden);
        if(false) {
            int testButtons=11;
            test=new Button[testButtons];
            for(i=0;i<testButtons;i++) {
                double x=x0+i*1.2*size/3;
                double y=y0+(3-.5)*size*1.2;
                y-=size*1.2/3;
                int r=(int)(i*.1*100);
                if(r>=100)
                    r=99;
                else if(r<0)
                    r=0;
                String text=""+r;
                while(text.length()<2)
                    text+='0';
                button=getButton(size/3,text,(float)(fontsize*.6),rows,columns,i,(int)x,(int)y);
                p("setting backgroud to: "+Colors.toString(Colors.smooth(i*.1)));
                button.setBackgroundColor(Colors.aColor(Colors.smooth(i*.1)));
                test[i]=button;
                relativeLayout.addView(button);
            }
        }
        relativeLayout.setBackgroundColor(colors.background|0xff000000);
        return relativeLayout;
    }
    GuiAdapter.GuiAdapterABC buildGuiAdapter() {
        GuiAdapter.GuiAdapterABC guiAdapterABC=new GuiAdapter.GuiAdapterABC(model) { // move this out of runner
            @Override
            public void setStatusText(String text) {
            }
            @Override
            public void processClick(int index) {
                int id=index+1;
                if(tablet!=null)
                    if(1<=id&&id<=model.buttons) {
                        p("model button, passing click to tablet: "+model);
                        p("models: "+model+", "+tablet.model()+", "+(model.equals(tablet.model())));
                        tablet.click(index+1);
                    } else { // some other button
                        p("not a model button.");
                        //Histories histories=histories(index); // removed old, use new if anything
                        //Toast.makeText(mainActivity,""+histories,Toast.LENGTH_LONG).show();
                    }
                else
                    p("tablet is null in gui adapter.");
            }
            @Override
            public void setButtonText(final int id,final String string) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttons[id-1].setText(string);
                    }
                });
            }
            @Override
            public void setButtonState(final int id,final boolean state) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttons[id-1].setBackgroundColor(colors.aColor(id-1,state));
                    }
                });
            }
        };
        return guiAdapterABC;
    }
    void setStatusVisibility(int visibility) {
        for(int i=0;i<group.keys().size();i++)
            status[i].setVisibility(visibility);
        lineStatus.setVisibility(visibility);
        serverStatus.setVisibility(visibility);
        wifiStatus.setVisibility(visibility);
        routerStatus.setVisibility(visibility);
    }
    public void onClick(final View v) {
        p("click on: "+v.getId());
        if(v instanceof Button) {
            Button button=(Button)v;
            Integer index=button.getId();
            p("index: "+index);
            Integer id=index+1;
            p("id: "+id);
            if(1<=id&&id<=model.buttons) {
                p("it's a model button.");
                if(guiAdapterABC!=null)
                    guiAdapterABC.processClick(index);
                else
                    p("guiAdapterABC is null!");
            } else if(index.equals(hiddenButonIndex)) {
                p("we clickon on the hiddem button.");
                mainActivity.openOptionsMenu();
            } else {
                p("map:"+indexToTabletId);
                Integer key=index-model.buttons;
                p("key: "+key);
                String tabletId=indexToTabletId.get(key);
                if(tabletId!=null) {
                    Histories histories=group.required(tabletId).histories();
                    p("got histories for: "+tabletId);
                    // p("histories for: "+tabletId+": "+histories);
                    if(tablet!=null&&tablet.tabletId().equals(tabletId)) {
                        p("this is our history");
                        if(histories.receiverHistory.history.attempts()==0)
                            l.severe("lost receiver history!");
                        else
                            l.info("we have receiver history.");
                    }
                    Toast.makeText(mainActivity,"histories for: "+tabletId+": "+histories,Toast.LENGTH_LONG).show();
                } else
                    p("no entry for key: "+key+" in: "+indexToTabletId);
            }
        } else
            l.severe("not a button!");
        if(lastClick!=Double.NaN)
            p((et.etms()-lastClick)+" between clicks.");
        lastClick=et.etms();
        if(tablet!=null) {
            Iterator<String> s=group.keys().iterator();
            for(int i=0;i<group.keys().size();i++) {
                Histories histories=group.required(s.next()).histories();
                if(false)
                    p("histories: "+histories.toString("after click"));
                double recentFaulureRate=histories.senderHistory.history.recentFailureRate();
                int color=histories.senderHistory.history.attempts()==0?colors.aColor(Colors.yellow):colors.aColor(colors.smooth(recentFaulureRate));
                if(false)
                    p("recent failure rate: "+recentFaulureRate+", color: "+Colors.toString(color));
                status[i].setBackgroundColor(color);
            }
        } else
            p("tablet is null!");
    }
    boolean menuItem(MenuItem item) {
        try {
            l.info("item: "+item);
            int id=item.getItemId();
            if(Enums.MenuItem.isItem(id))
                if(Enums.MenuItem.item(id).equals(Enums.MenuItem.Quit)) {
                    pl("quitting.");
                    //mainActivity.stopTabletStuff();
                    areWeQuitting=true;
                } else {
                    if(Enums.MenuItem.values()[id].equals(Enums.MenuItem.toggleExtraStatus))
                        setStatusVisibility(status[0].getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
                    else
                        Enums.MenuItem.doItem(id,tablet);
                    return true;
                }
            else if(Enums.LevelSubMenuItem.isItem(id-Enums.MenuItem.values().length)) {
                Enums.LevelSubMenuItem.doItem(id-Enums.MenuItem.values().length); // hack!
                return true;
            } else
                l.severe(item+" is not a tablet meun item!");
        } catch(Exception e) {
            l.severe("menut item: "+item+", caught: "+e);
        }
        return false;
        /*
        else if(id==Enums.MenuItem.values().length) { // some hack for restarting tablet?
            // wtf was i doing here?
            Intent i=mainActivity.getBaseContext().getPackageManager().getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mainActivity.startActivity(i);
        }
        */
    }
    void setupAudio() {
        ((Audio.Factory.FactoryImpl.AndroidAudio)Audio.audio).setCallback(new IO.Callback<Audio.Sound>() {
            @Override
            public void call(Audio.Sound sound) {
                Integer id=id(sound);
                l.info("playing sound: "+sound+", id: "+id);
                if(id!=null) {
                    mediaPlayer=MediaPlayer.create(mainActivity,id);
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
    final MainActivity mainActivity;
    final Et et;
    final Group group;
    final MessageReceiver.Model model;
    final Colors colors;
    volatile Boolean areWeQuitting;
    MediaPlayer mediaPlayer;
    TextView bottom; // was used for messages, put it back
    Button[] buttons, status, test;
    Button lineStatus, serverStatus, wifiStatus, routerStatus, singleStatus, hidden;
    Double lastClick=Double.NaN;
    final int hiddenButonIndex=1_000;
    GuiAdapter.GuiAdapterABC guiAdapterABC;
    final Map<Integer,String> indexToTabletId=new TreeMap<>();
    Tablet tablet;
}
