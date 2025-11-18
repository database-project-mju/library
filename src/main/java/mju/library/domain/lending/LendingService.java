package mju.library.domain.lending;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book; 
import mju.library.domain.book.BookRepository; 
import mju.library.domain.member.Member; 
import mju.library.domain.member.MemberRepository;
import mju.library.domain.mypage.dto.LendingResDto;
import mju.library.domain.reservation.Reservation;
import mju.library.domain.reservation.ReservationRepository; 
import mju.library.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List; 
import java.util.Arrays; 
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional 
public class LendingService {

    private final LendingRepository lendingRepository;
    private final MemberRepository memberRepository; 
    private final BookRepository bookRepository; 
    private final ReservationRepository reservationRepository;
    private static final int MAX_LOAN_COUNT = 3;

    // [관리자 대시보드] (R) 연체 건수 조회
    @Transactional(readOnly = true)
    public long getOverdueCount() {
        return lendingRepository.countByStatus(LendingStatus.OVERDUE);
    }

    //[관리자 대출/반납] (R) 학생의 현재 대출/연체 목록 조회
    @Transactional(readOnly = true)
    public List<Lending> findActiveLoansByMember(String studentNo) {
        // 1. 학번으로 Member 객체를 찾음
        Member member = memberRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));

        // 2. '대출중'이거나 '연체' 상태인 것만 조회
        List<LendingStatus> statuses = Arrays.asList(LendingStatus.BORROWED, LendingStatus.OVERDUE);
        // 3. 2단계에서 만든 Repository 메서드 호출
        return lendingRepository.findByMemberAndStatusIn(member, statuses);
    }

    // [관리자 대출/반납] (C) 대출 처리
    public void checkOut(String studentNo, Long bookId) {
        Member member = memberRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서ID입니다."));

        // 1. 이미 도서가 대출중인지 확인
        boolean isBorrowed = lendingRepository.existsByBookIdAndStatus(book.getId(), LendingStatus.BORROWED);
        if (isBorrowed) {
            throw new IllegalArgumentException("이미 대출중인 도서입니다.");
        }
        
        // [ ✨ 2. (TODO 해결!) 1인당 대출 가능 권수 제한 로직 추가 ✨ ]
        long currentLoanCount = lendingRepository.countByMemberAndStatus(member, LendingStatus.BORROWED);
        
        if (currentLoanCount >= MAX_LOAN_COUNT) {
            throw new IllegalArgumentException(member.getName() + "님은 현재 " + currentLoanCount + "권을 대출 중입니다. (최대 " + MAX_LOAN_COUNT + "권)");
        }
        
        // 3. 대출 기록 생성
        Lending newLending = Lending.builder()
                .member(member)
                .book(book)
                .lendDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .status(LendingStatus.BORROWED)
                .build();
        lendingRepository.save(newLending);
    }

    //[관리자 대출/반납] (U) 반납 처리
    public Optional<Member> checkIn(Long lendingId) {
        Lending lending = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대출 기록입니다."));

        // 1. 책 반납 처리 (Lending 테이블 UPDATE)
        lending.returnBook(); 

        // 2. 예약자 확인
        Optional<Reservation> reservationOpt = reservationRepository
            .findWaitingReservation(lending.getBook(), ReservationStatus.WAITING);
        
        // 3. 예약자가 있는지 확인하고, 있으면 '정보'를 반환
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            // 3-1. 예약 상태를 '대출 가능'으로 변경
            reservation.updateStatus(ReservationStatus.READY);
            
            // 3-2. Controller에게 "다음 예약자는 이 사람입니다"라고 알려줌
            return Optional.of(reservation.getMember());
        } 
        // 4. 예약자가 없으면 '빈 상자'를 반환
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public LendingResDto.LendingListDto getLendList(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Lending> lendListWithMember = lendingRepository.findByMemberIdFetch(memberId, pageable);

        List<LendingResDto.LendingDto> lendingDto = lendListWithMember.getContent().stream()
                .map(l -> LendingResDto.LendingDto.builder()
                        .lendId(l.getId())
                        .bookId(l.getBook().getId())
                        .bookName(l.getBook().getTitle())
                        .bookAuthor(l.getBook().getPublisher())
                        .publisher(l.getBook().getPublisher())
                        .imageUrl(l.getBook().getImageUrl())
                        .publishDate(l.getBook().getPublishDate())
                        .lendDate(LocalDate.from(l.getLendDate()))
                        .dueDate(LocalDate.from(l.getDueDate()))
                        .build()
                )
                .toList();
        return LendingResDto.LendingListDto.builder()
                .totalCount((int) lendListWithMember.getTotalElements())
                .totalPage(lendListWithMember.getTotalPages())
                .currentPage(page)
                .lendingList(lendingDto)
                .build();

    }


    @Transactional
    public void cancelLend(Long memberId, Long lendId) {

        Lending lend = lendingRepository.findByIdAndMemberId(lendId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("대출 정보를 찾을 수 없습니다."));

        if (lend.getStatus().equals(LendingStatus.RETURNED)) {
            throw new IllegalArgumentException("이미 반납된 항목은 삭제할 수 없습니다.");
        }

        lendingRepository.delete(lend);
    }
}