package com.u8x.services;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


/**
 * 运行时获取Spring Bean
 * Created by ant on 2015/10/15.
 */
@Component
@Scope("singleton")
public class XApplicationContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        XApplicationContext.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){

        return applicationContext;
    }

    public static Object getBean(String name){

        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    public static Object getBean(String name, Class type){
        return applicationContext.getBean(name, type);
    }

    public static boolean containBean(String name){

        return applicationContext.containsBean(name);
    }


    @PreDestroy
    public void onDestory(){


    }

}
