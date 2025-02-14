package com.zb.meeteat.domain.user.service;

import com.zb.meeteat.domain.user.dto.UserProfileResponse;
import com.zb.meeteat.domain.user.entity.User;
import com.zb.meeteat.domain.user.repository.UserRepository;
import com.zb.meeteat.exception.CustomException;
import com.zb.meeteat.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getIntroduce(),
                user.getRole().name(),
                user.getSignupType().name(),
                user.getMatchingCount(),
                user.getIsPenalty(),
                user.getBannedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Transactional
    public void updateNickname(User user, String newNickname) {
        // 닉네임 중복 검사
        if (userRepository.existsByNickname(newNickname)) {
            throw new CustomException(UserErrorCode.NICKNAME_ALREADY_REGISTERED);
        }

        // 닉네임 변경
        user.updateNickname(newNickname);
        userRepository.save(user);
    }

    @Transactional
    public void updateIntroduce(User user, String newIntroduce) {
        user.updateIntroduce(newIntroduce);
        userRepository.save(user);
    }



}

