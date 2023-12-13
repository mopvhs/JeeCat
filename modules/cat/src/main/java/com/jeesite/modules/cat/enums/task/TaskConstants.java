package com.jeesite.modules.cat.enums.task;

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
}
