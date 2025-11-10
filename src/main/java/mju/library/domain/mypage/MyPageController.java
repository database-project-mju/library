package mju.library.domain.mypage;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.domain.review.Review;
import mju.library.domain.review.ReviewService;
import mju.library.global.auth.annotation.LoginMember; 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor // ReviewService를 자동으로 주입
public class MyPageController {

    private final ReviewService reviewService;

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
}
