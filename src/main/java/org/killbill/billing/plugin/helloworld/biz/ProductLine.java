package org.killbill.billing.plugin.helloworld.biz;

import com.google.common.base.Strings;

/**
 * @Description 赠送金适用的产品线，目前只上线 IM 产品
 * @Author Toria toria.shi@easemob.com
 * @Date 2021/11/14 下午9:41
 **/
public enum ProductLine {
    IM, MQTT, PUSH, TELCO, MSG, ANY;

    public static ProductLine find(String product){
        if(!Strings.isNullOrEmpty(product)){
            for(ProductLine e : ProductLine.values()){
                if(product.equals(e.name())){
                    return e;
                }
            }
        }
        return null;
    }

    public static ProductLine findStartWith(String planName){
        if(!Strings.isNullOrEmpty(planName)){
            for(ProductLine e : ProductLine.values()){
                if(planName.toUpperCase().startsWith(e.name())){
                    return e;
                }
            }
        }
        return null;
    }
}
