
package com.jeesite.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * JSON工具类
 * 
 * @author zhaoloon
 * @since 1.0 create at 2016年5月31日
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final ObjectMapper OM_WITH_NULL = new ObjectMapper();

    private JsonUtils() {

    }

    static {
        OM.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /**
     * 对象转json
     * 
     * @param values
     * @return json string
     * @throws IOException
     * @throws IOException
     */
    public static String toJSONString(Object... values)  {
        if (null == values || values.length == 0) {
            return "";
        }

        try {
            if (values.length == 1) {
                return OM.writeValueAsString(values[0]);
            } else {
                ObjectNode result = OM.createObjectNode();
                for (Object value : values) {
                    result.putPOJO(value.getClass().getSimpleName(), value);
                }
                return OM.writeValueAsString(result);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将json字符串转换为Java对象
     * 
     * @param jsonString
     * @param clazz
     * @return T
     * @throws IOException
     */
    public static <T> T toJavaObject(String jsonString, Class<T> clazz) throws IOException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }

        return OM.readValue(jsonString, clazz);
    }

    /**
     * 将json转换为List、Map等引用类型
     * 
     * @param jsonString
     * @param typeReference
     * @return reference type
     * @throws IOException
     */
    public static <T> T toReferenceType(String jsonString, TypeReference<T> typeReference) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }

            return OM.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            log.error("json转换异常 jsonString:{}", jsonString, e);
        }
        return null;
    }

    /**
     * 将json转换为List、Map等引用类型
     *
     * @param jsonString
     * @throws IOException
     */
    public static JSONObject toJsonObject(String jsonString) {
        try {
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }

            return JSONObject.parseObject(jsonString);
        } catch (Exception e) {
//            log.error("json转换异常 jsonString:{}", jsonString, e);
        }

        return null;
    }
}
