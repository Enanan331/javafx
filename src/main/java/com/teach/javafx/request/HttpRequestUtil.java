package com.teach.javafx.request;

import com.teach.javafx.AppStore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * HttpRequestUtil 后台请求实例程序，主要实践向后台发送请求的方法
 *  static boolean isLocal 业务处理程序实现方式 false java-server实现 前端程序通过下面的方法把数据发送后台程序，后台返回前端需要的数据，true 本地方式 业务处理 在SQLiteJDBC 实现
 *  String serverUrl = "http://localhost:9090" 后台服务的机器地址和端口号
 */
public class HttpRequestUtil {
    private static final Gson gson = new Gson();
    private static final HttpClient client = HttpClient.newHttpClient();
    public static String serverUrl = "http://localhost:22223";
//    public static String serverUrl = "http://202.194.7.29:22222";

    /**
     *  应用关闭是需要做关闭处理
     */
    public static void close(){
    }

    /**
     * String login(LoginRequest request)  用户登录请求实现
     * @param request  username 登录账号 password 登录密码
     * @return  返回null 登录成功 AppStore注册登录账号信息 非空，登录错误信息
     */

    public static String login(LoginRequest request){
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/auth/login"))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .headers("Content-Type", "application/json")
                    .build();
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("response.statusCode===="+response.statusCode());
                if (response.statusCode() == 200) {
                    JwtResponse jwt = gson.fromJson(response.body(), JwtResponse.class);
                    AppStore.setJwt(jwt);
                    return null;
                } else if (response.statusCode() == 401) {
                    return "用户名或密码不存在！";
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        return "登录失败";
    }

    /**
     * DataResponse request(String url,DataRequest request) 一般数据请求业务的实现
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return DataResponse 返回后台返回数据
     */
    public static DataResponse request(String url, DataRequest request){
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + url))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .headers("Content-Type", "application/json")
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            request.add("username",AppStore.getJwt().getUsername());
            HttpClient client = HttpClient.newHttpClient();
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                System.out.println("url=" + url +"    response.statusCode="+response.statusCode());
                if (response.statusCode() == 200) {
//                                    System.out.println(response.body());
                    return gson.fromJson(response.body(), DataResponse.class);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        return null;
    }

    /**
     *  MyTreeNode requestTreeNode(String url, DataRequest request) 获取树节点对象
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return MyTreeNode 返回后台返回数据
     */
    public static MyTreeNode requestTreeNode(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), MyTreeNode.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<MyTreeNode> requestTreeNodeList(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<String>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                List<Map<String,Object>> list = gson.fromJson(response.body(),List.class);
                List<MyTreeNode> rList = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : list) {
                    rList.add(new MyTreeNode(stringObjectMap));
                }
                return rList;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求选项列表
     * @param url 请求URL
     * @param req 请求参数
     * @return 选项列表
     */
    public static List<OptionItem> requestOptionItemList(String url, DataRequest req) {
        try {
            String json = post(url, req.toJson());
            if (json == null) {
                return null;
            }
            
            // 尝试解析为数组
            try {
                return new Gson().fromJson(json, new TypeToken<List<OptionItem>>(){}.getType());
            } catch (JsonSyntaxException e) {
                // 如果解析为数组失败，尝试解析为OptionItemList对象
                try {
                    OptionItemList itemList = new Gson().fromJson(json, OptionItemList.class);
                    return itemList.getItemList();
                } catch (JsonSyntaxException e2) {
                    // 如果两种方式都失败，记录错误并返回空列表
                    System.err.println("JSON解析错误: " + e2.getMessage());
                    System.err.println("原始JSON: " + json);
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *   List<OptionItem> getDictionaryOptionItemList(String code) 获取数据字典OptionItemList对象
     * @param code  数据字典类型吗
     * @param
     * @return List<OptionItem> 返回后台返回数据
     */
    public static  List<OptionItem> getDictionaryOptionItemList(String code) {
        DataRequest req = new DataRequest();
        req.add("code", code);
        return requestOptionItemList("/api/base/getDictionaryOptionItemList",req);
    }

    /**
     *  byte[] requestByteData(String url, DataRequest request) 获取byte[] 对象 下载数据文件等
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param request 请求参数对象
     * @return List<OptionItem> 返回后台返回数据
     */
    public static byte[] requestByteData(String url, DataRequest request){
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer "+AppStore.getJwt().getToken())
                .build();
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<byte[]>  response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                return response.body();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * DataResponse uploadFile(String fileName,String remoteFile) 上传数据文件
     * @param uri 上传接口URI
     * @param fileName 本地文件名
     * @param remoteFile 远程文件路径
     * @return 上传操作信息
     */
    public static DataResponse uploadFile(String uri, String fileName, String remoteFile) {
        try {
            Path file = Path.of(fileName);
            if (!Files.exists(file)) {
                return new DataResponse(1, null, "文件不存在: " + fileName);
            }
            
            if (!Files.isReadable(file)) {
                return new DataResponse(1, null, "文件不可读: " + fileName);
            }
            
            // 简化URL构建，避免编码问题
            String url = serverUrl + uri + "?uploader=HttpTestApp&remoteFile=" + remoteFile + "&fileName=" + file.getFileName();
            
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("上传照片响应状态码: " + response.statusCode());
            System.out.println("上传照片响应内容: " + response.body());
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            } else {
                return new DataResponse(1, null, "服务器返回错误状态码: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new DataResponse(1, null, "上传过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * DataResponse importData(String url, String fileName, String paras) 导入数据文件
     * @param url  Web请求的Url 对用后的 RequestMapping
     * @param fileName 本地文件名
     * @param paras  上传参数
     * @return 导入结果信息
     */
    public static DataResponse importData(String url, String fileName, String paras)  {
        try {
            Path file = Path.of(fileName);
            String urlStr = serverUrl+url+"?uploader=HttpTestApp&fileName=" + file.getFileName() ;
            if(paras != null && !paras.isEmpty())
                urlStr += "&"+paras;
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                    .build();
            HttpResponse<String>  response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), DataResponse.class);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送POST请求并返回响应字符串
     * @param url 请求URL
     * @param jsonBody 请求体JSON字符串
     * @return 响应字符串
     */
    private static String post(String url, String jsonBody) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .headers("Content-Type", "application/json")
                .headers("Authorization", "Bearer " + AppStore.getJwt().getToken())
                .build();
        
        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("HTTP错误: " + response.statusCode());
                System.err.println("响应内容: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return null;
    }

}
