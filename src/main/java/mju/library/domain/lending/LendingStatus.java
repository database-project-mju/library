package mju.library.domain.lending;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LendingStatus {

    BORROWED("대출중"),
    RETURNED("반납완료"),
    OVERDUE("연체");

    private final String korean; // 한글 표시

    // Enum 이름(영어) → 한글 반환
    public static String getKoreanByName(String name) {
        if (name == null) return null;
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(name))
                .map(LendingStatus::getKorean)
                .findFirst()
                .orElse(null);
    }

}
