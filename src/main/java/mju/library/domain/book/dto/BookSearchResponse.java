package mju.library.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {
    private Long id;
    private String title;
    private String writer;
    private String publisher;
    private LocalDate publishDate;
    private String imageUrl;
    private String description;
    private String lendStatus; // “대출가능” or “대출중”
    private boolean canReserve; // 예약 가능 여부
    private boolean liked;     // 찜 여부
}
