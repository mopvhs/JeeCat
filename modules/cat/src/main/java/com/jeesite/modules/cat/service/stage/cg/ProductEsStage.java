package com.jeesite.modules.cat.service.stage.cg;

import com.jeesite.modules.cat.model.CarAlimamaUnionProductIndex;

import java.util.List;
import java.util.function.Function;

public interface ProductEsStage<C, T> {

    List<String> getSources();

    T convert(C context);

}
