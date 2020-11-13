package com.qinsong.rpc.client;

import com.qinsong.rpc.common.EnableQSRpc;
import com.qinsong.rpc.common.serialize.ISerialize;
import com.qinsong.rpc.common.serialize.Protostuff;
import com.qinsong.rpc.common.util.AopTargetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.song.qsrpc.send.RPCClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;

@Service
public class ClientStartListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStartListener.class);


    @Override//装载完所有bean调用,此时还没启动http服务,ApplicationRunner是启动完http端口后的
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
                scan(event.getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Autowired(required = false)
    private ISerialize iSerialize;

    //扫描容器所有@QSRpcReference的变量,new一个代理赋值
    private void scan(ApplicationContext ctx) throws Exception {
        Map<String, Object> startMap = ctx.getBeansWithAnnotation(EnableQSRpc.class);
        if (startMap == null || startMap.size() == 0) return;

        EnableQSRpc enableQSRpc = startMap.values().iterator().next().getClass().getAnnotation(EnableQSRpc.class);
        if (iSerialize == null) iSerialize = new Protostuff();

        String[] beanNames = ctx.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = ctx.getBean(beanName);//注意这个获取的有可能是代理对象

//            LOGGER.info("bean: " + beanName + "--->" + bean);

            bean = AopTargetUtils.getTarget(bean);
            //这里可以加一个过滤

            Class beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();
            for (Field f : fields) {
//                LOGGER.info("Scan Field: " + f.getName());

                QSRpcReference qsRpcReference = f.getAnnotation(QSRpcReference.class);
                if (qsRpcReference != null) {
                    Class<?> c = f.getType();
                    if (!c.isInterface()) new IllegalArgumentException(f + " @QSRpcReference 必须注解在一个接口上");
                    Object porxy = new QSRpcPorxy(c, qsRpcReference, iSerialize).getPorxy();//创建代理

                    f.setAccessible(true);
                    f.set(bean, porxy);

                    LOGGER.info("Create QSRpcPorxy: " + c.getName() + "--->" + bean);
                }
            }
        }
        //初始化下客户端
        RPCClientManager.getInstance();

    }


    private void filter(Object bean, String[] packNames) {
        Class c = bean.getClass();
        String pack = c.getPackage().getName();
        for (String packName : packNames) {

        }

    }

}