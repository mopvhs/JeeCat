package com.jeesite.modules.cat.service.stage.cg.ocean;

import com.jeesite.common.codec.Md5Utils;
import com.jeesite.common.utils.JsonUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SimilarContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 2215503693050824872L;

    @Deprecated
    private List<SimilarDetail> products;

    // 淘宝为空
    @Deprecated
    private List<String> failUrls;

    // 口令或者链接个数
    @Deprecated
    private Integer num;

    private String simHash;

    public String calCode() {
        List<SimilarDetail> details = new ArrayList<>();
        List<String> fUrls = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(products)) {
            // 先排序
            details.addAll(products);
            details.sort(new Comparator<SimilarDetail>() {
                @Override
                public int compare(SimilarDetail o1, SimilarDetail o2) {
                    return o1.getResourceId().compareTo(o2.getResourceId());
                }
            });

        }
        if (CollectionUtils.isNotEmpty(failUrls)) {
            // 先排序
            fUrls = failUrls.stream().sorted().collect(Collectors.toList());
        }

        // 计算md5
        String one = Md5Utils.md5(JsonUtils.toJSONString(details));
        String two = Md5Utils.md5(JsonUtils.toJSONString(fUrls));

        return num + "_" + Md5Utils.md5(one + "_" + two);
    }
}
