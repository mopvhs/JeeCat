package com.jeesite.modules.cat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class RateDetailTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5499552590836215201L;

    private String count;

    private String type;

    private String word;
}
