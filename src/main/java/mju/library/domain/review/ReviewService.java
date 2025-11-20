package mju.library.domain.review;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.review.dto.ReviewResDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    
    // (C) 리뷰 생성
    public void createReview(Long memberId, Long bookId, String reviewText) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다."));
        
        Member member = Member.builder().id(memberId).build(); 

        Review review = Review.builder()
                .member(member)
                .book(book)
                .reviewText(reviewText)
                // [ ✨ 'rating' 빌더 삭제됨 ✨ ]
                .build();
        
        reviewRepository.save(review);
    }

    // (U) 리뷰 수정 (권한 검사)
    public void updateReview(Long memberId, Long reviewId, String reviewText) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!Objects.equals(review.getMember().getId(), memberId)) {
            throw new IllegalArgumentException("리뷰를 수정할 권한이 없습니다.");
        }

        review.updateReview(reviewText); 
    }

    // (D) 리뷰 삭제 
    public void deleteReview(Long memberId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!Objects.equals(review.getMember().getId(), memberId)) {
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    // (D) 관리자용 리뷰 삭제 
    public void adminDeleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    
    // (R) 읽기
    @Transactional(readOnly = true)
    public List<Review> getReviewsByBook(Long bookId) {
        Book book = Book.builder().id(bookId).build();
        return reviewRepository.findByBook(book);
    }
    
    @Transactional(readOnly = true)
    public List<Review> getReviewsByMember(Long memberId) {
        Member member = Member.builder().id(memberId).build();
        return reviewRepository.findByMember(member);
    }

    // (R) 관리자용 모든 리뷰 페이징 조회
    @Transactional(readOnly = true) 
    public Page<Review> findAllReviews(Pageable pageable) {
        return reviewRepository.findAllWithMemberAndBook(pageable);
    }

    // (R) 관리자 대시보드용 '총 리뷰 수' 조회
    @Transactional(readOnly = true)
    public long getReviewCount() {
        return reviewRepository.count();
    }


    @Transactional(readOnly = true)
    public Page<ReviewResDto.ReviewDto> getMyReviewList(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return reviewRepository.findByMemberId(memberId, pageable)
                .map(review -> ReviewResDto.ReviewDto.builder()
                        .reviewId(review.getId())
                        .bookId(review.getBook().getId())
                        .bookTitle(review.getBook().getTitle())
                        .content(review.getReviewText())
                        .createdDate(review.getCreatedAt().toLocalDate())
                        .build());
    }

    @Transactional
    public void deleteSelectedReviews(List<Long> reviewIds, Long memberId) {
        // 본인 리뷰만 삭제하도록 검증 (권한 문제 방지)
        List<Review> reviews = reviewRepository.findAllById(reviewIds);

        for (Review review : reviews) {
            if (!review.getMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
            }
        }

        reviewRepository.deleteAll(reviews);
    }
}