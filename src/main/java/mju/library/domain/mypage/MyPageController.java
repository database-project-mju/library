package mju.library.domain.mypage;

import lombok.RequiredArgsConstructor;
import mju.library.domain.lending.Lending;
import mju.library.domain.lending.LendingService;
import mju.library.domain.like.LikeBookService;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberService;
import mju.library.domain.mypage.dto.LendingResDto;
import mju.library.domain.mypage.dto.LikeResDto;
import mju.library.domain.mypage.dto.ReserveResDto;
import mju.library.domain.reservation.ReservationService;
import mju.library.domain.review.Review;
import mju.library.domain.review.ReviewService;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor // ReviewService를 자동으로 주입
public class MyPageController {

    private final ReviewService reviewService;
    private final LendingService lendingService;
    private final MemberService memberService;
    private final ReservationService reservationService;
    private final LikeBookService likeBookService;

    /**
     * (R) 마이페이지 - 내가 쓴 리뷰 조회
     */
    @GetMapping("/mypage/reviews")
    public String getMyReviewPage(@LoginMember Member loginMember, Model model) {

        // 1. 로그인한 회원 ID로 '내가 쓴 리뷰' 목록을 DB에서 조회
        List<Review> myReviews = reviewService.getReviewsByMember(loginMember.getId());

        // 2. HTML로 "myReviews"라는 이름표를 붙여 데이터 전달
        model.addAttribute("myReviews", myReviews);

        // 3. mypage/reviews.html 파일을 찾아서 보여줌
        return "mypage/reviews";
    }



    /**
     * 대출 목록 조회
     */
    @GetMapping("mypage/lends")
    public String borrowList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @LoginMember Member member,
            Model model
    ) {
        model.addAttribute("memberName", member.getName());

        // ⭐ 서비스에서 DTO 완성해서 가져옴
        LendingResDto.LendingListDto lendingListDto =
                lendingService.getLendList(member.getId(), page, size);

        model.addAttribute("lendingListDto", lendingListDto);

        return "mypage/lends";
    }    /**
     * 예약 목록 조회
     */
    @GetMapping("mypage/reserve")
    public String reserveList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @LoginMember Member member,
            Model model
    ) {
        model.addAttribute("memberName", member.getName());

        // ⭐ 서비스에서 DTO 완성해서 가져옴
        ReserveResDto.ReserveListDto reserveListDto = reservationService.getReserveList(member.getId(), page, size);

        model.addAttribute("reserveListDto", reserveListDto);

        return "mypage/reserve";
    }


/**
     * 찜 목록 조회
     */
    @GetMapping("mypage/like")
    public String likeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @LoginMember Member member,
            Model model
    ) {
        model.addAttribute("memberName", member.getName());

        // ⭐ 서비스에서 DTO 완성해서 가져옴
        LikeResDto.LikeListDto likeListDto = likeBookService.getLikeList(member.getId(), page, size);

        model.addAttribute("likeListDto", likeListDto);

        return "mypage/like";
    }



    @GetMapping("mypage/myInfo")
    public String myPage(
            @LoginMember Member loginMember,
            Model model) {
        model.addAttribute("memberName", loginMember.getName());
        model.addAttribute("member",  memberService.getMemberInfo(loginMember.getId()));
        return "mypage/myInfo";
    }

    @PostMapping("/mypage/myInfo/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @LoginMember Member loginMember,
                                 RedirectAttributes redirect) {
        // 1) 새 비밀번호 두 개 일치 여부 → 컨트롤러에서 처리
        if (!newPassword.equals(confirmPassword)) {
            redirect.addFlashAttribute("error", "새 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage/myInfo";
        }


        try {
            // 2) 서비스 호출 (현재 비밀번호 검증 + 변경)
            memberService.changePassword(currentPassword, newPassword, loginMember.getId());

            redirect.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다!");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mypage/myInfo";
    }

}
