package cn.hmcode.middle.db.router.annotation;

import java.lang.annotation.*;


/**
 * @description: 路由策略，分表标记
 * @author: hm
 * @date: 2023/5/11
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DBRouterStrategy {

    boolean splitTable() default false;

}
