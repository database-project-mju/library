package mju.library.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class LikeResDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeListDto {
        private int totalCount;
        private int totalPage;
        private int currentPage;
        private List<LikeResDto.LikeDto> likeList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeDto {
        private Long likeId;
        private Long bookId;
        private String bookName;
        private String bookAuthor;
        private String publisher;
        private String imageUrl;
        private LocalDate publishDate;
        private String description;

    }
}
