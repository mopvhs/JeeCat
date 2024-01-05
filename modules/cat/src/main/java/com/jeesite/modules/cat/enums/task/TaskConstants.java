package com.jeesite.modules.cat.enums.task;

import com.jeesite.modules.cat.enums.EnumMatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.function.Function;

public interface TaskConstants {

    enum Props {

        KG("kg", "重量"),
        CM("cm", "尺寸"),
        COLOR("color", "颜色"),
        SIZE("size", "尺码"),
        ;

        private String code;
        private String desc;

        Props(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static Props getByCode(String code) {
            if (code == null) {
                return null;
            }
            for (Props props : Props.values()) {
                if (props.getCode().equals(code)) {
                    return props;
                }
            }
            return null;
        }
    }

    @Getter
    @EnumMatch(method = "getSource")
    enum TaskSource {
        OCEAN( "ocean", "公海"),
        PRODUCT("product", "选品库");

        private String source;

        private String desc;

        TaskSource(String source, String desc) {
            this.source = source;
            this.desc = desc;
        }

        public static TaskSource getBySource(String source) {
            return BaseEnum.getByItem(source, TaskSource.values());
        }
    }

    interface BaseEnum {

        public static <T, R> T getByItem(R item, T[] enumObj) {
            if (item == null) {
                return null;
            }
            try {
                Class<?> aClass = Class.forName(enumObj[0].getClass().getName());
                EnumMatch annotation = aClass.getAnnotation(EnumMatch.class);
                if (annotation == null) {
                    return null;
                }

                String method = annotation.method();

                Method methodObj = aClass.getMethod(method);

                for (T obj : enumObj) {
                    R res = (R) methodObj.invoke(obj);
                    if (item.equals(res)) {
                        return obj;
                    }
                }
            } catch (Exception e) {

            }
            return null;
        }
    }
}
