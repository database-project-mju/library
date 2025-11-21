package mju.library.domain.lending;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import mju.library.domain.lending.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    boolean existsByBookAndStatus(Book book, LendingStatus status);

    // ✅ ID 기반 메서드 (더 안전)
    boolean existsByBookIdAndStatus(Long bookId, LendingStatus status);

    // 관리자 대시보드용 대출건수 세기
    long countByStatus(LendingStatus status);

    // ✅ 로그인한 사용자가 해당 도서를 대출 중인지 확인
    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, LendingStatus status);

    // (관리자용) 특정 회원이 '대출중' 또는 '연체' 상태인 모든 대출 기록 조회
    List<Lending> findByMemberAndStatusIn(Member member, List<LendingStatus> statuses);
    
    // (반납용) 특정 ID의 대출 기록 찾기 
    Optional<Lending> findById(Long id);

    // (대출 한도 검사용) 특정 회원이 '대출중' 상태인 도서의 개수를 셉니다.
    long countByMemberAndStatus(Member member, LendingStatus status);

    @Query(
            value = """
                select l
                from Lending l
                join fetch l.book
                where l.member.id = :memberId
                and l.status <> 'RETURNED'
                """,
            countQuery = """
                select count(l)
                from Lending l
                where l.member.id = :memberId
                and l.status <> 'RETURNED'
                """
    )
    Page<Lending> findByMemberIdFetch(@Param("memberId") Long memberId, Pageable pageable);

    Optional<Lending> findByIdAndMemberId(Long id, Long memberId);

    // (연체 현황 페이지용) 특정 상태(연체) 목록 페이징 조회
    @Query(value = "SELECT l FROM Lending l JOIN FETCH l.book JOIN FETCH l.member WHERE l.status = :status",
           countQuery = "SELECT COUNT(l) FROM Lending l WHERE l.status = :status")
    Page<Lending> findByStatus(@Param("status") LendingStatus status, Pageable pageable);

    // ✅ [수정됨] 특정 학생의 '반납되지 않은(대출중/연체)' 목록을 조회 (책 정보까지 한 번에 가져옴)
    // 이렇게 하면 Thymeleaf에서 lend.book.title을 호출해도 에러가 나지 않습니다.
    @Query("SELECT l FROM Lending l JOIN FETCH l.book WHERE l.member.studentNo = :studentNo AND l.returnDate IS NULL")
    List<Lending> findActiveLendsByStudentNo(@Param("studentNo") String studentNo);
}

