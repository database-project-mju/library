package mju.library.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class LendingResDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LendingListDto {
        private int totalCount;
        private int totalPage;
        private int currentPage;
        private List<LendingDto> lendingList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LendingDto {
        private Long lendId;
        private Long bookId;
        private String bookName;
        private String bookAuthor;
        private String publisher;
        private String imageUrl;
        private LocalDate publishDate;
        private LocalDate lendDate;
        private LocalDate dueDate;
    }
}
