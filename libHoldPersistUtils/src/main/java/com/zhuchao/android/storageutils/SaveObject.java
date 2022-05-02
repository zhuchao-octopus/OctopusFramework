package com.zhuchao.android.storageutils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SaveObject {


    public void fileSave2Local(Object obj) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            //通过openFileOutput方法得到一个输出流
            //fos = openFileOutput("obj.out", Context.MODE_WORLD_READABLE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj); //写入
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close(); //最后关闭输出流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
