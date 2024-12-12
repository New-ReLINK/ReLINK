package com.my.relink.domain.report.repository;

import com.my.relink.domain.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
