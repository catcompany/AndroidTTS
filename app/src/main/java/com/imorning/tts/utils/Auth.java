package com.imorning.tts.utils;

public class Auth {

    private static volatile Auth ourInstance;

    private final String appId = "14568028";

    private final String appKey = "py14hFnQpWofQrXn7vqbosgLGimNI8Nt";

    private final String secretKey = "hw0k2DzCZ2geLDCeykx4l8jd9DsT3oWW";

    private Auth() {
    }

    public static Auth getInstance() {
        if (ourInstance == null) {
            synchronized (Auth.class) {
                ourInstance = new Auth();
            }
        }
        return ourInstance;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

}
