package mju.library.domain.reservation;

public enum ReservationStatus {
    WAITING, // 대기중
    READY,   // 대출 가능 (반납됨)
    COMPLETED, // 대출 완료 (예약 후 대출해 감)
    CANCELED // 예약 취소됨
}