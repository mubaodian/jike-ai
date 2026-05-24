package com.swl.jikeai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 仅对方法有效
@Retention(RetentionPolicy.RUNTIME) // 运行时注解
public @interface AuthCheck {

    /**
     * 权限校验注解
     *
     * @return 必须有个角色
     */
    String mustRole() default "";
}
