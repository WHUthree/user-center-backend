package com.three.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 4994674003360456562L;

    private String userAccount;

    private String userPassword;
}
