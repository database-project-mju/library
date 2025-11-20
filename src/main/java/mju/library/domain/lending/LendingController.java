package mju.library.domain.lending;


import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LendingController {
    private final LendingService lendingService;

    @DeleteMapping("/lend")
    public String cancelLend(
            @RequestParam Long lendId,
            @LoginMember Member loginMember,
            RedirectAttributes redirect
    ) {
        try {
            lendingService.cancelLend(loginMember.getId(), lendId);
            redirect.addFlashAttribute("success", "대출이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage/lends";
    }


}
