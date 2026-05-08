package com.nokcha.efbe.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@NoArgsConstructor
public class BaseEntity {
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time", nullable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @CreatedBy
    @Column(updatable = false, name = "create_user")
    private Long createUser;

    @LastModifiedBy
    @Column(nullable = false, name = "update_user",
            columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long updateUser;
}
