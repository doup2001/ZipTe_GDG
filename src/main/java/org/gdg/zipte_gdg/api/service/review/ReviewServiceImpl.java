package org.gdg.zipte_gdg.api.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.gdg.zipte_gdg.api.controller.page.request.PageRequestDto;
import org.gdg.zipte_gdg.api.controller.review.request.ReviewRequestDto;
import org.gdg.zipte_gdg.api.service.comment.response.CommentResponseWithReviewDto;
import org.gdg.zipte_gdg.api.service.page.response.PageResponseDto;
import org.gdg.zipte_gdg.api.service.review.response.ReviewResponseDto;
import org.gdg.zipte_gdg.api.service.review.response.ReviewResponseWithCommentDto;
import org.gdg.zipte_gdg.domain.comment.Comment;
import org.gdg.zipte_gdg.domain.member.Member;
import org.gdg.zipte_gdg.domain.member.MemberRepository;
import org.gdg.zipte_gdg.domain.review.Review;
import org.gdg.zipte_gdg.domain.review.ReviewImage;
import org.gdg.zipte_gdg.domain.review.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ReviewImageService reviewImageService;

    @Override
    public ReviewResponseDto register(ReviewRequestDto reviewRequestDto) {
        Member member = getMember(reviewRequestDto);
        Review review = Review.addNewReview(member, reviewRequestDto.getTitle(), reviewRequestDto.getContent());

        Review savedReview = reviewRepository.save(review);

        List<String> uploads = reviewImageService.saveFiles(savedReview, reviewRequestDto.getFiles());


        ReviewResponseDto reviewResponseDto = entityToDto(savedReview);
        reviewResponseDto.setUploadFileNames(uploads);

        return reviewResponseDto;
    }

    private Member getMember(ReviewRequestDto reviewRequestDto) {
        Optional<Member> byId = memberRepository.findById(reviewRequestDto.getMemberId());
        return byId.orElseThrow();
    }

    @Override
    public ReviewResponseWithCommentDto getOne(Long reviewId) {

        Review review = reviewRepository.findById(reviewId).orElseThrow();

        ReviewResponseWithCommentDto reviewResponseDto = entityToCommentDto(review);

        List<Comment> commentsWithReview = reviewRepository.findCommentsWithReview(reviewId);
        List<ReviewImage> reviewImages = reviewRepository.selectReviewImages(reviewId);


        // 댓글을 DTO로 변환합니다.
        List<CommentResponseWithReviewDto> commentResponseDtos = commentsWithReview.stream()
                .map(this::commentEntityToDto) // 댓글 엔티티를 DTO로 변환하는 메서드를 호출합니다.
                .collect(Collectors.toList());

        // 리뷰 DTO에 댓글을 설정합니다.
        reviewResponseDto.setComments(commentResponseDtos);
        reviewResponseDto.setUploadFileNames(reviewImages.stream().map(ReviewImage::getFileName).collect(Collectors.toList()));

        return reviewResponseDto;
    }

    private CommentResponseWithReviewDto commentEntityToDto(Comment comment) {
        return CommentResponseWithReviewDto.builder()
                .id(comment.getId())
                .memberId(comment.getMember().getId())
                .author(comment.getMember().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Override
    public PageResponseDto<ReviewResponseDto> getList(PageRequestDto pageRequestDto) {
        log.info("=== getList ===");

        Pageable pageable = PageRequest.of(pageRequestDto.getPage()-1, pageRequestDto.getSize(), Sort.by("id").descending());

        Page<Object[]> result = reviewRepository.selectList(pageable);
        log.info(result);


        List<ReviewResponseDto> dtoList = result.get().map(arr -> {
            Review review = (Review) arr[0];
            ReviewImage reviewImage = (ReviewImage) arr[1];

            String imageStr = (reviewImage != null) ? reviewImage.getFileName() : "No image found";
            ReviewResponseDto dto = entityToDto(review);
            dto.setUploadFileNames(Collections.singletonList(imageStr));

            return dto;
        }).toList();

        long total = result.getTotalElements();

        return new PageResponseDto<ReviewResponseDto>(dtoList, pageRequestDto, total);
    }



    @Override
    public PageResponseDto<ReviewResponseDto> getReviewsByMemberId(PageRequestDto pageRequestDto, Long memberId) {
        log.info("=== getListById ===");

        Pageable pageable = PageRequest.of(pageRequestDto.getPage() - 1, pageRequestDto.getSize(), Sort.by("id").descending());

        Page<Review> result = reviewRepository.findReviewsByMemberId(memberId, pageable);

        log.info(result);
        // Review 엔티티를 ReviewResponseDto로 변환
        List<ReviewResponseDto> dtoList = result.stream()
                .map(this::entityToDto)  // entityToDto 메서드 사용
                .collect(Collectors.toList());

        dtoList.forEach(dto->{


            ReviewImage reviewImage = reviewRepository.selectReviewImagesthumbnail(dto.getId());

            String imageStr = (reviewImage != null) ? reviewImage.getFileName() : "No image found";

            dto.setUploadFileNames(Collections.singletonList(imageStr));
        });

        long total = result.getTotalElements();

        return new PageResponseDto<>(dtoList, pageRequestDto, total);
    }

}
