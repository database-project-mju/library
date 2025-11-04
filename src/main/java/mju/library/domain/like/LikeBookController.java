package mju.library.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/likeBook")
public class LikeBookController {

    private final LikeBookService likeBookService;

    // ❤️ 찜 추가
    @PostMapping("/{bookId}")
    public String likeBook(@PathVariable Long bookId) {
        // TODO: 로그인 구현 후 실제 로그인된 memberId 가져오기
        Long memberId = 1L;
        likeBookService.addLike(memberId, bookId);
        return "redirect:/book/search";
    }

    // 🤍 찜 취소
    @DeleteMapping("/{bookId}")
    public String unlikeBook(@PathVariable Long bookId) {
        Long memberId = 1L;
        likeBookService.removeLike(memberId, bookId);
        return "redirect:/book/search";
    }
}
