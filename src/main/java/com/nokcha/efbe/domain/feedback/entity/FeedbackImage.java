package com.nokcha.efbe.domain.feedback.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "feedback_image",
        indexes = {
                @Index(name = "idx_feedback_image_feedback", columnList = "feedback_id, sort_order")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_image_feedback"))
    private Feedback feedback;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Builder
    private FeedbackImage(Feedback feedback, String originalName, String storedName,
                          Integer sortOrder, String url) {
        this.feedback = feedback;
        this.originalName = originalName;
        this.storedName = storedName;
        this.sortOrder = sortOrder;
        this.url = url;
    }
}
