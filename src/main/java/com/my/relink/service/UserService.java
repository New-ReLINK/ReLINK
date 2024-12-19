package com.my.relink.service;

import com.my.relink.controller.user.dto.req.*;
import com.my.relink.controller.user.dto.resp.*;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.domain.item.exchange.repository.ExchangeItemRepository;
import com.my.relink.domain.point.Point;
import com.my.relink.domain.point.repository.PointRepository;
import com.my.relink.domain.review.repository.ReviewRepository;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.TradeRepository;
import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.domain.user.repository.dto.UserInfoWithCountRepositoryDto;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;
    private final PointRepository pointRepository;
    private final ReviewRepository reviewRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final TradeRepository tradeRepository;

    public User findByIdOrFail(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }


    public UserCreateRespDto register(UserCreateReqDto dto) {
        dto.changePassword(passwordEncoder.encode(dto.getPassword()));
        User savedUser = userRepository.save(dto.toEntity(dto));

        Point point = Point.builder().amount(0).user(savedUser).build();
        pointRepository.save(point);

        return new UserCreateRespDto(savedUser.getId());
    }

    public UserInfoRespDto findUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Image image = imageRepository.findByEntityIdAndEntityType(user.getId(), EntityType.USER).orElse(null);

        return new UserInfoRespDto(user, image);
    }

    public UserPointRespDto findUserPoint(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Point point = pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));

        return new UserPointRespDto(point.getAmount());
    }

    public UserAddressRespDto findAddress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserAddressRespDto(user.getAddress());
    }

    public void userInfoEdit(Long userId, UserInfoEditReqDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.changeInfo(dto.getName(), dto.getNickname());
        userRepository.save(user);
    }

    public void deleteUser(Long userId, UserDeleteReqDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        delayDeleteUser(user);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.MISS_MATCHER_PASSWORD);
        }

        user.changeIsDeleted();
        userRepository.save(user);
        exchangeItemRepository.updateTradeStatusToUnavailable(user.getId());
    }

    public UserValidNicknameRespDto validNickname(UserValidNicknameRepDto dto) {
        return new UserValidNicknameRespDto(userRepository.findByNickname(dto.getNickname()).isPresent());
    }

    public UserValidEmailRespDto validEmail(UserValidEmailReqDto dto) {
        return new UserValidEmailRespDto(userRepository.findByEmail(dto.getEmail()).isPresent());
    }

    public UserProfileRespDto getUserProfile(Long userId) {
        UserInfoWithCountRepositoryDto repositoryDto = userRepository.findUserDetailInfo(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Double avgStar = reviewRepository.getTotalStarAvg(userId);

        return new UserProfileRespDto(avgStar, repositoryDto);
    }

    public void delayDeleteUser(User currentUser) {
        boolean hasActiveTrades = tradeRepository.existsByRequesterIdAndTradeStatus(currentUser.getId(), TradeStatus.IN_EXCHANGE) ||
                        tradeRepository.existsByRequesterIdAndTradeStatus(currentUser.getId(), TradeStatus.IN_DELIVERY);
        if (hasActiveTrades) {
            throw new BusinessException(ErrorCode.ACTIVE_TRADE_EXISTS);
        }
    }
}
