package com.small.mq.client.consumer;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/5/19 6:09 PM
 */
public class Result {

    // code
    public static final String SUCCESS_CODE = "SUCCESS";
    public static final String FAIL_CODE = "FAIL";   // default

    // result
    public static final Result SUCCESS = new Result(SUCCESS_CODE);
    public static final Result FAIL = new Result(FAIL_CODE);

    // field
    public String code;
    public String log;

    // construct
    public Result() {
    }

    public Result(String code) {
        this.code = code;
    }

    public Result(String code, String log) {
        this.code = code;
        this.log = log;
    }

    // set get
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    // tool
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
