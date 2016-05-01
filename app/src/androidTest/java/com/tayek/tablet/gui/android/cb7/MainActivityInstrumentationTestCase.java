package com.tayek.tablet.gui.android.cb7;
import android.app.Application;
import android.support.test.runner.*;
import android.test.*;
import android.util.*;
import android.widget.*;

import static com.tayek.io.IO.*;
import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

import com.tayek.tablet.io.*;

import org.junit.*;
import org.junit.runner.*;
public class MainActivityInstrumentationTestCase extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityInstrumentationTestCase() {
        super(MainActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mainActivity = getActivity();
    }
    @Test
    public void testForSanity() {
        assertEquals(mainActivity.model.colors.n,mainActivity.buttons.length);
    }
    MainActivity mainActivity;
}
