package mju.library.domain.book;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.dto.BookDetailResponse;
import mju.library.domain.book.dto.MainPageBooksDto;
import mju.library.domain.member.Member;
import mju.library.global.auth.annotation.LoginMember;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import mju.library.domain.book.dto.BookSearchResponse;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * 메인 페이지 렌더링
     * - 최근 도서 8권
     * - 인기 도서 (좋아요순 4권, 대출순 4권)
     */
    @GetMapping("/")
    public String home(@LoginMember Member member, Model model) {
        MainPageBooksDto mainBooks = bookService.getMainPageBooks();

        model.addAttribute("recentBooks", mainBooks.getRecentBooks());
        model.addAttribute("popularByLikes", mainBooks.getPopularByLikes());
        model.addAttribute("popularByLending", mainBooks.getPopularByLending());

        // 로그인된 사용자 정보 전달
        if (member != null) {
            model.addAttribute("memberName", member.getName());
        }

        return "home"; // templates/home.html
    }

    /**
     * 도서 검색 기능
     */
    @GetMapping("/book/search")
    public String searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,   // ✅ 추가
            @RequestParam(defaultValue = "0") int page,
            @LoginMember Member member,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 8);

        Page<BookSearchResponse> results;

        // 🔍 Case 1: category만 존재
        if (category != null && (keyword == null || keyword.isBlank())) {
            results = bookService.searchByCategory(category, pageable, member);
        }
        // 🔍 Case 2: category + keyword 동시 검색
        else if (category != null && keyword != null && keyword.length() >= 2) {
            results = bookService.searchByCategoryAndKeyword(category, keyword, pageable, member);
        }
        // 🔍 Case 3: 기존 검색
        else {
            results = bookService.searchBooks(keyword, pageable, member);
        }

        model.addAttribute("searchResults", results.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", results.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category); // ✅ 추가

        if (results.isEmpty()) {
            model.addAttribute("message", "검색 결과가 없습니다.");
        }

        if (member != null) {
            model.addAttribute("memberName", member.getName());
        }

        return "book/search";
    }

    // 도서 상세 페이지
    @GetMapping("/book/{id}")
    public String getBookDetail(@PathVariable Long id,
                                @LoginMember Member member,
                                Model model) {
        // 로그인 기능 미구현 상태에서는 null 전달
        BookDetailResponse bookDetail = bookService.getBookDetail(id, member);

        model.addAttribute("book", bookDetail);

        // 로그인된 사용자 정보 전달 - nav에 띄울 이름
        if (member != null) {
            model.addAttribute("memberName", member.getName());
        }

        return "book/detail";
    }

}
