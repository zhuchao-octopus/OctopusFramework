package com.zhuchao.android.serialport;

import com.zhuchao.android.fbase.ByteUtils;
import com.zhuchao.android.fbase.MMLog;

public class TProtocol_Package {
    private final String TAG = "TProtocol_Package";
    public final int DEFAULT_HEAD = 0x0101;
    public final int DEFAULT_END = 0x7E;
    private final int DEFAULT_MSGID_LENGTH = 2;
    private final int DEFAULT_MSGINDEX_LENGTH = 2;
    private final int DEFAULT_MSGLENGTH_LENGTH = 2;
    private final int DEFAULT_MSGCRC_LENGTH = 1;
    private final int DEFAULT_MSGEND_LENGTH = 1;

    private int msgHead = DEFAULT_HEAD;
    private int msgID;
    private int msgIndex;
    private int msgLength;
    private byte[] datas;
    private int msgCRC;
    private int msgEnd = DEFAULT_END;

    public TProtocol_Package() {
        //this.msgHead = DEFAULT_END;
        //this.msgEnd = DEFAULT_END;
        this.msgLength = 0;
    }

    public TProtocol_Package(int msgHead, int msg_End) {
        this.msgHead = msgHead;
        this.msgEnd = msg_End;
        this.msgLength = 0;
    }

    //public int getDEFAULT_HEAD() {
    //    return DEFAULT_HEAD;
    //}

    //public int getDEFAULT_END() {
    //   return DEFAULT_END;
    //}

    public int getMsgHead() {
        return msgHead;
    }

    public void setMsgHead(int msgHead) {
        this.msgHead = msgHead;
    }

    public int getMsgID() {
        return msgID;
    }

    public void setMsgID(int msgID) {
        this.msgID = msgID;
    }

    public int getMsgIndex() {
        return msgIndex;
    }

    public void setMsgIndex(int msgIndex) {
        this.msgIndex = msgIndex;
    }

    public int getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(int msgLength) {
        this.msgLength = msgLength;
    }

    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }

    public int getMsgCRC() {
        return msgCRC;
    }

    public void setMsgCRC(int msgCRC) {
        this.msgCRC = msgCRC;
    }

    public int getMsg_End() {
        return msgEnd;
    }

    public void setMsg_End(int msg_End) {
        this.msgEnd = msg_End;
    }

    private int getIntVale(int start, int count, byte[] buffer) {
        if (start < 0 || start >= buffer.length)
            return -1;
        switch (count) {
            case 1:
                return buffer[start];
            case 2:
                return ByteUtils.DoubleBytesToInt(buffer[start + 1], buffer[start]);
            case 3:
                return ByteUtils.ThreeBytesToInt(buffer[start], buffer[start + 1], buffer[start + 2]);
            case 4:
                return ByteUtils.FourBytesToInt(buffer[start], buffer[start + 1], buffer[start + 2], buffer[start + 3]);
            default: {
                MMLog.d(TAG, "do not support more than 4 bytes convert!!!");
                return -1;
            }
        }
    }

    public void parse(byte[] buffer) {
        //datas = new byte[1];
        //this.msgLength = datas.length;
        if (buffer == null) {
            MMLog.log(TAG, "parse failed buffer = " + null);
            return;
        }

        if (buffer.length >= 9) {
            this.msgHead = getIntVale(0, 2, buffer);
            if(this.msgHead != DEFAULT_HEAD) return;

            this.msgEnd = getIntVale(buffer.length - 1, 1, buffer);
            if(this.msgEnd != DEFAULT_END) return;

            this.msgID = getIntVale(2, 2, buffer);
            this.msgIndex = getIntVale(4, 1, buffer);
            this.msgLength = getIntVale(5, 2, buffer);
            this.msgCRC = getIntVale(buffer.length - 2, 1, buffer);

        } else {
            MMLog.log(TAG, "parse failed length = " + buffer.length);
        }
        if (msgLength <= 0) return;
        datas = new byte[msgLength];
        System.arraycopy(buffer, 7, datas, 0, this.msgLength);
    }

}
