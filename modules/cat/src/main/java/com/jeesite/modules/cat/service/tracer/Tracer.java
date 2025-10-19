package com.jeesite.modules.cat.service.tracer;

import com.jeesite.modules.cat.model.MemberUserRespVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Tracer {

    public static final String MDC_USER_ID = "userId";

    private static ThreadLocal<TraceInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static TraceInfo getCurrentTraceInfo() {
        TraceInfo traceInfo = THREAD_LOCAL.get();
        if (traceInfo == null) {
            traceInfo = new TraceInfo();

            setCurrentTraceInfo(traceInfo);
        }
        return traceInfo;
    }

    public static MemberUserRespVO getUser() {
        TraceInfo traceInfo = THREAD_LOCAL.get();
        if (traceInfo == null) {
            return null;
        }

        return traceInfo.getUser();
    }

    public static void setCurrentTraceInfo(TraceInfo traceInfo) {
        THREAD_LOCAL.set(traceInfo);
    }

    public static void clear() {
        THREAD_LOCAL.remove();
    }

}
