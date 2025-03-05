package com.jeesite.modules.cgcat.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BrandLibImportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -9031523956677541414L;

    @ExcelFields({
            @ExcelField(title="brand_cn", attrName="brandCn", align= ExcelField.Align.LEFT, sort=10),
            @ExcelField(title="brand_en", attrName="brandEn", align= ExcelField.Align.LEFT, sort=20),
            @ExcelField(title="product_name", attrName="productName", align = ExcelField.Align.LEFT, sort=30),
            @ExcelField(title="product_desc", attrName="productDesc", align= ExcelField.Align.LEFT, sort=40),
            @ExcelField(title="keywords", attrName="keywords", align= ExcelField.Align.LEFT, sort=50),
    })
    public BrandLibImportDTO() {

    }

    @ExcelProperty("brand_cn") // product_brand.creator (新增)
    private String brandCn;

    @ExcelProperty("brand_en") // product_brand.creator (新增)
    private String brandEn;

    @ExcelProperty("product_name") // product_brand.creator (新增)
    private String productName;

    @ExcelProperty("product_desc") // product_brand.creator (新增)
    private String productDesc;

    @ExcelProperty("keywords") // product_brand.creator (新增)
    private String keywords;
}
