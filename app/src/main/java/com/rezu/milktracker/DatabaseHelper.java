package com.rezu.milktracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) { super(context, "BabyTrackerDB", null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE milk (id INTEGER PRIMARY KEY, amount INTEGER, time LONG)");
        db.execSQL("CREATE TABLE diapers (id INTEGER PRIMARY KEY, time LONG)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}
    public void addMilkEntry(int amt, long t) {
        ContentValues v = new ContentValues(); v.put("amount", amt); v.put("time", t);
        getWritableDatabase().insert("milk", null, v);
    }
    public void addDiaperEntry(long t) {
        ContentValues v = new ContentValues(); v.put("time", t);
        getWritableDatabase().insert("diapers", null, v);
    }
    public ArrayList<Entry> getAllMilkEntries() { return fetch("milk"); }
    public ArrayList<Entry> getAllDiaperEntries() { return fetch("diapers"); }
    private ArrayList<Entry> fetch(String table) {
        ArrayList<Entry> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + table + " ORDER BY time ASC", null);
        while (c.moveToNext()) {
            int val = table.equals("milk") ? c.getInt(1) : 1;
            long time = table.equals("milk") ? c.getLong(2) : c.getLong(1);
            list.add(new Entry(val, time));
        }
        c.close();
        return list;
    }
    public void clearMilkData() { getWritableDatabase().delete("milk", null, null); }
    public void clearDiaperData() { getWritableDatabase().delete("diapers", null, null); }
    static class Entry { int value; long timestamp; Entry(int v, long t) { value = v; timestamp = t; } }
}