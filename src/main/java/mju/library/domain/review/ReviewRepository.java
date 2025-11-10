package mju.library.domain.review;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import org.springframework.data.jpa.repository.Query; 
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    //책으로 리뷰 찾기(책 상세 페이지)
    List<Review> findByBook(Book book);

    //회원으로 리뷰 찾기(마이페이지)
    List<Review> findByMember(Member member);

    @Query(value = "SELECT r FROM Review r JOIN FETCH r.member JOIN FETCH r.book",
           countQuery = "SELECT COUNT(r) FROM Review r")
    Page<Review> findAllWithMemberAndBook(Pageable pageable);

}
