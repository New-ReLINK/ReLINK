package com.my.relink.config.log.context;

import org.slf4j.MDC;

public class SystemMDCContext implements AutoCloseable{

    private static final String COMPONENT = "component";
    private static final String OPERATION = "operation";
    private static final String REQUEST_ID = "requestId";
    private static final String DURATION = "duration";

    public void putComponent(String component) {
        MDC.put(COMPONENT, component);
    }

    public void putOperation(String operation) {
        MDC.put(OPERATION, operation);
    }

    public void putRequestId(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    public void putDuration(long duration) {
        MDC.put(DURATION, String.valueOf(duration));
    }

    @Override
    public void close() {
        MDC.remove(COMPONENT);
        MDC.remove(OPERATION);
        MDC.remove(REQUEST_ID);
        MDC.remove(DURATION);
    }
}
