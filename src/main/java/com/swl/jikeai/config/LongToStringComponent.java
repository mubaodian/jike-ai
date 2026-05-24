package com.swl.jikeai.config;

import tools.jackson.databind.ValueSerializer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import org.springframework.boot.jackson.JacksonComponent;

/**
 *  Spring MVC Jackson 配置
 */
@JacksonComponent
public class LongToStringComponent {

    /**
     *  添加 Long 转 json精度丢失的配置
     */
    public static class LongSerializer extends ValueSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator jgen, SerializationContext context) {
            if (value == null) {
                jgen.writeNull();
            } else {
                jgen.writeString(value.toString());  // 将 Long 转为字符串输出
            }
        }
    }
}