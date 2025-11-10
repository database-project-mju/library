package mju.library.domain.review;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // (C) 리뷰 생성
    @PostMapping("/book/{bookId}/reviews")
    public String createReview(@PathVariable Long bookId,
                               @LoginMember Member loginMember,
                               @RequestParam String reviewText) { 
        
        reviewService.createReview(loginMember.getId(), bookId, reviewText); 
        
        return "redirect:/book/" + bookId; 
    }

    // (U) 리뷰 수정
    @PostMapping("/reviews/{reviewId}/update")
    public String updateReview(@PathVariable Long reviewId,
                               @LoginMember Member loginMember,
                               @RequestParam String reviewText) { 
        
        reviewService.updateReview(loginMember.getId(), reviewId, reviewText); 
        
        return "redirect:/mypage/reviews"; 
    }

    // (D) 리뷰 삭제 
    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
                               @LoginMember Member loginMember) {
        
        reviewService.deleteReview(loginMember.getId(), reviewId);

        return "redirect:/mypage/reviews"; 
    }
}