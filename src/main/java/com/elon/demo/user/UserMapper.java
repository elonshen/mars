package com.elon.demo.user;

import com.elon.demo.user.model.User;
import com.elon.demo.user.model.UserCreateRequest;
import com.elon.demo.user.model.UserUpdateRequest;
import com.elon.demo.user.model.UserVo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest userCreateRequest);

    UserVo toUserVo(User user);

    List<UserVo> toUserVos(List<User> users);

    default PageImpl<UserVo> toUserVoPage(List<User> content, Pageable pageable, long total) {
        return new PageImpl<>(toUserVos(content), pageable, total);
    }
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updatePerson(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
