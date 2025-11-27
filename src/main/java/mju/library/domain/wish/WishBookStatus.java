package mju.library.domain.wish;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WishBookStatus {
    APPLIED("신청완료"),       // 처음 신청했을 때
    DONATION("기부신청됨"),    // 누군가 기부하겠다고 버튼을 눌렀을 때
    SHIPPING("배송중"),        // (관리자 처리) 책이 오고 있음
    STOCKED("입고완료");       // (관리자 처리) 도서관에 등록됨

    private final String korean;
}
