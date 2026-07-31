package com.acme.mapper;

import com.acme.domain.model.Dependent;
import com.acme.domain.model.Family;
import com.acme.domain.model.User;
import com.acme.dto.response.DependentResponse;
import com.acme.dto.response.MyFamilyResponse;
import com.acme.dto.response.UserResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class MyFamilyMapper {

    @Inject
    UserMapper userMapper;

    @Inject
    DependentMapper dependentMapper;

    public MyFamilyResponse toResponse(
            Family family,
            List<User> users,
            List<Dependent> dependents
    ) {
        List<UserResponse> responsibleUsers = users.stream()
                .map(userMapper::toResponse)
                .toList();

        List<DependentResponse> dependentResponses = dependents.stream()
                .map(dependentMapper::toResponse)
                .toList();

        return new MyFamilyResponse(
                family.getId(),
                family.getName(),
                responsibleUsers,
                dependentResponses
        );
    }
}
