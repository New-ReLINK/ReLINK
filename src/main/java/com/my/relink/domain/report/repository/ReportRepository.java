package com.my.relink.domain.report.repository;

import com.my.relink.domain.report.Report;
import com.my.relink.domain.report.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByEntityIdAndReportTypeAndTargetUserId(Long entityId, ReportType reportType, Long targetUserId);

    Optional<Report> findByEntityIdAndReportType(Long entityId, ReportType reportType);
}
