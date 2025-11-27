package mju.library.domain.wish;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WishBookRepository extends JpaRepository<WishBook, Long> {

    // 신청 목록 조회 (N+1 방지)
    @Query(value = "SELECT w FROM WishBook w JOIN FETCH w.member",
           countQuery = "SELECT COUNT(w) FROM WishBook w")
    Page<WishBook> findAllWithMember(Pageable pageable);
}