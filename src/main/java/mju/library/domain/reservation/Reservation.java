package mju.library.domain.reservation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.common.BaseEntity;
import mju.library.domain.member.Member;

import java.time.LocalDateTime;


@Entity
@Getter
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
public class Reservation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Id → id (일관성 통일)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false) // 필수 제약 추가
    private LocalDateTime reservationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ReservationStatus.WAITING;
        }
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    

}
