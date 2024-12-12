package com.my.relink.domain.report;


import com.my.relink.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportReason reportReason;

    @Column(nullable = false)
    private Long entityId;

    @Lob
    private String description;

    @Column(nullable = false)
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Builder
    public Report(Long id, ReportReason reportReason, Long entityId, String description, Long targetUserId, ReportType reportType) {
        this.id = id;
        this.reportReason = reportReason;
        this.entityId = entityId;
        this.description = description;
        this.targetUserId = targetUserId;
        this.reportType = reportType;
    }
}
