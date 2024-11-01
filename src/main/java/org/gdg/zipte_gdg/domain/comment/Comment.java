package org.gdg.zipte_gdg.domain.comment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gdg.zipte_gdg.domain.member.Member;
import org.gdg.zipte_gdg.domain.review.Review;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    // 로직
    public static Comment addNewComment(Review review, Member member, String content) {

        Comment comment = Comment.builder()
                .review(review)
                .member(member)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        member.addComment(comment);
        review.addComment(comment);

        return comment;
    }

    public void setReview(Review review) {
        this.review = review;
    }

}
