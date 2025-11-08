package com.cc.vo;

import lombok.Data;

@Data
public class LoginVO {
    private long id;
    private String username;
    private String nickName;
    private String token;
}
