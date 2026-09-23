package ru.practicum.shareit.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailDuplicateException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователя с ID = " + id + " не существует"
                ));

        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicateException("user с email " + userDto.getEmail() + " уже создан");
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {

        User user = UserMapper.toUser(userDto);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User с ID " + userId + " не найден"
                ));

        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            if (userRepository.existsByEmailAndIdNot(user.getEmail(), userId)) {
                throw new EmailDuplicateException(
                        "Пользователь с почтой " + user.getEmail() +
                                " уже зарегистрирован в системе"
                );
            }

            currentUser.setEmail(user.getEmail());
        }

        return UserMapper.toUserDto(currentUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                () -> new NotFoundException(
                        "User с id " + userId + " не найден"
                ));
        userRepository.delete(user);
    }
}