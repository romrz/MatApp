package com.rom.matapp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

import com.rom.matapp.R;

/**
 * Created by rom on 30/08/14.
 */
public class MyNumberPicker extends NumberPicker {

    public MyNumberPicker(Context context) {
        this(context, null);
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttributes(attrs);
    }

    public MyNumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttributes(attrs);
    }

    private void processAttributes(AttributeSet attrs) {

        setMinValue(attrs.getAttributeIntValue(null, "minValue", 1));
        setMaxValue(attrs.getAttributeIntValue(null, "maxValue", 1));

    }

}
