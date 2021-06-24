package com.chenghui.lib.admodle;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 存储
 */
public class AdmodelSPUtls {

    private static SharedPreferences preferences;
    private static Editor editor;
    private static AdmodelSPUtls mySharedPreferences;

    private AdmodelSPUtls() {
    }

    public static AdmodelSPUtls getInstance(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences("admodel",
                    context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        if (mySharedPreferences == null) {
            mySharedPreferences = new AdmodelSPUtls();
        }
        return mySharedPreferences;
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void putLong(String key, Long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

}
