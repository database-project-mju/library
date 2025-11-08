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
    public String home(Model model) {
        MainPageBooksDto mainBooks = bookService.getMainPageBooks();

        model.addAttribute("recentBooks", mainBooks.getRecentBooks());
        model.addAttribute("popularByLikes", mainBooks.getPopularByLikes());
        model.addAttribute("popularByLending", mainBooks.getPopularByLending());

        return "home"; // templates/home.html
    }

    /**
     * 도서 검색 기능
     */
    @GetMapping("/book/search")
    public String searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 8);

        try {
            // 로그인 기능이 아직 없으므로 null 전달
            Page<BookSearchResponse> results = bookService.searchBooks(keyword, pageable, null);

            model.addAttribute("searchResults", results.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", results.getTotalPages());
            model.addAttribute("keyword", keyword);
            model.addAttribute("message", results.isEmpty() ? "검색 결과가 없습니다." : null);

        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
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
        return "book/detail";
    }

}
