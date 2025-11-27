package mju.library.domain.wish;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WishBookController {

    private final WishBookService wishBookService;

    // 1. 목록 조회 
    @GetMapping("/wish-book")
    public String list(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       @LoginMember Member member, Model model) {
        
        Page<WishBook> wishPage = wishBookService.getWishBookList(pageable);
        model.addAttribute("wishPage", wishPage);
        
        if (member != null) {
            model.addAttribute("memberName", member.getName());
        }
        return "wish/list";
    }

    // 2. 신청 폼 
    @GetMapping("/wish-book/new")
    public String requestForm(@LoginMember Member member, Model model) {
        if (member == null) {
            return "redirect:/login";
        }
        model.addAttribute("memberName", member.getName());
        return "wish/form";
    }

    // 3. 신청 처리
    @PostMapping("/wish-book/new")
    public String processRequest(@LoginMember Member member,
                                 @RequestParam String title,
                                 @RequestParam String author,
                                 @RequestParam String url) { // publisher, reason 삭제 -> url 추가
        if (member == null) {
            return "redirect:/login";
        }
        // 서비스 호출 시 url 전달
        wishBookService.requestWishBook(member.getId(), title, author, url);
        return "redirect:/wish-book";
    }

    // 4. 기부 신청 버튼 처리
    @PostMapping("/wish-book/donate")
    public String donateBook(@RequestParam Long id, 
                             @RequestParam String donorName,
                             @RequestParam String donorPhone,
                             @RequestParam String deliveryUrl) {
        
        // 로그인 체크 없이 바로 서비스 호출
        wishBookService.applyDonation(id, donorName, donorPhone, deliveryUrl);
        
        return "redirect:/wish-book";
    }
}