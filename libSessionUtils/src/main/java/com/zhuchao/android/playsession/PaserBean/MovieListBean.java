/**
  * Copyright 2019 bejson.com 
  */
package com.zhuchao.android.playsession.PaserBean;


import com.zhuchao.android.video.Movie;

import java.util.List;

/**
 * Auto-generated: 2019-01-15 10:9:8
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
 public class MovieListBean {

    private int pages;
    private int page;
    private int pageSize;
    private List<Movie> list;

    public void setPages(int pages) {
         this.pages = pages;
     }
     public int getPages() {
         return pages;
     }

    public void setPage(int page) {
         this.page = page;
     }
     public int getPage() {
         return page;
     }

    public void setPageSize(int pageSize) {
         this.pageSize = pageSize;
     }
    public int getPageSize() {
         return pageSize;
     }

    public void setList(List<Movie> list) {
         this.list = list;
     }
     public List<Movie> getList() {
         return list;
     }

}