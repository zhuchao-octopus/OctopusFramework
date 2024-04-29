package com.zhuchao.android.car;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.zhuchao.android.fbase.ByteUtils;

import java.util.ArrayList;

public class PEventCourier implements Parcelable {
    private String from;
    private String target;
    private int id;
    private byte[] datas;
    private ArrayList<String> mStrings = new ArrayList<>();

    public PEventCourier(int id, int value) {
        this.from = null;
        this.target = null;
        this.id = id;
        this.datas = ByteUtils.intToBytes(value);
    }

    public PEventCourier(int id, byte[] datas) {
        this.from = null;
        this.target = null;
        this.id = id;
        this.datas = datas;
    }

    public PEventCourier(int id, ArrayList<String> strings) {
        this.from = null;
        this.target = null;
        this.id = id;
        this.mStrings.clear();
        this.mStrings.addAll(strings);
    }

    protected PEventCourier(Parcel in) {
        id = in.readInt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            datas = in.readBlob();
        } else {
            byte[] bytes = new byte[in.readInt()];
            in.readByteArray(bytes);
            this.datas = bytes;
        }
        mStrings = in.createStringArrayList();
        from = in.readString();
        target = in.readString();
    }

    public static final Creator<PEventCourier> CREATOR = new Creator<PEventCourier>() {
        @Override
        public PEventCourier createFromParcel(Parcel in) {
            return new PEventCourier(in);
        }

        @Override
        public PEventCourier[] newArray(int size) {
            return new PEventCourier[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            dest.writeBlob(datas);
        } else {
            dest.writeInt(datas.length);
            dest.writeByteArray(datas);
        }

        if (mStrings != null) dest.writeStringList(mStrings);
        if (from != null) dest.writeString(from);
        if (target != null) dest.writeString(target);
    }

    private String toHexStr(Byte inByte) {
        return String.format("%02x", inByte).toUpperCase();
    }

    private String datasToHexStr() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("datas=[");
        if (this.datas != null) {
            for (byte valueOf : datas) {
                //strBuilder.append(toHexStr(Byte.valueOf(valueOf)));
                strBuilder.append(toHexStr(valueOf));
                strBuilder.append(" ");
            }
        }
        strBuilder.append("]");
        return strBuilder.toString();
    }

    public String toStr() {
        if (from != null && mStrings != null)
            return "EventCourier{" + "id=" + id + ",datas=" + datasToHexStr() + ",strings=" + mStrings.toString() + ",fromClass='" + from + '\'' + ",target='" + target + '\'' + '}';
        else if (mStrings != null)
            return "EventCourier{" + "id=" + id + ",datas=" + datasToHexStr() + ",strings=" + mStrings.toString() + ",fromClass='" + from + '\'' + ",target='" + target + '\'' + '}';
        else return "EventCourier{" + "id=" + id + ",datas=" + datasToHexStr() + ",fromClass='" + '\'' + ",target='" + target + '\'' + '}';
    }
}
