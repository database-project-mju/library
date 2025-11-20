package mju.library.domain.admin;

import mju.library.domain.book.Book;
import mju.library.domain.book.BookService;
import mju.library.domain.book.Category;
import mju.library.domain.lending.Lending;
import mju.library.domain.lending.LendingService;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRole;
import mju.library.domain.member.MemberService;
import mju.library.domain.review.Review; 
import mju.library.domain.review.ReviewService; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; 
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

@Controller
public class AdminController {

    // MemberService, ReviewService 연결
    private final MemberService memberService;
    private final ReviewService reviewService;
    private final LendingService lendingService;
    private final BookService bookService;

    public AdminController(MemberService memberService, ReviewService reviewService, LendingService lendingService, BookService bookService) {
        this.memberService = memberService;
        this.reviewService = reviewService;
        this.lendingService = lendingService;
        this.bookService = bookService;
    }

    @GetMapping("/admin")
    public String adminHome(Model model) { 
        
        long overdueCount = lendingService.getOverdueCount();
        long memberCount = memberService.getMemberCount();
        long bookCount = bookService.getBookCount();
        long reviewCount = reviewService.getReviewCount();
        
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("memberCount", memberCount);
        model.addAttribute("bookCount", bookCount);
        model.addAttribute("reviewCount", reviewCount);
        
        return "admin/home";
    }

    // '학생 조회' 페이지 메서드 추가 
    @GetMapping("/admin/members")
    public String getMemberListPage(@RequestParam(value = "keyword", required = false) String keyword,
                                    Pageable pageable, 
                                    Model model) {
        
        // Service를 호출해 학생 목록을 가져옴
        Page<Member> membersPage = memberService.findMembers(keyword, pageable);
        
        // HTML로 데이터를 보냄
        model.addAttribute("membersPage", membersPage);
        model.addAttribute("keyword", keyword);
        
        return "admin/member-list"; // → templates/admin/member-list.html
    }

    // (R) 학생 추가
    @GetMapping("/admin/members/new")
    public String getMemberAddForm() {
        return "admin/member-add-form"; // → templates/admin/member-add-form.html
    }

    // (C) '학생 추가 폼' 데이터를 받아서 처리
    @PostMapping("/admin/members/create")
    public String createMember(@RequestParam String studentNo,
                               @RequestParam String name,
                               @RequestParam String password,
                               @RequestParam MemberRole memberRole) {

        memberService.createMember(studentNo, name, password, memberRole);

        return "redirect:/admin/members"; // 학생 목록으로 새로고침
    }

    // TODO: 학생 삭제 시 데이터 무결성 문제 발생 (논의 후 추가)

    @GetMapping("/admin/reviews")
    public String getReviewListPage(
            // (PageableDefault: 기본 정렬값 설정 - 10개씩, 최신순)
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, 
            Model model) {
        
        Page<Review> reviewPage = reviewService.findAllReviews(pageable);
        
        model.addAttribute("reviewPage", reviewPage);
        
        return "admin/review-list"; // admin/review-list.html 호출
    }

    // (D) 관리자 리뷰 삭제
    @PostMapping("/admin/reviews/{reviewId}/delete")
    public String deleteReviewByAdmin(@PathVariable Long reviewId) {
        
        reviewService.adminDeleteReview(reviewId);
        
        return "redirect:/admin/reviews"; 
    }

    // (R) '대출/반납' 페이지 조회 
    @GetMapping("/admin/lending")
    public String getLendingPage(@RequestParam(value = "studentNo", required = false) String studentNo,
                                 Model model) {

        // 학번 검색어가 있을 경우
        if (studentNo != null && !studentNo.trim().isEmpty()) {
            try {
                // 학생의 현재 대출 목록 조회
                List<Lending> activeLoans = lendingService.findActiveLoansByMember(studentNo);
                model.addAttribute("activeLoans", activeLoans); // 대출 목록
                model.addAttribute("searchedStudentNo", studentNo); // 검색한 학번
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage()); // "존재하지 않는 학번"
            }
        }

        return "admin/lending-return";
    }

    // (C) '대출 처리' 기능 
    @PostMapping("/admin/checkout")
    public String checkOut(@RequestParam String studentNo, @RequestParam String bookIdentifier, RedirectAttributes redirectAttributes) {

        try {
            lendingService.checkOut(studentNo, bookIdentifier);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("checkoutError", e.getMessage());
        }

        return "redirect:/admin/lending?studentNo=" + studentNo;
    }

    // (U) '반납 처리' 기능 
    @PostMapping("/admin/checkin")
    public String checkIn(@RequestParam Long lendingId, 
                          @RequestParam String studentNo,
                          RedirectAttributes redirectAttributes) { 
        
        // 5. Service로부터 '다음 예약자 정보'를 받음
        Optional<Member> nextMemberOpt = lendingService.checkIn(lendingId);
        
        // 6. 만약 다음 예약자가 존재하면 (ifPresent)
        nextMemberOpt.ifPresent(member -> {
            // 7. 팝업으로 띄울 메시지를 만듦
            String popupMessage = String.format(
                "반납이 완료되었습니다.\n다음 예약자: %s (%s)",
                member.getName(),
                member.getStudentNo()
            );
            // 8. '임시 가방'에 팝업 메시지를 담아서 HTML로 보냄
            redirectAttributes.addFlashAttribute("popupMessage", popupMessage);
        });
        
        // 9. 학생 대출 페이지로 새로고침
        return "redirect:/admin/lending?studentNo=" + studentNo;
    }

    // (R) '도서 조회' 페이지 추가 
    @GetMapping("/admin/books")
    public String getBookListPage(@RequestParam(value = "keyword", required = false) String keyword,
                                  @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                  Model model) {
        
        Page<Book> bookPage = bookService.adminSearchBooks(keyword, pageable);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("keyword", keyword);

        return "admin/book-list"; 
    }

    // (C) '도서 추가 폼' 페이지 추가
    @GetMapping("/admin/books/new")
    public String getBookAddForm(Model model) {
        model.addAttribute("categories", Category.values());
        return "admin/book-add-form"; 
    }

    // (C) '도서 추가' (수동 폼) 처리 
    @PostMapping("/admin/books/create")
    public String createBook(@RequestParam String isbn,
                             @RequestParam String title,
                             @RequestParam String writer,
                             @RequestParam String publisher,
                             @RequestParam LocalDate publishDate,
                             @RequestParam String description,
                             @RequestParam Integer page,
                             @RequestParam String imageUrl,
                             @RequestParam Category category) {
        
        try {
            bookService.createBook(isbn, title, writer, publisher, publishDate, description, page, imageUrl, category);
        } catch (Exception e) {
            // TODO: 에러 메시지를 FlashAttribute로 전달하여 사용자에게 보여주기
            System.out.println(e.getMessage());
        }
        
        return "redirect:/admin/books"; 
    }

    //  (R) '연체 현황' 페이지 조회
    @GetMapping("/admin/lending/overdue")
    public String getOverdueListPage(
            // (반납기한이 오래된 순 = 오름차순)
            @PageableDefault(size = 10, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable,
            Model model) {
        
        Page<Lending> overduePage = lendingService.findOverdueLoans(pageable);
        
        model.addAttribute("overduePage", overduePage);
        
        return "admin/overdue-list"; // → templates/admin/overdue-list.html
    }
}