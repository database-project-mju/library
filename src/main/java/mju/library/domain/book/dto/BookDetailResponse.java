package mju.library.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.library.domain.book.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailResponse {

    private Long id;
    private String title;
    private String writer;
    private String publisher;
    private LocalDate publishDate;
    private Integer page;
    private Category category;
    private String description;
    private String imageUrl;

    private String lendStatus;   // “대출가능” or “대출중”
    private boolean canReserve;  // 예약 버튼 활성화 여부
    private boolean liked;       // 로그인 사용자의 찜 여부

    private LocalDateTime createdAt;
}
