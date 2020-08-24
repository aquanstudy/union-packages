package com.u8x.controller;

import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.ListView;
import com.u8x.controller.view.ResponseView;
import com.u8x.dao.entity.ChannelMaster;
import com.u8x.dao.entity.ChannelProduct;
import com.u8x.dao.entity.Keystore;
import com.u8x.services.ChannelProductService;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ant on 2018/5/18.
 */

@Controller
@RequestMapping("/admin/channelproduct")
public class ChannelProductController {

    private static final XLogger logger = XLogger.getLogger(ChannelProductController.class);

    @Autowired
    private ChannelProductService productService;


    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<ListView<ChannelProduct>> getChannelProducts(String appID, Integer channelID){
        DeferredResult<ListView<ChannelProduct>> result = new DeferredResult<>(30 * 60 * 1000L);

        try{
            CompletableFuture<List<ChannelProduct>> future = productService.getChannelProducts(appID, channelID);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((lst, err) -> {
                if (err != null) {
                    result.setResult(ListView.buildFailure(err.getMessage()));
                    return;
                }
                if (lst == null) {
                    result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                    return;
                }
                ListView<ChannelProduct> view = new ListView<>();
                view.setData(lst);
                result.setResult(view);

            });


        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }


    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    DeferredResult<ResponseView> saveProduct(String appID, Integer channelID, @ModelAttribute ChannelProduct product) {
        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();

        try{

            CompletableFuture<Boolean> future = productService.saveChannelProduct(appID, channelID, product);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((data, err) -> {
                if (err != null) {
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }
                if (data == null || !data) {
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    return;
                }

                result.setResult(view.success());

            });



        }catch (Exception e){
            e.printStackTrace();
            result.setResult(view.failure(Consts.Tips.FailMsg));
        }

        return result;
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    DeferredResult<ResponseView>  deleteProduct(String appID, Integer channelID, Integer id){
        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();

        try{

            CompletableFuture<Boolean> future = productService.deleteChannelProduct(appID, channelID, id);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((data, err) -> {
                if (err != null) {
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }
                if (data == null || !data) {
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    return;
                }

                result.setResult(view.success());

            });



        }catch (Exception e){
            e.printStackTrace();
            result.setResult(view.failure(Consts.Tips.FailMsg));
        }

        return result;
    }

}
