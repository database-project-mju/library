package mju.library.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class ReturnResDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnListDto {
        private int totalCount;
        private int totalPage;
        private int currentPage;
        private List<ReturnResDto.ReturnDto> returnList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnDto {
        private Long lendId;
        private Long bookId;
        private String bookName;
        private String bookAuthor;
        private String publisher;
        private String imageUrl;
        private LocalDate publishDate;
        private Long lendCount;
        private LocalDate lendDate;
    }
}
