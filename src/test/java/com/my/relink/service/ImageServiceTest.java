package com.my.relink.service;

import com.my.relink.config.s3.S3Service;
import com.my.relink.controller.image.dto.resp.ImageUserProfileCreateRespDto;
import com.my.relink.controller.image.dto.resp.ImageUserProfileDeleteRespDto;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.image.Image;
import com.my.relink.domain.image.repository.ImageRepository;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("존재하지 않는 사용자 프로필 삭제 시도 시 Exception 발생")
    void userProfileNotFoundFailTest() {
        // given
        Long userId = 1L;
        Long imageId = 1L;

        when(imageRepository.findByIdAndEntityIdAndEntityType(userId, imageId, EntityType.USER)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> imageService.deleteUserProfile(userId, imageId));
        verify(imageRepository, times(1)).findByIdAndEntityIdAndEntityType(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 프로필 이미지 Entity ID 가 일치 하지 않을 때 Exception 발생")
    void userProfileNotOwnerFailTest() {
        // given
        Long userId = 1L;
        Long imageId = 1L;

        Image image = Image.builder()
                .id(imageId)
                .entityId(2L)
                .entityType(EntityType.USER)
                .build();

        when(imageRepository.findByIdAndEntityIdAndEntityType(imageId, userId, EntityType.USER)).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> imageService.deleteUserProfile(userId, imageId));
        verify(imageRepository, times(1)).findByIdAndEntityIdAndEntityType(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 프로필 이미지 정상 삭제")
    void userProfileDeleteSuccessTest() {
        // given
        Long userId = 1L;
        Long imageId = 1L;

        Image image = Image.builder()
                .id(imageId)
                .entityId(userId)
                .entityType(EntityType.USER)
                .build();

        when(imageRepository.findByIdAndEntityIdAndEntityType(imageId, userId, EntityType.USER)).thenReturn(Optional.of(image));
        doNothing().when(imageRepository).delete(image);
        doNothing().when(s3Service).deleteImage(image.getImageUrl());

        // when
        ImageUserProfileDeleteRespDto result = imageService.deleteUserProfile(userId, imageId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getImageId()).isEqualTo(imageId);
        verify(imageRepository, times(1)).findByIdAndEntityIdAndEntityType(any(), any(), any());
        verify(imageRepository, times(1)).delete(any());
        verify(s3Service, times(1)).deleteImage(any());
    }

    @Test
    @DisplayName("상품 이미지 등록 성공")
    void testAddExchangeItemImages_Success() {
        AtomicLong idGenerator = new AtomicLong(100);
        Long itemId = 1L;
        MultipartFile file1 = new MockMultipartFile("image1", "image1.png", "image/png", "mock1".getBytes());
        MultipartFile file2 = new MockMultipartFile("image2", "image2.png", "image/png", "mock2".getBytes());
        List<MultipartFile> files = List.of(file1, file2);

        when(imageRepository.countImages(itemId, EntityType.EXCHANGE_ITEM)).thenReturn(3);
        when(s3Service.upload(file1)).thenReturn("http://example.com/image1.png");
        when(s3Service.upload(file2)).thenReturn("http://example.com/image2.png");
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> {
            Image image = invocation.getArgument(0);
            return Image.builder()
                    .id(idGenerator.incrementAndGet())
                    .imageUrl(image.getImageUrl())
                    .entityId(image.getEntityId())
                    .entityType(image.getEntityType())
                    .build();
        });

        List<Long> response = imageService.addExchangeItemImage(itemId, files);

        assertEquals(2, response.size());
        assertTrue(response.contains(101L));
        assertTrue(response.contains(102L));
    }

    @Test
    @DisplayName("상품 이미지 등록 실패 - 이미 등록된 이미지가 업로드한 이미지 갯수의 합이 5개 초과")
    void testAddExchangeItemImages_Fail_MaxImageCount() {
        Long itemId = 1L;
        MultipartFile file1 = new MockMultipartFile("image1", "image1.png", "image/png", "mock1".getBytes());
        MultipartFile file2 = new MockMultipartFile("image2", "image2.png", "image/png", "mock2".getBytes());
        List<MultipartFile> files = List.of(file1, file2);

        when(imageRepository.countImages(itemId, EntityType.EXCHANGE_ITEM)).thenReturn(4);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageService.addExchangeItemImage(itemId, files)
        );

        assertEquals(ErrorCode.MAX_IMAGE_COUNT, exception.getErrorCode());
        assertEquals("사진은 총 5장까지 등록 가능합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("상품 이미지 등록 실패 - 업로드된 이미지가 없는 경우")
    void testAddExchangeItemImages_Fail_NotUploadImage () {
        Long itemId = 1L;
        List<MultipartFile> files = Collections.emptyList();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> imageService.addExchangeItemImage(itemId, files)
        );

        assertEquals(ErrorCode.NO_IMAGE_UPLOADED, exception.getErrorCode());
        assertEquals("사진이 업로드되지 않았습니다.", exception.getMessage());
    }

}