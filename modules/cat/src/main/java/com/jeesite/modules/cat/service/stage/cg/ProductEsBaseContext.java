package com.jeesite.modules.cat.service.stage.cg;

import com.jeesite.modules.cat.entity.MaocheAlimamaUnionProductDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProductEsBaseContext<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -3936951650166756265L;

    private MaocheAlimamaUnionProductDO item;

    private T productModel;

}
