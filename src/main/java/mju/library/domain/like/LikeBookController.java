package mju.library.domain.like;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/likeBook")
public class LikeBookController {

    private final LikeBookService likeBookService;

    // ❤️ 찜 추가
    @PostMapping("/{bookId}")
    public ResponseEntity likeBook(@PathVariable Long bookId,
                           @LoginMember Member member,
                           HttpServletRequest request) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        likeBookService.addLike(member, bookId);
        return ResponseEntity.ok().build();
    }

    // 🤍 찜 취소
    @DeleteMapping("/{bookId}")
    public ResponseEntity unlikeBook(@PathVariable Long bookId,
                             @LoginMember Member member,
                             HttpServletRequest request) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        likeBookService.removeLike(member, bookId);
        return ResponseEntity.ok().build();
    }
}
