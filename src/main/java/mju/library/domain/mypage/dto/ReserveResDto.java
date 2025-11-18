package mju.library.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class ReserveResDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveListDto {
        private int totalCount;
        private int totalPage;
        private int currentPage;
        private List<ReserveResDto.ReserveDto> reserveList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveDto {
        private Long reserveId;
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
