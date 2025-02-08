package com.zb.meeteat.domain.user.repository;

import com.zb.meeteat.domain.user.entity.Role;
import com.zb.meeteat.domain.user.entity.SignUpType;
import com.zb.meeteat.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * 기본적인 User 객체를 생성하는 메서드
     */
    private User createUser(String email, String password, String nickname) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(Role.USER)
                .signupType(SignUpType.EMAIL)
                .build();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void findByEmailTest() {
        // given
        User user = createUser("test@example.com", "password123", "testuser");
        userRepository.saveAndFlush(user);

        // when
        User foundUser = userRepository.findByEmail("test@example.com")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // then
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getNickname()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외 발생")
    void findByNonExistingEmailTest() {
        assertThatThrownBy(() -> userRepository.findByEmail("nonexistent@example.com")
                .orElseThrow(() -> new IllegalArgumentException("User not found")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("닉네임으로 사용자 조회 테스트")
    void findByNicknameTest() {
        // given
        User user = createUser("nickname@example.com", "password123", "uniqueNickname");
        userRepository.saveAndFlush(user);

        // when
        User foundUser = userRepository.findByNickname("uniqueNickname")
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // then
        assertThat(foundUser.getNickname()).isEqualTo("uniqueNickname");
        assertThat(foundUser.getEmail()).isEqualTo("nickname@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 닉네임으로 조회 시 예외 발생")
    void findByNonExistingNicknameTest() {
        assertThatThrownBy(() -> userRepository.findByNickname("nonexistentNickname")
                .orElseThrow(() -> new IllegalArgumentException("User not found")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("사용자 저장 테스트")
    void savedUserTest() {
        // given
        User user = createUser("save@example.com", "password123", "savedUser");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("save@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("savedUser");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 테스트")
    void existsByEmailTest() {
        // given
        User user = createUser("exists@example.com", "password123", "existsUser");
        userRepository.saveAndFlush(user);

        // when
        boolean exists = userRepository.existsByEmail("exists@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 존재 여부 확인 테스트")
    void existsByNicknameTest() {
        // given
        User user = createUser("nicknameexists@example.com", "password123", "nicknameExists");
        userRepository.saveAndFlush(user);

        // when
        boolean exists = userRepository.existsByNickname("nicknameExists");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    void deleteUserTest() {
        // given
        User user = createUser("delete@example.com", "password123", "deleteUser");
        userRepository.saveAndFlush(user);

        // when
        userRepository.delete(user);

        // then
        assertThat(userRepository.findByEmail("delete@example.com")).isEmpty();
    }

    @Test
    @DisplayName("중복된 이메일 저장 시 예외 발생 테스트")
    void duplicateEmailTest() {
        // given
        User user1 = createUser("duplicate@example.com", "password123", "user1");
        User user2 = createUser("duplicate@example.com", "password456", "user2");

        userRepository.saveAndFlush(user1);

        // when, then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("중복된 닉네임 저장 시 예외 발생 테스트")
    void duplicateNicknameTest() {
        // given
        User user1 = createUser("unique1@example.com", "password456", "duplicateNickname");
        User user2 = createUser("unique2@example.com", "password456", "duplicateNickname");

        userRepository.saveAndFlush(user1);

        // when, then
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
