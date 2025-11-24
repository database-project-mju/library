package mju.library.domain.lending;


import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LendingController {
    private final LendingService lendingService;

    @PostMapping("/lend/extend")
    public String extendLend(
            @LoginMember Member member,
            @RequestParam Long lendId) {
        lendingService.extendLend(lendId, member.getId());
        return "redirect:/mypage/lends";
    }



}
