package com.three.usercenter.common;

/**
 * @description 返回工具类
 *
 * @author liusirui
 */
public class ResultUtils {

    /**
     * @description 失败
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * @description 失败
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * @description 失败
     * @param code
     * @param errorCode
     * @return
     */
    public static BaseResponse error(int code,ErrorCode errorCode) {
        return new BaseResponse(errorCode);
    }

    /**
     * @description 失败
     * @param errorCode
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }
}
