package com.rom.matapp.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Romario on 18/07/2014.
 */
public class MyUtils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static final AtomicInteger sNextGeneratedName = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        if(Build.VERSION.SDK_INT < 17)
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        else
            return View.generateViewId();
    }

    /**
     * Hides the keyboard
     *
     * @param activity
     * @param v
     */
    public static void hideSoftKeyboard(Activity activity, View v) {

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

    public static String generateMatrixName() {

        for (;;) {
            final int result = sNextGeneratedName.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedName.compareAndSet(result, newValue)) {
                return "M" + result;
            }
        }

    }
}
