package com.teach.javafx.request;

import com.teach.javafx.util.DateTimeTool;
import com.google.gson.Gson;

import java.util.*;

/**
 * DataRequest 请求参数数据类
 * Map data 保存前端请求参数的Map集合
 */
public class DataRequest {
    private Map<String,Object> data;

    public DataRequest() {
        data = new HashMap<>();
    }

    public Map<String,Object> getData() {
        return data;
    }

    public void setData(Map<String,Object> data) {
        this.data = data;
    }

    public void add(String key, Object obj){
        data.put(key,obj);
    }
    public Object get(String key){
        return data.get(key);
    }

    public String getString(String key){
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof String)
            return (String)obj;
        return obj.toString();
    }
    public Boolean getBoolean(String key){
        Object obj = data.get(key);
        if(obj == null)
            return false;
        if(obj instanceof Boolean)
            return (Boolean)obj;
        return "true".equals(obj.toString());
    }

    public List<?> getList(String key){
        Object obj = data.get(key);
        if(obj == null)
            return new ArrayList<>();
        if(obj instanceof List)
            return (List<?>)obj;
        else
            return new ArrayList<>();
    }
    public Map<String,Object> getMap(String key){
        if(data == null)
            return new HashMap<>();
        Object obj = data.get(key);
        if(obj == null)
            return new HashMap<>();
        if(obj instanceof Map)
            return (Map<String,Object>)obj;
        else
            return new HashMap<>();
    }

    public Integer getInteger(String key) {
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Integer)
            return (Integer)obj;
        String str = obj.toString();
        try {
            return (int)Double.parseDouble(str);
        }catch(Exception e) {
            return null;
        }
    }
    public Long getLong(String key) {
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Long)
            return (Long)obj;
        String str = obj.toString();
        try {
            return Long.parseLong(str);
        }catch(Exception e) {
            return null;
        }
    }

    public Double getDouble(String key) {
        if(data == null)
            return null;
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Double)
            return (Double)obj;
        String str = obj.toString();
        try {
            return Double.parseDouble(str);
        }catch(Exception e) {
            return null;
        }
    }
    public Date getDate( String key) {
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Date)
            return (Date)obj;
        String str = obj.toString();
        return DateTimeTool.formatDateTime(str,"yyyy-MM-dd");
    }
    public Date getTime(String key) {
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Date)
            return (Date)obj;
        String str = obj.toString();
        return DateTimeTool.formatDateTime(str,"yyyy-MM-dd HH:mm:ss");
    }
    public Integer getCurrentPage(){
        Integer cPage = this.getInteger("currentPage");
        if(cPage != null && cPage >0 )
            cPage = cPage -1;
        else
            cPage = 0;
        return cPage;

    }

    /**
     * 将请求数据转换为JSON字符串
     * @return JSON字符串
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

}
