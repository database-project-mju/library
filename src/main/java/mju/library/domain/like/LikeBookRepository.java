package mju.library.domain.like;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface LikeBookRepository extends JpaRepository<LikeBook, Long> {
    // 특정 회원이 찜한 도서들의 ID 목록 조회
    @Query("SELECT lb.book.id FROM LikeBook lb WHERE lb.member.id = :memberId")
    Set<Long> findBookIdsByMemberId(Long memberId);
    
    // 찜 기능
    boolean existsByMemberAndBook(Member member, Book book);
    void deleteByMemberIdAndBookId(Long memberId, Long bookId);

    @Query(
            value = """
                select li
                from LikeBook li
                join fetch li.book
                where li.member.id = :memberId
                """,
            countQuery = """
                select count(li)
                from LikeBook li
                where li.member.id = :memberId
                """
    )
    Page<LikeBook> findByMemberIdFetch(@Param("memberId") Long memberId, Pageable pageable);
}
