package com.u8x.utils;

import com.u8x.common.XLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by guofeng.qin on 2017/3/1.
 */
public class HttpUtils {
    private static final XLogger logger = XLogger.getLogger(HttpUtils.class);

    // FIXME: 配置合理的线程池 http://www.cnblogs.com/zemliu/p/3719292.html
    private static final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

    static {
        httpClient.start();
    }

    public static CloseableHttpAsyncClient getHttpClient() {
        return httpClient;
    }

    public static void post(String url, Map<String, String> paramsMap, final IHttpCallback callback) throws UnsupportedEncodingException {

        logger.debug("post url :{}", url);

        List<NameValuePair> params = new ArrayList<>();
        if(paramsMap != null){
            for (Map.Entry<String, String> d : paramsMap.entrySet()) {
                params.add(new BasicNameValuePair(d.getKey(), d.getValue()));
            }
        }
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(params, Charset.forName("UTF-8")));

        getHttpClient().execute(post, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                String data = parseBody(httpResponse);
                callback.completed(data);
            }

            @Override
            public void failed(Exception e) {
                callback.failed(e.getMessage());
            }

            @Override
            public void cancelled() {
                callback.failed("http cancelled");
            }
        });

    }

    public static String parseBody(HttpResponse result) {
        String body = null;

        if (result != null && result.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = result.getEntity();
            if (entity != null) {
                InputStream instream = null;
                try {
                    instream = entity.getContent();
                    StringBuilder sb = new StringBuilder();
                    char[] tmp = new char[64];
                    Reader reader = new InputStreamReader(instream, "utf-8");
                    int l;
                    while ((l = reader.read(tmp)) != -1) {
                        sb.append(tmp, 0, l);
                    }
                    body = sb.toString();
                    logger.debug("http body: {}", body);
                } catch (IOException e) {
                    logger.error("HttpException!", e);
                } finally {
                    if (instream != null) {
                        try {
                            instream.close();
                        } catch (IOException e) {
                            logger.error("HttpCloseException!", e);
                        }
                    }
                }
            } else {
                logger.debug("http entity is null !");
            }
        }

        return body;
    }

    public static String getIpAddr(HttpServletRequest request){

        try{
            String ip = request.getHeader("X-Real-IP");
            if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getHeader("X-Forwarded-For");
            if (!StringUtils.isEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 多次反向代理后会有多个IP值，第一个为真实IP。
                int index = ip.indexOf(',');
                if (index != -1) {
                    return ip.substring(0, index);
                } else {
                    return ip;
                }
            } else {
                return request.getRemoteAddr();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "unknown";
    }

    public interface IHttpCallback{
        public void completed(String data);
        public void failed(String msg);
    }


    public static ResponseEntity<InputStreamResource> generateDownloadFile(String filePath, String fileName) throws IOException {

        FileSystemResource file = new FileSystemResource(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(file.getInputStream()));
    }

}
