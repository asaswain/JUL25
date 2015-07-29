package edu.nyu.scps.asa.jul25;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class AssetHelper extends SQLiteAssetHelper {
    public AssetHelper(Context context, String filename) {
        super(context, filename, null, 1);	//1 is the database version number
    }
}

