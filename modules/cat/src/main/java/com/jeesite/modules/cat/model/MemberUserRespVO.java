package com.jeesite.modules.cat.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MemberUserRespVO implements Serializable {
   
    @Serial
    private static final long serialVersionUID = 8007015289721871726L;

    private Long id;

    private String registerIp;

    private String loginIp;

    private Date loginDate;

    private Date createTime;

    // ========== 其它信息 ==========

    private Integer point;

    private Integer totalPoint;

    private List<String> tagNames;

    private String levelName;

    private String groupName;

    private Integer experience;

    private String mobile;

    private Byte status;

    private String nickname;

    private String avatar;

    private String name;

    private Byte sex;

    private Long areaId;

    private String areaName;

    private Date birthday;

    private String mark;

    private List<Long> tagIds;

    private Long levelId;

    private Long groupId;
}
