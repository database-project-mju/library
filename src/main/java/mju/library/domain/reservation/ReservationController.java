package mju.library.domain.reservation;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{bookId}")
    @ResponseBody
    public ResponseEntity<String> reserveBook(@PathVariable Long bookId,
                                              @LoginMember Member member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }

        try {
            reservationService.reserveBook(member, bookId);
            return ResponseEntity.ok("예약이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{bookId}")
    @ResponseBody
    public ResponseEntity<String> cancelReservation(@PathVariable Long bookId,
                                                    @LoginMember Member member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용해주세요.");
        }

        try {
            reservationService.cancelReservation(member, bookId);
            return ResponseEntity.ok("예약이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("")
    public String cancelReserveWithRedirect(
            @RequestParam Long bookId,
            @LoginMember Member loginMember,
            RedirectAttributes redirect
    ) {
        try {
            reservationService.cancelReservation(loginMember,bookId);
            redirect.addFlashAttribute("success", "대출이 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mypage/reserve";
    }
}

