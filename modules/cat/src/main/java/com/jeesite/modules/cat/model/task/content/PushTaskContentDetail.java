package com.jeesite.modules.cat.model.task.content;

import com.jeesite.modules.cat.service.cg.task.dto.TaskDetail;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PushTaskContentDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 3619517226687520758L;

    private String detail;

    private String img;

    public PushTaskContentDetail() {
    }

    public PushTaskContentDetail(String detail, String img) {
        this.detail = detail;
        this.img = img;
    }

    public static PushTaskContentDetail buildContent(TaskDetail detail) {
        PushTaskContentDetail content = new PushTaskContentDetail();


        return content;
    }
}
