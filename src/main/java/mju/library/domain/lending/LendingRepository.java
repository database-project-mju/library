package mju.library.domain.lending;

import mju.library.domain.book.Book;
import mju.library.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    boolean existsByBookAndStatus(Book book, LendingStatus status);

    // ✅ ID 기반 메서드 (더 안전)
    boolean existsByBookIdAndStatus(Long bookId, LendingStatus status);

    // ✅ 로그인한 사용자가 해당 도서를 대출 중인지 확인
    boolean existsByBookIdAndMemberIdAndStatus(Long bookId, Long memberId, LendingStatus status);
}
