package com.my.relink.domain.image;

import com.my.relink.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Image extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 255)
    private String imageUrl;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Builder
    public Image(Long id, String imageUrl, Long entityId, EntityType entityType) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.entityId = entityId;
        this.entityType = entityType;
    }
}
