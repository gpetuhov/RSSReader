package com.gpetuhov.android.rssreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RSSReaderInstrumentedTest {

    // Keeps app context
    private Context mContext;

    @Before
    public void getAppContext() {
        // Get context of the app under test
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void useAppContext() throws Exception {
        assertEquals("com.gpetuhov.android.rssreader", mContext.getPackageName());
    }

    @Test
    public void firstRunFlag_isCorrect() {
        // Get SharedPreferences and instantiate UtilsPrefs with it
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        UtilsPrefs utilsPrefs = new UtilsPrefs(sharedPreferences);

        // Save flag initial value
        boolean firstRunFlagOldValue = utilsPrefs.isFirstRun();

        // Set flag to false
        utilsPrefs.setNotFirstRun();

        // Check if works properly
        assertEquals(false, utilsPrefs.isFirstRun());

        // Restore initial flag value
        utilsPrefs.setFirstRunFlagValue(firstRunFlagOldValue);
    }
}
