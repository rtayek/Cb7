package com.tayek.tablet.gui.android.cb7;
import android.app.*;
import android.content.*;
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

import static com.tayek.io.IO.l;
import static com.tayek.io.IO.p;
class Gui implements Observer, View.OnClickListener, Tablet.HasATablet{
    Gui(MainActivity mainActivity,Group group,MessageReceiver.Model model) {
        this.mainActivity=mainActivity;
        this.et=mainActivity.et;
        this.group=group;
        this.model=model;
        colors=model.colors;
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
        button.setId(rows*columns+i); // id is index!
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
        for(int i=0;i<group.keys().size();i++) {
            double x=x0+i*1.2*size/3;
            double y=y0+(3-.5)*size*1.2;
            button=getButton(size/3,""+Integer.valueOf(i+1),fontsize,rows,columns,i,(int)x,(int)y);
            status[i]=button;
            relativeLayout.addView(button);
        }
        int i=group.keys().size();
        double xs=x0;
        double ys=y0+2*size*1.2;
        lineStatus=getButton(6*size,size/3,"w",fontsize,rows,columns,i,(int)xs,(int)ys);
        relativeLayout.addView(lineStatus);
        //yr+=1.2*size/3;

        double xr=x0+i*1.2*size/3;
        double yr=y0+(3-.5)*size*1.2;
        // set layout params?
        // this is done ny getButton()
        wifiStatus=getButton(size/3,"w",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(wifiStatus);
        xr+=1.2*size/3;
        routerStatus=getButton(size/3,"r",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(routerStatus);
        xr=x0+(columns+1.2)*size;
        singleStatus=getButton(size/3,"s",fontsize,rows,columns,i,(int)xr,(int)yr);
        relativeLayout.addView(singleStatus);
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
                    if(1<=id&&id<=model.buttons)
                        tablet.click(index+1);
                    else { // some other button
                        Histories histories=histories(index);
                        Toast.makeText(mainActivity,""+histories,Toast.LENGTH_LONG).show();
                    }
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
        wifiStatus.setVisibility(visibility);
        routerStatus.setVisibility(visibility);
    }
    Histories histories(int index) {
        Histories histories=null;
        if(tablet!=null) {
            int tabletIndex=index-tablet.model().colors.rows*tablet.model().colors.columns;
            if(0<=tabletIndex&&tabletIndex<group.keys().size()) {
                String tabletId=IO.aTabletId(tabletIndex+1);
                histories=group.required(tabletId).histories();
            } else {
                l.severe(index+" is bad index!");
            }
        }
        return histories;
    }

    public void onClick(final View v) {
        p("click &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        if(v instanceof Button) {
            Button button=(Button)v;
            int index=button.getId();
            if(guiAdapterABC!=null)
                guiAdapterABC.processClick(index);
            else
                l.severe("gui adapter is null!");
        } else
            l.severe("not a button!");
        if(lastClick!=Double.NaN)
            p((et.etms()-lastClick)+" between clicks.");
        lastClick=et.etms();
        if(tablet!=null) {
            Iterator<String> s=group.keys().iterator();
            p("tablets: "+group.keys());
            for(int i=0;i<group.keys().size();i++) {
                Histories histories=group.required(s.next()).histories();
                p("histories: "+histories);
                double recentFaulureRate=histories.senderHistory.history.recentFailureRate();
                int color=histories.senderHistory.history.attempts()==0?colors.aColor(Colors.yellow):colors.aColor(colors.smooth(recentFaulureRate));
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
                    alert("Quitting",false);
                    mainActivity.finish();
                    l.severe("after finish! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                    try {
                        Thread.sleep(5_000);
                    } catch(InterruptedException e) {
                        l.info("caught: "+e);
                    }
                    //System.exit(0);
                } else {
                    if(Enums.MenuItem.values()[id].equals(Enums.MenuItem.toggleExtraStatus))
                        setStatusVisibility(status[0].getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
                    else
                        Enums.MenuItem.doItem(id,tablet);
                    return true;
                }
            else if(id==Enums.MenuItem.values().length) { // some hack for restarting tablet?
                Intent i=mainActivity.getBaseContext().getPackageManager().getLaunchIntentForPackage(mainActivity.getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainActivity.startActivity(i);
            } else if(Enums.LevelSubMenuItem.isItem(id-Enums.MenuItem.values().length)) {
                Enums.LevelSubMenuItem.doItem(id-Enums.MenuItem.values().length); // hack!
                return true;
            } else
                l.severe(item+" is not atablet men item!");
        } catch(Exception e) {
            l.severe("menut item: "+item+", caught: "+e);
        }
        return false;
    }
    final MainActivity mainActivity;
    final Et et;
    final Group group;
    final MessageReceiver.Model model;
    final Colors colors;
    TextView bottom; // was used for messages, put it back
    Button[] buttons, status, test;
    Button lineStatus, wifiStatus, routerStatus,singleStatus;
    Double lastClick=Double.NaN;
    ExecutorService executorService=Executors.newFixedThreadPool(10);
    GuiAdapter.GuiAdapterABC guiAdapterABC;
    Tablet tablet;
}
