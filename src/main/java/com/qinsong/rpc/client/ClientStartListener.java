package com.qinsong.rpc.client;

import com.qinsong.rpc.common.EnableQSRpc;
import com.qinsong.rpc.common.serialize.ISerialize;
import com.qinsong.rpc.common.serialize.Protostuff;
import com.qinsong.rpc.common.util.AopTargetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.song.qsrpc.send.RPCClientManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;

@Service
public class ClientStartListener implements ApplicationListener<ContextRefreshedEvent>, BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStartListener.class);


    @Override//装载完所有bean调用,此时还没启动http服务,ApplicationRunner是启动完http端口后的
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            initClient(event.getApplicationContext());
        }
    }


    private void initClient(ApplicationContext ctx) {
        Map<String, Object> startMap = ctx.getBeansWithAnnotation(EnableQSRpc.class);
        if (startMap.size() == 0) return;
        EnableQSRpc enableQSRpc = startMap.values().iterator().next().getClass().getAnnotation(EnableQSRpc.class);
        if (!enableQSRpc.enabled()) return;
        //初始化下客户端
        RPCClientManager.getInstance();
    }


    @Autowired(required = false)
    private ISerialize iSerialize;

    //扫描容器所有@QSRpcReference的变量,new一个代理赋值
    @Override//所有bean初始化完成前会调用
    public Object postProcessBeforeInitialization(final Object row, final String beanName) throws BeansException {
        if (iSerialize == null) iSerialize = new Protostuff();

        Object bean = row;
        try {
            bean = AopTargetUtils.getTarget(row);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("获取被代理对象出错:" + row);
        }
//        if (bean != row) {
//            LOGGER.debug("Bean getTarget: " + row.getClass() + "--->" + bean.getClass());
//        }

        Class beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field f : fields) {
//            LOGGER.debug("Scan Field: " + f.getName());
            QSRpcReference qsRpcReference = f.getAnnotation(QSRpcReference.class);
            if (qsRpcReference != null) {

                Class<?> c = f.getType();
                if (!c.isInterface()) throw new IllegalArgumentException(f + " @QSRpcReference 必须注解在一个接口上");
                Object porxy = new QSRpcPorxy(c, qsRpcReference, iSerialize).getPorxy();//创建代理

                f.setAccessible(true);
                try {
                    f.set(bean, porxy);
                    LOGGER.info("Create QSRpcPorxy: " + c.getName() + "--->" + bean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return row;
    }

}