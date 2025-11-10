package mju.library.domain.reservation;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.lending.LendingRepository;
import mju.library.domain.lending.LendingStatus;
import mju.library.domain.member.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LendingRepository lendingRepository;
    private final BookRepository bookRepository;

    /**
     * 예약 생성
     */
    public void reserveBook(Member member, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

        boolean isBorrowed = lendingRepository.existsByBookAndStatus(book, LendingStatus.BORROWED);
        boolean hasReservation = reservationRepository.existsByBook(book);

        if (!isBorrowed) {
            throw new IllegalStateException("현재 대출 중이 아닌 도서는 예약할 수 없습니다.");
        }
        if (hasReservation) {
            throw new IllegalStateException("이미 예약된 도서입니다.");
        }

        Reservation reservation = Reservation.builder()
                .member(member)
                .book(book)
                .reservationDate(LocalDateTime.now())
                .build();

        reservationRepository.save(reservation);
    }

    /**
     * 예약 취소
     */
    public void cancelReservation(Member member, Long bookId) {
        Book book = Book.builder().id(bookId).build();
        // findAll().stream() 대신 바로 Repository 사용
        Reservation reservation = reservationRepository.findByMemberAndBook(member, book) 
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        reservationRepository.delete(reservation);
    }
}
