package com.atguigu.gmall.bean;

public class Cramb {
//
   private String  urlParam;
   private String valueName;

    public Cramb(String urlParam, String valueName) {
        this.urlParam = urlParam;
        this.valueName = valueName;
    }

    public Cramb() {
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
