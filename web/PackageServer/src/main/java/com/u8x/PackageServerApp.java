package com.u8x;

import com.u8x.common.XLogger;
import com.u8x.sdk.LocalSDKManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * 执行入口
 */
@SpringBootApplication
@EnableScheduling
@ServletComponentScan
//@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})          //去掉Spring默认的文件上传处理
public class PackageServerApp {

    private static final XLogger logger = XLogger.getLogger(PackageServerApp.class);

    @PostConstruct
    void start(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        logger.debug("curr time zone:"+TimeZone.getDefault());
    }


    public static void main(String[] args) {
        SpringApplication.run(PackageServerApp.class, args);
    }
}
