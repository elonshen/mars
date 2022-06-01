package com.elon.demo.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserVo {
    private Long id;
    private String name;
    private String username;
    private String role;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createTime;
}
