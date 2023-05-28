package com.jeesite.modules.cat.model.keytitle;

import com.jeesite.modules.cat.model.UnionProductTagTO;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class UnionProductTagModel implements Serializable {

    @Serial
    private static final long serialVersionUID = -6541520529425032851L;

    private String brand;
    // 保持字段名称不变
    private String secondbrand;
    private String product;
    private List<String> object;
    private List<String> season;
    private List<String> model;
    private List<String> material;
    private List<String> attribute;

    public UnionProductTagModel() {
    }

    public UnionProductTagModel(UnionProductTagTO tagTO) {
        if (tagTO == null) {
            new UnionProductTagModel();
            return;
        }
        this.brand = getSingleTag(tagTO.getBrand());
        this.secondbrand = getSingleTag(tagTO.getSecondbrand());
        this.product = getSingleTag(tagTO.getProduct());
        this.object = Optional.ofNullable(tagTO.getObject()).orElse(new ArrayList<>());
        this.season = Optional.ofNullable(tagTO.getSeason()).orElse(new ArrayList<>());
        this.material = Optional.ofNullable(tagTO.getMaterial()).orElse(new ArrayList<>());
        this.attribute = Optional.ofNullable(tagTO.getAttribute()).orElse(new ArrayList<>());
    }

    public static String getSingleTag(List<String> tagValues) {
        if (CollectionUtils.isEmpty(tagValues)) {
            return "";
        }

        return tagValues.get(0);
    }
}
