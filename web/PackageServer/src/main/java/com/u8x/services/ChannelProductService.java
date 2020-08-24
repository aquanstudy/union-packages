package com.u8x.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.u8x.common.XLogger;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.ChannelParamMeta;
import com.u8x.dao.entity.ChannelProduct;
import com.u8x.utils.EncryptUtils;
import com.u8x.utils.HttpUtils;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ant on 2018/5/18.
 */

@Service
public class ChannelProductService {

    private static final XLogger logger = XLogger.getLogger(ChannelProductService.class);

    @Autowired
    private GlobalConfig globalConfig;

    //从u8server同步渠道商品映射配置
    public CompletableFuture<List<ChannelProduct>> getChannelProducts(String appID, Integer channelID){

        CompletableFuture<List<ChannelProduct>> future = new CompletableFuture<>();

        try{

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelID=").append(channelID)
                    .append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID);
            params.put("channelID", channelID+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelproductUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if (jsonResult != null) {
                                logger.error("get channel products from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }

                            future.complete(null);
                            return;
                        }

                        List<ChannelProduct> products = null;
                        JSONArray jsonArray = jsonResult.getJSONArray("data");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            products = JSON.parseArray(jsonArray.toString(), ChannelProduct.class);
                            logger.debug("get channel products from u8server manager success. localize {} channel metes.", products.size());
                        } else {
                            logger.debug("get channel products u8server manager success. there is no channel masters.");
                        }


                        future.complete(products);

                    } catch (Exception e) {
                        future.complete(null);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(null);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }


    //同步渠道商品映射配置到u8server
    public CompletableFuture<Boolean> saveChannelProduct(String appID, Integer channelID, ChannelProduct product){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try{

            if(product.getId() == null){
                product.setId(0);
            }

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelID=").append(channelID)
                    .append("&channelProductID=").append(product.getChannelProductID())
                    .append("&gameProductID=").append(product.getGameProductID())
                    .append("&productID=").append(product.getId())
                    .append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            logger.debug("sign str:{};sign:{}", sb.toString(), sign);

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID);
            params.put("channelID", channelID+"");
            params.put("channelProductID", product.getChannelProductID());
            params.put("gameProductID", product.getGameProductID());
            params.put("productID", product.getId()+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelProductSaveUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if ( jsonResult.getIntValue("code") == 0) {
                            logger.error("save channel product to u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            future.complete(null);
                            return;
                        }

                        future.complete(true);

                    } catch (Exception e) {
                        future.complete(false);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(false);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    //删除渠道商品映射
    public CompletableFuture<Boolean> deleteChannelProduct(String appID, Integer channelID, Integer productID){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try{

            if(productID == null){
                productID = 0;
            }

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelID=").append(channelID)
                    .append("&productID=").append(productID)
                    .append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            logger.debug("sign str:{};sign:{}", sb.toString(), sign);

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID);
            params.put("channelID", channelID+"");
            params.put("productID", productID+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelProductDeleteUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if ( jsonResult.getIntValue("code") == 0) {
                            logger.error("delete channel product from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            future.complete(null);
                            return;
                        }

                        future.complete(true);

                    } catch (Exception e) {
                        future.complete(false);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(false);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

}
