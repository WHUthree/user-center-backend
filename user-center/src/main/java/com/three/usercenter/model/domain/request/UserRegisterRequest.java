package com.three.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liusirui
 * @description 用户注册的请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 6041686645972601287L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String email;
}
