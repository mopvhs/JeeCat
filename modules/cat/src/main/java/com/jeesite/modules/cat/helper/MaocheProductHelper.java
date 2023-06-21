package com.jeesite.modules.cat.helper;

import com.jeesite.modules.cat.entity.MaocheProductDO;
import com.jeesite.modules.cat.model.MaocheProductIndex;

import java.util.Date;

public class MaocheProductHelper {

    public static MaocheProductIndex buildIndex(MaocheProductDO productDO) {

        if (productDO == null) {
            return null;
        }

        MaocheProductIndex index = new MaocheProductIndex();

        index.setId(productDO.getIid());
        index.setContent(productDO.getContent());
        index.setItemId(productDO.getItemId());
        index.setItemIdSuffix(productDO.getItemIdSuffix());
        index.setUniqueHash(productDO.getUniqueHash());
        index.setContentNew(productDO.getContentNew());
        index.setTitle(productDO.getTitle());
        index.setStatus(productDO.getStatus());
        index.setSyncTime(date2Long(productDO.getSyncTime()));
        index.setAffLinkConvTime(date2Long(productDO.getAffLinkConvTime()));
        index.setCreateTime(date2Long(productDO.getCreateTime()));
        index.setUpdateTime(date2Long(productDO.getUpdateTime()));
        index.setProcessed(productDO.getProcessed());
        index.setAffType(productDO.getAffType());
        index.setImageUrl(productDO.getImageUrl());

        return index;
    }

    private static long date2Long(Date date) {
        if (date == null) {
            return 0L;
        }

        return date.getTime();
    }
}
