package mju.library.domain.book;

import mju.library.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // [메인홈]
    // 최신 도서 8개 (createdAt 기준 내림차순)
    List<Book> findTop8ByOrderByCreatedAtDesc();

    // 좋아요(LikeBook) 많은 도서 Top 4
    // LikeBook 엔티티와 조인해서 좋아요 수를 세고 정렬
    @Query("""
        SELECT b
        FROM Book b
        LEFT JOIN LikeBook l ON b.id = l.book.id
        GROUP BY b.id
        ORDER BY COUNT(l.id) DESC, b.createdAt DESC, b.id DESC
    """)
    List<Book> findTopByLikeCount(org.springframework.data.domain.Pageable pageable);
    // List<Book> findTop4ByLikeCount(); // 동점자 있으면 4개 이상 받아짐 -> 페이징 사용

    // 대출 횟수가 많은 도서 Top 4
    // Lending 엔티티를 기준으로 집계
    @Query("""
        SELECT b
        FROM Book b
        LEFT JOIN Lending lend ON b.id = lend.book.id
        GROUP BY b.id
        ORDER BY COUNT(lend.id) DESC, b.createdAt DESC, b.id DESC
    """)
    List<Book> findTopByLendingCount(org.springframework.data.domain.Pageable pageable);
    // List<Book> findTop4ByLendingCount(); // 동점자 있으면 4개 이상 받아짐 -> 페이징 사용

    // [검색 기능]
    /**
     * 제목 또는 저자 기준으로 부분 일치 검색
     * (대소문자 구분 없음)
     */
    Page<Book> findByTitleContainingIgnoreCaseOrWriterContainingIgnoreCase(
            String title, String writer, Pageable pageable
    );

    // (C) 도서 추가시 중복 검사용
    boolean existsByIsbn(String Isbn);

    // ISBN으로 도서를 찾는 메소드
    Optional<Book> findByIsbn(String isbn);

    // 카테고리만 검색
    Page<Book> findByCategory(Category category, Pageable pageable);

    // 카테고리 + 제목 검색
    Page<Book> findByCategoryAndTitleContainingIgnoreCase(
            Category category, String title, Pageable pageable);

    // 카테고리 + 저자 검색
    Page<Book> findByCategoryAndWriterContainingIgnoreCase(
            Category category, String writer, Pageable pageable);

}
