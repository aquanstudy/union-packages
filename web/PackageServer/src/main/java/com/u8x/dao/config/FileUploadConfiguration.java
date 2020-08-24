package com.u8x.dao.config;

import com.u8x.common.upload.FileMultipartResolver;
import com.u8x.utils.U8XUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartResolver;

/**
 * Created by ant on 2018/4/6.
 */
//@Configuration
public class FileUploadConfiguration {

    @Autowired
    private Environment env;

    //显示声明CommonsMultipartResolver为mutipartResolver
//    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver(){
        RelaxedPropertyResolver presolver = new RelaxedPropertyResolver(env, "spring.servlet.multipart.");

        String maxFileSizeStr = presolver.getProperty("max-file-size");

        long maxFileSize = U8XUtils.parseStoreUnitToBytes(maxFileSizeStr);

        FileMultipartResolver resolver = new FileMultipartResolver();
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);//resolveLazily属性启用是为了推迟文件解析，以在在UploadAction中捕获文件大小异常
        resolver.setMaxInMemorySize(40960);
        resolver.setMaxUploadSize(maxFileSize);//上传文件大小 1024M 50*1024*1024

        return resolver;
    }
}
