package mju.library.domain.review;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.domain.review.dto.ReviewResDto;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/mypage/review")
    public String myReviewList(Model model,
                               @LoginMember Member loginMember,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {

        Long memberId = loginMember.getId();
        Page<ReviewResDto.ReviewDto> reviewPage = reviewService.getMyReviewList(memberId, page, size);
        model.addAttribute("memberName", loginMember.getName());

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalReviews", reviewPage.getTotalElements());

        return "mypage/reviewList";
    }
    @PostMapping("/mypage/review/delete")
    public String deleteSelectedReviews(
            @RequestParam("reviewIds") List<Long> reviewIds,
            @LoginMember Member loginMember
    ) {
        reviewService.deleteSelectedReviews(reviewIds, loginMember.getId());
        return "redirect:/mypage/review";
    }


}