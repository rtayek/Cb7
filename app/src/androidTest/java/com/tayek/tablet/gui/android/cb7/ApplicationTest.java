package com.tayek.tablet.gui.android.cb7;
import android.app.Application;
import android.support.test.runner.*;
import android.test.ApplicationTestCase;
import android.util.*;

import org.junit.*;
import org.junit.runner.*;
@RunWith(AndroidJUnit4.class) public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        //mainActivity=new MainActivity(this.getContext());
    }
    @Test
    public void test1() {
        Log.d("me","foo bar");
        System.out.println("foo bar");
    }
    MainActivity mainActivity;
}