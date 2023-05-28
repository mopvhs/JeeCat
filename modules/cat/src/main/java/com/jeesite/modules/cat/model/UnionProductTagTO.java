package com.jeesite.modules.cat.model;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cat.model.keytitle.UnionProductTagModel;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UnionProductTagTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1384530677925860108L;

    private List<String> brand = new ArrayList<>();
    // 保持字段名称不变
    private List<String> secondbrand = new ArrayList<>();
    private List<String> product = new ArrayList<>();
    private List<String> object = new ArrayList<>();
    private List<String> season = new ArrayList<>();
    private List<String> model = new ArrayList<>();
    private List<String> material = new ArrayList<>();
    private List<String> attribute = new ArrayList<>();

//    // 简单包装个方法，不使用反射
//    public void setTag(Object obj, String key) {
//        if (obj == null || StringUtils.isBlank(key)) {
//            return;
//        }
//        List<String> tags = getTags(obj);
//        if (CollectionUtils.isEmpty(tags)) {
//            return;
//        }
//
//
//        switch (key) {
//            case "brand":
//                if (StringUtils.isBlank(tagValue)) {
//                    return;
//                }
//                this.setBrand(String.valueOf(value));
//                break;
//        }
//
//    }
//
//    private List<String> getTags(Object obj) {
//        List<String> tags = new ArrayList<>();
//
//        Object o = jsonObject.get(key);
//        if (o == null) {
//            return tags;
//        }
//
//        if (o instanceof String tag) {
//            if (StringUtils.isBlank(tag)) {
//                return null;
//            }
//            tags.add(tag);
//        } else if (o instanceof JSONArray) {
//            for (Object v : (JSONArray) o) {
//                if (v instanceof String tag) {
//                    tags.add(tag);
//                }
//            }
//        }
//
//        return tags;
//    }


    public UnionProductTagTO() {
    }

    public UnionProductTagTO(UnionProductTagModel model) {
        if (model == null) {
            new UnionProductTagTO();
            return;
        }
        if (StringUtils.isNotBlank(model.getBrand())) {
            this.getBrand().add(model.getBrand());
        }
        if (StringUtils.isNotBlank(model.getSecondbrand())) {
            this.getSecondbrand().add(model.getSecondbrand());
        }
        if (StringUtils.isNotBlank(model.getProduct())) {
            this.getProduct().add(model.getProduct());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
        if (CollectionUtils.isNotEmpty(model.getModel())) {
            this.setModel(model.getModel());
        }
    }
}
