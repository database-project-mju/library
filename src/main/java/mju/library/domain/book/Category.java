package mju.library.domain.book;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Category {

    HUMANITIES("인문"),
    SOCIAL_SCIENCES("사회"),
    SCIENCE_ENGINEERING("과학/공학"),
    RELIGION("종교"),
    FOREIGN_BOOKS("외국도서"),
    TEXTBOOK_EXAM("교재/수험서"),
    ARTS("예술");

    private final String korean; // 한글 표시

    // Enum 이름(영어) → 한글 반환
    public static String getKoreanByName(String name) {
        if (name == null) return null;
        return Arrays.stream(values())
                .filter(c -> c.name().equalsIgnoreCase(name))
                .map(Category::getKorean)
                .findFirst()
                .orElse(null);
    }
}
