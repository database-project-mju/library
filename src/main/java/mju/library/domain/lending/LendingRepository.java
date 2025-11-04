package mju.library.domain.lending;

import mju.library.domain.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    // 특정 도서가 현재 대출중인지 여부 확인
    boolean existsByBookAndStatus(Book book, LendingStatus status);

}
