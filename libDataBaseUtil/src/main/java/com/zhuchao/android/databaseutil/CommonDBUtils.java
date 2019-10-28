package com.zhuchao.android.databaseutil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class CommonDBUtils {
    private static CommonDBUtils dbUtils;
    private SQLiteDatabase db;
    private String DefaultDatabaseName;
    private String DefaultTableName;

    /**
     * 单例模式
     */
    public static CommonDBUtils getInstance() {
        if (dbUtils == null) {
            dbUtils = new CommonDBUtils();
            return dbUtils;
        }
        return dbUtils;
    }

    /**
     * 创建数据表
     *
     * @param contenxt 上下文对象
     */
    public void creadDatabase(Context contenxt, String databaseName, String tableName) {
        DefaultDatabaseName = databaseName;
        DefaultTableName = tableName;

        if (DefaultDatabaseName == null)
            DefaultDatabaseName = "CommonDB.db";
        if (DefaultTableName == null)
            DefaultTableName = "MyTable";

        String path = contenxt.getCacheDir().getPath() + "/" + DefaultDatabaseName;

        db = SQLiteDatabase.openOrCreateDatabase(path, null);

        String sql = "create table if not exists " + DefaultTableName +
                "(id integer primary key autoincrement," +
                "name text(50),p1 text(50),p2 text(50)," +
                "p3 text(50),p4 text(50),p5 text(50),p6 text(50)," +
                "p7 text(50),p8 text(50),p9 text(50),p10 text(50),p11 text(50),p12 text(50),p13 text(50),p14 text(50))";

        db.execSQL(sql);//创建表
    }

    /**
     * 查询数据
     * 返回List
     */
    public ArrayList<String> selectAllData(String tableName) {
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String p1 = cursor.getString(cursor.getColumnIndex("p1"));
            String p2 = cursor.getString(cursor.getColumnIndex("p2"));
            String p3 = cursor.getString(cursor.getColumnIndex("p3"));
            String p4 = cursor.getString(cursor.getColumnIndex("p4"));
            String p5 = cursor.getString(cursor.getColumnIndex("p5"));
            String p6 = cursor.getString(cursor.getColumnIndex("p6"));
            String p7 = cursor.getString(cursor.getColumnIndex("p7"));
            String p8 = cursor.getString(cursor.getColumnIndex("p8"));
            String p9 = cursor.getString(cursor.getColumnIndex("p9"));
            String p10 = cursor.getString(cursor.getColumnIndex("p10"));
            String p11 = cursor.getString(cursor.getColumnIndex("p11"));
            String p12 = cursor.getString(cursor.getColumnIndex("p12"));
            String p13 = cursor.getString(cursor.getColumnIndex("p13"));
            String p14 = cursor.getString(cursor.getColumnIndex("p14"));

            list.add(String.valueOf(id) +','+name + ',' + p1 + ',' + p2 + ',' + p3 + ',' + p4 + ',' + p5 + ',' + p6 + ',' + p7 + ',' + p8 + ',' + p9 + ',' + p10 + ',' + p11 + ',' + p12 + ',' + p13 + ',' + p14);
            //Log.d("--Main--", "selectis=========" + id + "==" + name + "==" + mon + "==" + address + "==" + number);
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public ArrayList<String> selectData(String tableName, String name) {
        ArrayList<String> list = new ArrayList<String>();
        //Cursor cursor = db.query("CommonDB", null, null, null, null, null, null);
        Cursor cursor = db.query(tableName, null, "name = ?", new String[]{name}, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String p1 = cursor.getString(cursor.getColumnIndex("p1"));
            String p2 = cursor.getString(cursor.getColumnIndex("p2"));
            String p3 = cursor.getString(cursor.getColumnIndex("p3"));
            String p4 = cursor.getString(cursor.getColumnIndex("p4"));
            String p5 = cursor.getString(cursor.getColumnIndex("p5"));
            String p6 = cursor.getString(cursor.getColumnIndex("p6"));
            String p7 = cursor.getString(cursor.getColumnIndex("p7"));
            String p8 = cursor.getString(cursor.getColumnIndex("p8"));
            String p9 = cursor.getString(cursor.getColumnIndex("p9"));
            String p10 = cursor.getString(cursor.getColumnIndex("p10"));
            String p11 = cursor.getString(cursor.getColumnIndex("p11"));
            String p12 = cursor.getString(cursor.getColumnIndex("p12"));
            String p13 = cursor.getString(cursor.getColumnIndex("p13"));
            String p14 = cursor.getString(cursor.getColumnIndex("p14"));

            list.add(String.valueOf(id)+','+name + ',' + p1 + ',' + p2 + ',' + p3 + ',' + p4 + ',' + p5 + ',' + p6 + ',' + p7 + ',' + p8 + ',' + p9 + ',' + p10 + ',' + p11 + ',' + p12 + ',' + p13 + ',' + p14);
            //Log.d("--Main--", "selectis=========" + id + "==" + name + "==" + mon + "==" + address + "==" + number);
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public ArrayList<String> selectData(String tableName, int Userid) {
        ArrayList<String> list = new ArrayList<String>();
        //Cursor cursor = db.query("CommonDB", null, null, null, null, null, null);
        Cursor cursor = db.query(tableName, null, "id = ?",  new String[]{String.valueOf(Userid)}, null, null, null);
        while (cursor.moveToNext()) {

            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String p1 = cursor.getString(cursor.getColumnIndex("p1"));
            String p2 = cursor.getString(cursor.getColumnIndex("p2"));
            String p3 = cursor.getString(cursor.getColumnIndex("p3"));
            String p4 = cursor.getString(cursor.getColumnIndex("p4"));
            String p5 = cursor.getString(cursor.getColumnIndex("p5"));
            String p6 = cursor.getString(cursor.getColumnIndex("p6"));
            String p7 = cursor.getString(cursor.getColumnIndex("p7"));
            String p8 = cursor.getString(cursor.getColumnIndex("p8"));
            String p9 = cursor.getString(cursor.getColumnIndex("p9"));
            String p10 = cursor.getString(cursor.getColumnIndex("p10"));
            String p11 = cursor.getString(cursor.getColumnIndex("p11"));
            String p12 = cursor.getString(cursor.getColumnIndex("p12"));
            String p13 = cursor.getString(cursor.getColumnIndex("p13"));
            String p14 = cursor.getString(cursor.getColumnIndex("p14"));

            list.add(String.valueOf(id)+','+name + ',' + p1 + ',' + p2 + ',' + p3 + ',' + p4 + ',' + p5 + ',' + p6 + ',' + p7 + ',' + p8 + ',' + p9 + ',' + p10 + ',' + p11 + ',' + p12 + ',' + p13 + ',' + p14);
            //Log.d("--Main--", "selectis=========" + id + "==" + name + "==" + mon + "==" + address + "==" + number);
        }

        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public ArrayList<String> selectDataNameP1(String tableName, String name,String p1) {
        ArrayList<String> list = new ArrayList<String>();
        //Cursor cursor = db.query("CommonDB", null, null, null, null, null, null);
        Cursor cursor = db.query(tableName, null,"name = ? and p1 = ?", new String[]{name,p1}, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            //String name = cursor.getString(cursor.getColumnIndex("name"));
            //String p1 = cursor.getString(cursor.getColumnIndex("p1"));
            String p2 = cursor.getString(cursor.getColumnIndex("p2"));
            String p3 = cursor.getString(cursor.getColumnIndex("p3"));
            String p4 = cursor.getString(cursor.getColumnIndex("p4"));
            String p5 = cursor.getString(cursor.getColumnIndex("p5"));
            String p6 = cursor.getString(cursor.getColumnIndex("p6"));
            String p7 = cursor.getString(cursor.getColumnIndex("p7"));
            String p8 = cursor.getString(cursor.getColumnIndex("p8"));
            String p9 = cursor.getString(cursor.getColumnIndex("p9"));
            String p10 = cursor.getString(cursor.getColumnIndex("p10"));
            String p11 = cursor.getString(cursor.getColumnIndex("p11"));
            String p12 = cursor.getString(cursor.getColumnIndex("p12"));
            String p13 = cursor.getString(cursor.getColumnIndex("p13"));
            String p14 = cursor.getString(cursor.getColumnIndex("p14"));

            list.add(String.valueOf(id)+','+name + ',' + p1 + ',' + p2 + ',' + p3 + ',' + p4 + ',' + p5 + ',' + p6 + ',' + p7 + ',' + p8 + ',' + p9 + ',' + p10 + ',' + p11 + ',' + p12 + ',' + p13 + ',' + p14);
            //Log.d("--Main--", "selectis=========" + id + "==" + name + "==" + mon + "==" + address + "==" + number);
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }


    /**
     * 根据ID删除数据
     * id 删除id
     */
    public int delData(String tableName, int id) {
        int inde = db.delete(tableName, "id = ?", new String[]{String.valueOf(id)});
        //Log.d("--Main--", "删除了==============" + inde);
        return inde;
    }

    public int delData(String tableName, String name) {
        int inde = db.delete(tableName, "name = ?", new String[]{name});
        Log.d("--Main--", "删除了==============" + inde);
        return inde;
    }
    public int delDataNameP1(String tableName, String name,String p1) {
        int inde = db.delete(tableName, "name = ? and p1 = ?", new String[]{name,p1});
        Log.d("--Main--", "删除了==============" + inde);
        return inde;
    }

    public int modifyData(int id ,String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("p1", p1);
        contentValues.put("p2", p2);
        contentValues.put("p3", p3);
        contentValues.put("p4", p4);
        contentValues.put("p5", p5);
        contentValues.put("p6", p6);
        contentValues.put("p7", p7);
        contentValues.put("p8", p8);
        contentValues.put("p9", p9);
        contentValues.put("p10", p10);
        contentValues.put("p11", p11);
        contentValues.put("p12", p12);
        contentValues.put("p13", p13);
        contentValues.put("p14", p14);
        int index = db.update(tableName, contentValues, "id = ?", new String[]{String.valueOf(id)});
        //Log.e("--Main--", "修改了===============" + index);
        return index;
    }

    public int modifyData(String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        ContentValues contentValues = new ContentValues();
        //contentValues.put("name", name);
        contentValues.put("p1", p1);
        contentValues.put("p2", p2);
        contentValues.put("p3", p3);
        contentValues.put("p4", p4);
        contentValues.put("p5", p5);
        contentValues.put("p6", p6);
        contentValues.put("p7", p7);
        contentValues.put("p8", p8);
        contentValues.put("p9", p9);
        contentValues.put("p10", p10);
        contentValues.put("p11", p11);
        contentValues.put("p12", p12);
        contentValues.put("p13", p13);
        contentValues.put("p14", p14);
        int index = db.update(tableName, contentValues, "name = ?", new String[]{name});
        //Log.e("--Main--", "修改了===============" + index);
        return index;
    }
    public int modifyDataByNameP1(String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        ContentValues contentValues = new ContentValues();
        //contentValues.put("name", name);
        //contentValues.put("p1", p1);
        contentValues.put("p2", p2);
        contentValues.put("p3", p3);
        contentValues.put("p4", p4);
        contentValues.put("p5", p5);
        contentValues.put("p6", p6);
        contentValues.put("p7", p7);
        contentValues.put("p8", p8);
        contentValues.put("p9", p9);
        contentValues.put("p10", p10);
        contentValues.put("p11", p11);
        contentValues.put("p12", p12);
        contentValues.put("p13", p13);
        contentValues.put("p14", p14);
        int index = db.update(tableName, contentValues, "name = ? and p1 = ?", new String[]{name,p1});
        //Log.e("--Main--", "修改了===============" + index);
        return index;
    }
    /**
     * 添加数据
     * bsid 添加的数据ID
     * name 添加数据名称
     */
    public long insertData(String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("p1", p1);
        contentValues.put("p2", p2);
        contentValues.put("p3", p3);
        contentValues.put("p4", p4);
        contentValues.put("p5", p5);
        contentValues.put("p6", p6);
        contentValues.put("p7", p7);
        contentValues.put("p8", p8);
        contentValues.put("p9", p9);
        contentValues.put("p10", p10);
        contentValues.put("p11", p11);
        contentValues.put("p12", p12);
        contentValues.put("p13", p13);
        contentValues.put("p14", p14);
        long dataSize = db.insert(tableName, null, contentValues);

        Log.d("--insertData--", "insertData====" + name);
        return dataSize;
    }

    public void updateData(String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        if (existsData(tableName, name))
            modifyData(tableName, name, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14);
        else
            insertData(tableName, name, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14);
    }

    public void updateData2(String tableName, String name, String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11, String p12, String p13, String p14) {
        if (existsDataNameP1(tableName, name,p1))
            modifyDataByNameP1(tableName, name, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14);
        else
            insertData(tableName, name, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14);
    }

     /*
     * 查询名字单个数据
     * @param name
     * @return
     */
    public boolean existsData(String tableName, String name)
    {
        //查询数据库
        Cursor cursor = db.query(tableName, null, "name = ?", new String[]{name}, null, null, null);
        while (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public boolean existsDataNameP1(String tableName, String name,String p1)
    {
        //查询数据库
        Cursor cursor = db.query(tableName, null, "name = ? and p1 = ?", new String[]{name,p1}, null, null, null);
        while (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

}
