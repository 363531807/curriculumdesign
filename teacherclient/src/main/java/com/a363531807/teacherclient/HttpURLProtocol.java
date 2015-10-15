package com.a363531807.teacherclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by 363531807 on 2015/8/7.
 */
public class HttpURLProtocol {

    public static String sendpostrequest(String path,
                                          Map<String, String> params) throws Exception {
        // name=xx&phone=xx
        StringBuilder data = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                data.append(entry.getKey()).append("=");
                data.append(entry.getValue()).append("&");
            }
            data.deleteCharAt(data.length() - 1);
        }
        byte[] entiy = data.toString().getBytes(); // 生成实体数据
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true, 默认情况下是false;
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        conn.setRequestProperty("Content-Length", String.valueOf(entiy.length));
        // Post 请求不能使用缓存
        conn.setUseCaches(false);
        // 连接，从上述第2条中url.openConnection()至此的配置必须要在connect之前完成，
        conn.connect();
        // 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，
        // 所以在开发中不调用上述的connect()也可以)。
        OutputStream outstream = conn.getOutputStream();
        outstream.write(entiy);
        outstream.flush();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream instream = conn.getInputStream();
            ByteArrayOutputStream byteoutstream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len ;
            while ((len=instream.read(buffer)) != -1) {
                byteoutstream.write(buffer,0,len);
            }
            instream.close();
            outstream.close();
            conn.disconnect();
            return byteoutstream.toString().trim();
        }
        outstream.close();
        conn.disconnect();
        return "false";
    }

    public static String postjson(String path,
                                         byte[] json) throws Exception {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true, 默认情况下是false;
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setRequestProperty("Content-Length", String.valueOf(json.length));
        // 连接，从上述第2条中url.openConnection()至此的配置必须要在connect之前完成，
        conn.connect();
        // 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，
        // 所以在开发中不调用上述的connect()也可以)。
        OutputStream outstream = conn.getOutputStream();
        outstream.write(json);
        outstream.flush();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream instream = conn.getInputStream();
            ByteArrayOutputStream byteoutstream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len=instream.read(buffer)) != -1) {
                byteoutstream.write(buffer,0,len);
            }
            outstream.close();
            instream.close();
            conn.disconnect();
            return byteoutstream.toString().trim();
        }
        outstream.close();
        conn.disconnect();
        return "error";
    }




}
