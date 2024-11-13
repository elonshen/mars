package com.elon.mars.controller.mapper;

import com.elon.mars.controller.dto.UserCreateRequest;
import com.elon.mars.controller.dto.UserUpdateRequest;
import com.elon.mars.controller.dto.UserVO;
import com.elon.mars.domain.User;
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

    UserVO toUserVo(User user);

    List<UserVO> toUserVos(List<User> users);

    default PageImpl<UserVO> toUserVoPage(List<User> content, Pageable pageable, long total) {
        return new PageImpl<>(toUserVos(content), pageable, total);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updatePerson(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}
