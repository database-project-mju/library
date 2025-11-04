package mju.library.domain.book;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.dto.MainPageBooksDto;
import mju.library.domain.lending.LendingRepository;
import mju.library.domain.lending.LendingStatus;
import mju.library.domain.like.LikeBookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import mju.library.domain.reservation.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mju.library.domain.book.dto.BookSearchResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final LendingRepository lendingRepository;
    private final LikeBookRepository likeBookRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 메인홈 화면
     * 최신 도서 조회, 좋아요 많은 도서, 대출 횟수 많은 도서 TOP N개 조회
     */
    // 최신 도서 8권 조회
    public List<Book> getRecentBooks() {
        return bookRepository.findTop8ByOrderByCreatedAtDesc();
    }

    // 좋아요 많은 도서 Top4 조회
    public List<Book> getPopularBooksByLikes() {
        return bookRepository.findTopByLikeCount(PageRequest.of(0, 4));
    }

    // 대출횟수 많은 도서 Top4 조회
    public List<Book> getPopularBooksByLending() {
        return bookRepository.findTopByLendingCount(PageRequest.of(0, 4));
    }

    // 메인 페이지 통합 데이터 조회(추후 컨트롤러에서 한 번에 전달)
    public MainPageBooksDto getMainPageBooks() {
        List<Book> recentBooks = getRecentBooks();
        List<Book> popularByLikes = getPopularBooksByLikes();
        List<Book> popularByLending = getPopularBooksByLending();

        return new MainPageBooksDto(recentBooks, popularByLikes, popularByLending);
    }

    // 도서 상세정보 확인
    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다. (id: " + id + ")"));
    }

    /**
     * 도서 검색 기능
     * 제목 또는 저자 기준 LIKE 검색 + Lending 상태 + 찜 여부 연동
     */
    public Page<BookSearchResponse> searchBooks(String keyword, Pageable pageable, Member currentMember) {

        // 1. 유효성 검사
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        if (keyword.trim().length() < 2) {
            throw new IllegalArgumentException("두 글자 이상 입력하세요.");
        }

        // 2. Book 검색 (LIKE 검색)
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable
        );

        // 3. 로그인한 사용자의 찜(bookId) 목록 조회
        Set<Long> likedBookIds = (currentMember != null)
                ? likeBookRepository.findBookIdsByMemberId(currentMember.getId())
                : new HashSet<>();

        // 4. DTO 변환 (Lending 상태 + 찜 여부 포함)
        return bookPage.map(book -> {
            boolean isBorrowed = lendingRepository.existsByBookAndStatus(book, LendingStatus.BORROWED);
            boolean hasReservation = reservationRepository.existsByBook(book);
            boolean canReserve = isBorrowed && !hasReservation;

            String lendStatus = isBorrowed ? "대출중" : "대출가능";

            boolean liked = likedBookIds.contains(book.getId());

            return BookSearchResponse.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .writer(book.getWriter())
                    .publisher(book.getPublisher())
                    .publishDate(book.getPublishDate())
                    .imageUrl(book.getImageUrl())
                    .description(book.getDescription())
                    .lendStatus(lendStatus)
                    .liked(liked)
                    .build();
        });
    }
}
