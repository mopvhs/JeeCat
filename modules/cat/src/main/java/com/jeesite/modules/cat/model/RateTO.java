package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class RateTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -2134742907154446171L;

    private List<RateDetailTO> details;
}
