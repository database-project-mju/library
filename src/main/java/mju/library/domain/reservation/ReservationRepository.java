package mju.library.domain.reservation;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import mju.library.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByBook(Book book);

    // ✅ ID 기반 비교 방식
    boolean existsByBookId(Long bookId);

    @Query("SELECT r FROM Reservation r WHERE r.book = :book AND r.status = :status")
    Optional<Reservation> findWaitingReservation(@Param("book") Book book, @Param("status") ReservationStatus status);

    Optional<Reservation> findByMemberAndBook(Member member, Book book);

    @Query(
            value = """
                select r
                from Reservation r
                join fetch r.book
                where r.member.id = :memberId
                """,
            countQuery = """
                select count(r)
                from Reservation r
                where r.member.id = :memberId
                """
    )
    Page<Reservation> findByMemberIdFetch(@Param("memberId") Long memberId, Pageable pageable);
}
