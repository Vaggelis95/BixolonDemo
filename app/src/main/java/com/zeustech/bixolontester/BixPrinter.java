package com.zeustech.bixolontester;

import android.content.Context;

import com.zeustech.bixolontester.PrinterControl.BixolonPrinter;

import androidx.annotation.NonNull;

class BixPrinter {

    private static volatile BixPrinter INSTANCE = null;
    private final BixolonPrinter bxlPrinter;

    static synchronized BixPrinter getInstance(@NonNull Context context) {
        if(INSTANCE == null) {
            try {
                INSTANCE = new BixPrinter(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }

    private BixPrinter(@NonNull Context context) {
        bxlPrinter = new BixolonPrinter(context.getApplicationContext());
    }

    BixolonPrinter getBxlPrinter() {
        return bxlPrinter;
    }
}
