package com.my.relink.config.log.context;

import org.slf4j.MDC;

public class HttpMDCContext implements AutoCloseable{

    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String STATUS = "status";
    private static final String DURATION = "duration";

    private static final String CLIENT_IP = "clientIp";

    public void putStartTime(long startTime){
        MDC.put(START_TIME, String.valueOf(startTime));
    }
    public void putRequestId(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    public static String getRequestId(){
        return MDC.get(REQUEST_ID);
    }

    public void putMethod(String method) {
        MDC.put(METHOD, method);
    }

    public void putUri(String uri) {
        MDC.put(URI, uri);
    }

    public void putClientIp(String clientIp) {
        MDC.put(CLIENT_IP, clientIp);
    }

    public void putStatus(int status) {
        MDC.put(STATUS, String.valueOf(status));
    }

    public void putDuration(long duration) {
        MDC.put(DURATION, String.valueOf(duration));
    }

    @Override
    public void close() {
        MDC.remove(START_TIME);
        MDC.remove(REQUEST_ID);
        MDC.remove(METHOD);
        MDC.remove(URI);
        MDC.remove(STATUS);
        MDC.remove(DURATION);
        MDC.remove(CLIENT_IP);
    }

}
