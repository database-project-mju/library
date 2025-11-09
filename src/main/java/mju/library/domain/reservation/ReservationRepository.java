package mju.library.domain.reservation;

import mju.library.domain.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByBook(Book book);

    // ✅ ID 기반 비교 방식
    boolean existsByBookId(Long bookId);
}
