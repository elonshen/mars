package com.elon.demo.user;

import com.elon.demo.authentication.model.MyUserDetails;
import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserCreateRequest;
import com.elon.demo.user.model.UserUpdateRequest;
import com.elon.demo.user.model.UserVo;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest userCreateRequest);

    User toUser(UserUpdateRequest userUpdateRequest);

    UserVo toUserVo(MyUserDetails myUserDetails);

    List<UserVo> toUserVos(List<User> users);

    default PageImpl<UserVo> toUserVoPage(List<User> content, Pageable pageable, long total) {
        return new PageImpl<>(toUserVos(content), pageable, total);
    }
}
