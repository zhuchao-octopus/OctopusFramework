/**
  * Copyright 2019 bejson.com 
  */
package com.zhuchao.android.playsession.PaserBean;
import java.util.List;

/**
 * Auto-generated: 2019-04-20 17:47:10
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class ScheduleVideoRootBean {

    private String msg;
    private List<ScheduleVideoBean> data;
    private int state;
    public void setMsg(String msg) {
         this.msg = msg;
     }
     public String getMsg() {
         return msg;
     }

    public void setData(List<ScheduleVideoBean> data) {
         this.data = data;
     }
     public List<ScheduleVideoBean> getData() {
         return data;
     }

    public void setState(int state) {
         this.state = state;
     }
     public int getState() {
         return state;
     }

}