package mju.library.domain.lending;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book; 
import mju.library.domain.book.BookRepository; 
import mju.library.domain.member.Member; 
import mju.library.domain.member.MemberRepository;
import mju.library.domain.mypage.dto.LendingResDto;
import mju.library.domain.mypage.dto.ReturnResDto;
import mju.library.domain.reservation.Reservation;
import mju.library.domain.reservation.ReservationRepository; 
import mju.library.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private static final int MAX_LEND_COUNT = 3;

    // [관리자 대시보드] (R) 연체 건수 조회
    @Transactional(readOnly = true)
    public long getOverdueCount() {
        return lendingRepository.countByStatus(LendingStatus.OVERDUE);
    }

    //[관리자 대출/반납] (R) 학생의 현재 대출/연체 목록 조회
    @Transactional(readOnly = true)
    public List<Lending> findActiveLendsByMember(String studentNo) {
        // 1. 학번으로 Member 객체를 찾음
        Member member = memberRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));

        return lendingRepository.findActiveLendsByStudentNo(studentNo);
    }

    // [관리자 대출/반납] (C) 대출 처리
    @Transactional
    public void checkOut(String studentNo, String bookIdentifier) {
        Member member = memberRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));
        Book book = findBookByIdentifier(bookIdentifier);

        // 1. 이미 도서가 대출중인지 확인
        boolean isBorrowed = lendingRepository.existsByBookIdAndStatus(book.getId(), LendingStatus.BORROWED);
        if (isBorrowed) {
            throw new IllegalArgumentException("이미 대출중인 도서입니다.");
        }
        
        // [ ✨ 2. (TODO 해결!) 1인당 대출 가능 권수 제한 로직 추가 ✨ ]
        long currentLendCount = lendingRepository.countByMemberAndStatus(member, LendingStatus.BORROWED);
        
        if (currentLendCount >= MAX_LEND_COUNT) {
            throw new IllegalArgumentException(member.getName() + "님은 현재 " + currentLendCount + "권을 대출 중입니다. (최대 " + MAX_LEND_COUNT + "권)");
        }

        // 3. 이 책에 대해 '대출 가능'(READY) 상태인 예약이 있는지 확인
        //    (ReservationRepository의 findWaitingReservation 메소드를 READY 상태로 조회)
        Optional<Reservation> readyReservationOpt = reservationRepository
                .findWaitingReservation(book, ReservationStatus.READY);

        if (readyReservationOpt.isPresent()) {
            // 4. '대출 가능' 예약이 있다면, 지금 빌리려는 사람과 예약자가 동일인인지 확인
            Reservation reservation = readyReservationOpt.get();
            if (!reservation.getMember().getId().equals(member.getId())) {
                // 4-1. 예약자와 대출자가 다르면, 대출 불가능
                throw new IllegalArgumentException(
                    "다른 사용자가 예약하여 대출 대기 중인 도서입니다. (예약자: "
                    + reservation.getMember().getName() + "님)"
                );
            }
            
            // 4-2. 예약자와 대출자가 동일하면, 예약 상태를 'COMPLETED'로 변경
            reservation.updateStatus(ReservationStatus.COMPLETED);
            
        } else {
            // 5. '대출 가능' 예약은 없지만, '대기중'(WAITING)인 예약이 있는지 확인
            //    (이 경우는 누군가 '반납'을 하지 않았는데 '대출'을 시도하는 비정상 상황)
            Optional<Reservation> waitingReservationOpt = reservationRepository
                .findWaitingReservation(book, ReservationStatus.WAITING);

            if (waitingReservationOpt.isPresent()) {
                // (이 로직은 1번 isBorrowed 확인으로 인해 대부분 걸러지지만,
                //  데이터 정합성을 위해 한 번 더 확인합니다.)
                throw new IllegalArgumentException("이미 다른 사용자가 예약(대기중) 중인 도서입니다.");
            }
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

    // ID 또는 ISBN으로 도서를 찾는 헬퍼(Helper) 메소드
    private Book findBookByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("도서 ID 또는 ISBN을 입력하세요.");
        }
        
        // 1. 숫자인지 아닌지 판별
        if (identifier.matches("\\d+")) { 
            // 2. 숫자이면 ID로 먼저 검색
            try {
                Long bookId = Long.parseLong(identifier);
                Optional<Book> bookById = bookRepository.findById(bookId);
                if (bookById.isPresent()) {
                    return bookById.get();
                }
            } catch (NumberFormatException e) {
                // (ISBN이 숫자로만 이루어진 경우 Long으로 파싱 실패할 수 있으므로, ISBN 검색으로 넘어감)
            }
        }
        
        // 3. ID 검색에 실패했거나, 숫자가 아니면 ISBN으로 검색
        return bookRepository.findByIsbn(identifier)
                .orElseThrow(() -> new IllegalArgumentException("'" + identifier + "'에 해당하는 도서를 찾을 수 없습니다. (ID 또는 ISBN)"));
    }

    //[관리자 대출/반납] (U) 반납 처리
    public Optional<Member> checkIn(Long lendingId) {
        Lending lending = lendingRepository.findById(lendingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대출 기록입니다."));

        // 1. 책 반납 처리 (Lending 테이블 UPDATE)
        lending.returnBook(); 

        // 반납상태 DB 저장
        lendingRepository.save(lending);

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


    // [관리자 연체 현황] (R) 연체중인 대출 목록 페이징 조회
    @Transactional(readOnly = true)
    public Page<Lending> findOverdueLends(Pageable pageable) {
        // '연체' 상태인 것만 조회
        return lendingRepository.findByStatus(LendingStatus.OVERDUE, pageable);
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
                        .extendable(l.isExtendable())
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

    public Page<Lending> findOverdueLoans(Pageable pageable) {

        return null;
    }

    @Transactional
    public void extendLend(Long lendId, Long memberId) {
        Lending lend = lendingRepository.findByIdAndMemberId(lendId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("대출 정보를 찾을 수 없습니다."));
        lend.extendLend();
    }

    @Transactional(readOnly = true)
    public ReturnResDto.ReturnListDto returnListDto(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lending> lendListWithMember = lendingRepository.findByMemberIdFetch(memberId, pageable);
        List<ReturnResDto.ReturnDto> returnDto = lendListWithMember.getContent().stream()
                .map(l -> ReturnResDto.ReturnDto.builder()
                        .lendId(l.getId())
                        .bookId(l.getBook().getId())
                        .bookName(l.getBook().getTitle())
                        .bookAuthor(l.getBook().getPublisher())
                        .publisher(l.getBook().getPublisher())
                        .imageUrl(l.getBook().getImageUrl())
                        .publishDate(l.getBook().getPublishDate())
                        .lendDate(LocalDate.from(l.getLendDate()))
                        .lendCount(lendingRepository.countByMemberWithBook(memberId,l.getBook().getId()))
                        .build()
                )
                .toList();
        return ReturnResDto.ReturnListDto.builder()
                .totalCount((int) lendListWithMember.getTotalElements())
                .totalPage(lendListWithMember.getTotalPages())
                .currentPage(page)
                .returnList(returnDto)
                .build();

    }
}
