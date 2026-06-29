package com.nokcha.efbe.domain.admin.errorLog.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.errorLog.dto.response.AdminErrorLogDetailRspDto;
import com.nokcha.efbe.domain.admin.errorLog.dto.response.AdminErrorLogItemRspDto;
import com.nokcha.efbe.domain.admin.errorLog.dto.response.AdminErrorLogPageRspDto;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import com.nokcha.efbe.domain.errorLog.entity.SystemErrorLog;
import com.nokcha.efbe.domain.errorLog.repository.SystemErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminErrorLogService {

    private final SystemErrorLogRepository systemErrorLogRepository;

    @Transactional(readOnly = true)
    public AdminErrorLogPageRspDto getErrorLogs(ErrorSource source, ErrorSeverity severity, String errorType,
                                                Long userId, Long adminId, Boolean resolved,
                                                LocalDateTime from, LocalDateTime to,
                                                int page, int size) {
        String kw = (errorType == null || errorType.isBlank()) ? null : errorType.trim();
        // 정렬은 @Query 의 order by 가 담당하므로 Pageable 에는 sort 미지정.
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        Page<SystemErrorLog> result = systemErrorLogRepository.search(
                source, severity, kw, userId, adminId, resolved, from, to, pageable);

        return AdminErrorLogPageRspDto.builder()
                .content(result.getContent().stream().map(AdminErrorLogItemRspDto::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminErrorLogDetailRspDto getErrorLog(Long id) {
        SystemErrorLog e = systemErrorLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR_LOG));
        return AdminErrorLogDetailRspDto.from(e);
    }

    @Transactional
    public AdminErrorLogDetailRspDto resolveErrorLog(Long id) {
        SystemErrorLog e = systemErrorLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR_LOG));
        e.resolve(LocalDateTime.now());
        return AdminErrorLogDetailRspDto.from(e);
    }
}
