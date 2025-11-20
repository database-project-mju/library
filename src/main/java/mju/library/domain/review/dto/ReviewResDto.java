package mju.library.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ReviewResDto {


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private Long reviewId;
        private Long bookId;
        private String bookTitle; // 책 제목
        private String content;   // 리뷰 내용
        private LocalDate createdDate;
    }


}


