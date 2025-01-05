package com.jeesite.modules.cat.service.cg.brandlib;

import com.jeesite.modules.cat.model.BrandLibTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibDTO;
import com.jeesite.modules.cat.service.cg.brandlib.dto.BrandLibKeywordDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BrandLibDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2483817488655167048L;

    private BrandLibDTO lib;

    private List<BrandLibKeywordDTO> keywords;
}
