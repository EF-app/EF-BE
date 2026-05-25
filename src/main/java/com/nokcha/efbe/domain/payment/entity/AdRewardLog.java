package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "ad_reward_log",
        uniqueConstraints = {@UniqueConstraint(name = "uk_ad_reward_tx", columnNames = "ad_tx_id")},
        indexes = {@Index(name = "idx_ad_reward_user_date", columnList = "user_id, reward_date, reward_type")})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdRewardLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reward_type", nullable = false, length = 40)
    private String rewardType;

    @Column(name = "reward_amount", nullable = false)
    private Integer rewardAmount = 1;

    @Column(name = "reward_date", nullable = false)
    private LocalDate rewardDate;

    @Column(name = "ad_network", length = 30)
    private String adNetwork;

    @Column(name = "ad_tx_id", length = 100)
    private String adTxId;

    @Builder
    private AdRewardLog(Long userId, String rewardType, Integer rewardAmount,
                        LocalDate rewardDate, String adNetwork, String adTxId) {
        this.userId = userId;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount == null ? 1 : rewardAmount;
        this.rewardDate = rewardDate;
        this.adNetwork = adNetwork;
        this.adTxId = adTxId;
    }
}
