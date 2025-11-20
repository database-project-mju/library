package mju.library.domain.review;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    //책으로 리뷰 찾기(책 상세 페이지)
    // ✅ [수정] N+1 문제를 해결하기 위해 JOIN FETCH 추가
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.book = :book")
    List<Review> findByBook(@Param("book") Book book);

    //회원으로 리뷰 찾기(마이페이지)
    // ✅ [수정] N+1 문제를 해결하기 위해 JOIN FETCH 추가
    @Query("SELECT r FROM Review r JOIN FETCH r.book WHERE r.member = :member")
    List<Review> findByMember(@Param("member") Member member);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.member JOIN FETCH r.book",
           countQuery = "SELECT COUNT(r) FROM Review r")
    Page<Review> findAllWithMemberAndBook(Pageable pageable);

    Page<Review> findByMemberId(Long memberId, Pageable pageable);
}
