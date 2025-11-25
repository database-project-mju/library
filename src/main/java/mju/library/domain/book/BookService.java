package mju.library.domain.book;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mju.library.domain.book.dto.BookDetailResponse;
import mju.library.domain.book.dto.BookSearchResponse;
import mju.library.domain.book.dto.MainPageBooksDto;
import mju.library.domain.lending.LendingRepository;
import mju.library.domain.lending.LendingStatus;
import mju.library.domain.like.LikeBookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import mju.library.domain.reservation.ReservationRepository;
import mju.library.domain.review.Review;
import mju.library.domain.review.ReviewService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final LendingRepository lendingRepository;
    private final LikeBookRepository likeBookRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewService reviewService;

    /**
     * 🏠 메인홈 화면
     * 최신 도서, 좋아요순, 대출순 TOP 조회
     */
    public List<Book> getRecentBooks() {
        return bookRepository.findTop8ByOrderByCreatedAtDesc();
    }

    public List<Book> getPopularBooksByLikes() {
        return bookRepository.findTopByLikeCount(PageRequest.of(0, 4));
    }

    public List<Book> getPopularBooksByLending() {
        return bookRepository.findTopByLendingCount(PageRequest.of(0, 4));
    }

    public MainPageBooksDto getMainPageBooks() {
        return new MainPageBooksDto(
                getRecentBooks(),
                getPopularBooksByLikes(),
                getPopularBooksByLending()
        );
    }

    /**
     * 📖 도서 상세 페이지
     */
    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetail(Long id, Member currentMember) {
        if (currentMember != null) {
            log.info("현재 로그인한 멤버: studentNo={}, name={}", currentMember.getStudentNo(), currentMember.getName());
        } else {
            log.info("로그인하지 않은 사용자 접근");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

        boolean isBorrowed = lendingRepository.existsByBookIdAndStatus(book.getId(), LendingStatus.BORROWED);
        boolean hasReservation = reservationRepository.existsByBookId(book.getId());

        // ✅ 내가 이미 대출한 책인지 확인
        boolean isMyBorrowedBook = (currentMember != null)
                && lendingRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), currentMember.getId(), LendingStatus.BORROWED);

        // ✅ 예약 가능 조건: "다른 사람이 대출 중" && "예약자 없음"
        boolean canReserve = isBorrowed && !hasReservation && !isMyBorrowedBook;

        boolean liked = (currentMember != null)
                && likeBookRepository.existsByMemberAndBook(currentMember, book);

        String lendStatus = isBorrowed ? "대출중" : "대출가능";

        List<Review> reviews = reviewService.getReviewsByBook(id);


        log.info("bookId={}, isBorrowed={}, hasReservation={}, isMyBorrowedBook={}, canReserve={}",
                book.getId(), isBorrowed, hasReservation, isMyBorrowedBook, canReserve);
        

        return BookDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .writer(book.getWriter())
                .publisher(book.getPublisher())
                .publishDate(book.getPublishDate())
                .page(book.getPage())
                .category(book.getCategory())
                .description(book.getDescription())
                .imageUrl(book.getImageUrl())
                .lendStatus(lendStatus)
                .canReserve(canReserve)
                .liked(liked)
                .createdAt(book.getCreatedAt())
                .reviews(reviews)
                .build();
    }

    /**
     * 🔍 도서 검색 기능
     * 제목/저자 LIKE 검색 + Lending 상태 + 찜 여부 + 예약 가능 여부 계산
     */
    public Page<BookSearchResponse> searchBooks(String keyword, Pageable pageable, Member currentMember) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어를 입력하세요.");
        }
        if (keyword.trim().length() < 2) {
            throw new IllegalArgumentException("두 글자 이상 입력하세요.");
        }

        // 1️⃣ 도서 검색
        Page<Book> bookPage = bookRepository.findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable
        );

        // 2️⃣ 로그인한 사용자의 찜(bookId) 목록 조회
        Set<Long> likedBookIds = (currentMember != null)
                ? likeBookRepository.findBookIdsByMemberId(currentMember.getId())
                : new HashSet<>();

        // 3️⃣ DTO 변환 (Lending 상태 + 찜 여부 + 예약 가능 여부 포함)
        return bookPage.map(book -> {
            boolean isBorrowed = lendingRepository.existsByBookIdAndStatus(book.getId(), LendingStatus.BORROWED);
            boolean hasReservation = reservationRepository.existsByBookId(book.getId());

            boolean isMyBorrowedBook = (currentMember != null)
                    && lendingRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), currentMember.getId(), LendingStatus.BORROWED);

            boolean canReserve = isBorrowed && !hasReservation && !isMyBorrowedBook;

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
                    .canReserve(canReserve)
                    .liked(liked)
                    .build();
        });
    }

    // (R) 관리자용 '도서 목록 조회' 메서드 추가 (학생용 searchBooks와 달리, keyword가 없으면 전체 조회를 실행)
    @Transactional(readOnly = true)
    public Page<Book> adminSearchBooks(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 1. 검색어가 있으면 -> 팀원이 만든 검색 기능 사용
            return bookRepository.findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            // 2. 검색어가 없으면 -> JpaRepository의 기본 전체 조회 사용
            return bookRepository.findAll(pageable);
        }
    }

    // (C) '신규 도서 수동 등록' 메서드 추가
    @Transactional
    public void createBook(String isbn, String title, String writer, String publisher, 
                           LocalDate publishDate, String description, Integer page, String imageUrl, Category category) {
        
        // 1. 이미 등록된 책인지 ISBN으로 검사
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("이미 등록된 ISBN입니다.");
        }

        // 2. 폼에서 받은 정보로 Book 엔티티 생성
        Book newBook = Book.builder()
                .isbn(isbn)
                .title(title)
                .writer(writer)
                .publisher(publisher)
                .publishDate(publishDate)
                .description(description)
                .page(page)
                .imageUrl(imageUrl)
                .category(category)
                .build();

        // 3. DB에 저장
        bookRepository.save(newBook);
    }

    // (R) 관리자 대시보드용 '총 도서 수' 조회 
    @Transactional(readOnly = true)
    public long getBookCount() {
        return bookRepository.count();
    }

    public Page<BookSearchResponse> searchByCategory(Category category,
                                                     Pageable pageable,
                                                     Member currentMember) {

        Page<Book> bookPage = bookRepository.findByCategory(category, pageable);
        return convertToSearchResponse(bookPage, currentMember);
    }

    public Page<BookSearchResponse> searchByCategoryAndKeyword(Category category,
                                                               String keyword,
                                                               Pageable pageable,
                                                               Member currentMember) {

        // 제목 기준 검색
        Page<Book> bookPage = bookRepository.findByCategoryAndTitleContainingIgnoreCase(
                category, keyword, pageable);

        // 제목 검색 결과 없으면 → 저자 검색
        if (bookPage.isEmpty()) {
            bookPage = bookRepository.findByCategoryAndWriterContainingIgnoreCase(
                    category, keyword, pageable);
        }

        return convertToSearchResponse(bookPage, currentMember);
    }

    private Page<BookSearchResponse> convertToSearchResponse(Page<Book> bookPage,
                                                             Member currentMember) {

        Set<Long> likedBookIds = (currentMember != null)
                ? likeBookRepository.findBookIdsByMemberId(currentMember.getId())
                : new HashSet<>();

        return bookPage.map(book -> {

            boolean isBorrowed = lendingRepository.existsByBookIdAndStatus(book.getId(), LendingStatus.BORROWED);
            boolean hasReservation = reservationRepository.existsByBookId(book.getId());

            boolean isMyBorrowedBook = (currentMember != null)
                    && lendingRepository.existsByBookIdAndMemberIdAndStatus(book.getId(), currentMember.getId(), LendingStatus.BORROWED);

            boolean canReserve = isBorrowed && !hasReservation && !isMyBorrowedBook;

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
                    .canReserve(canReserve)
                    .liked(liked)
                    .build();
        });
    }

}
