package com.jeesite.modules.cat.service.stage.cg;

import java.util.function.Function;

public abstract class  AbstractProductEsStage<C, T> implements ProductEsStage<C, T> {

    public T doConvert(Function<C, T> function, C context) {
        if (function == null) {
            throw new IllegalArgumentException("组装逻辑不能为空");
        }

        return function.apply(context);
    }

}
