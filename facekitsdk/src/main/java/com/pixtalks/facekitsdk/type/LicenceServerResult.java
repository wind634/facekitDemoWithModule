package com.pixtalks.facekitsdk.type;

public class LicenceServerResult {
    private int code;
    private String content;

    public LicenceServerResult(int code, String content) {
        this.code = code;
        this.content = content;
    }

    /**
     * @return 0 success
     */
    public int getCode() {
        return code;
    }

    /**
     * @return while code == 0, the content is a valid licence.
     */
    public String getContent() {
        return content;
    }
}
