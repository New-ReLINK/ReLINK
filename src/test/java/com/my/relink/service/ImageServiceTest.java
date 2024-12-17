package com.my.relink.service;

import com.my.relink.config.s3.S3Service;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.ex.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ImageService imageService;

    @Mock
    private MultipartFile mockFile;

    @Test
    @DisplayName("사용자 프로필 사진을 등록할 때 프로필 사진이 이미 존재할 때 Exception 이 발생한다.")
    void alreadyUserProfileFailTest() {
        // given
        Long userId = 1L;

        Image image = Image.builder()
                .id(1L)
                .entityId(userId)
                .imageUrl("test/test.jpg")
                .entityType(EntityType.USER)
                .build();

        when(imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(userId, EntityType.USER))
                .thenReturn(Optional.of(image));

        // when & then
        assertThrows(BusinessException.class, () -> imageService.addUserProfile(userId, mockFile));
        verify(imageRepository, times(1))
                .findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(any(), any());
    }

    @Test
    @DisplayName("사용자 프로필 이미지 S3 이미지 등록 성공 테스트")
    void registerUserProfileSuccessTest() {
        // given
        Long userId = 1L;

        String imageUrl = "test/test.jpg";

        Image image = Image.builder()
                .id(1L)
                .entityId(userId)
                .imageUrl("test/test.jpg")
                .entityType(EntityType.USER)
                .build();

        when(imageRepository.findTopByEntityIdAndEntityTypeOrderByCreatedAtAsc(userId, EntityType.USER))
                .thenReturn(Optional.empty());
        when(s3Service.upload(mockFile)).thenReturn(imageUrl);
        when(imageRepository.save(any())).thenReturn(image);

        // when
        ImageUserProfileCreateRespDto result = imageService.addUserProfile(userId, mockFile);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
    }
}