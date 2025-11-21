package mju.library.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    //관리자용 검색 기능
    @Query("""
            SELECT m 
            FROM Member m
            WHERE (m.studentNo LIKE %:keyword% OR m.name LIKE %:keyword%)
            AND m.isDeleted = false
            """)
    Page<Member> findMembersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 기본 목록 조회용 (검색어 없을 때)
    // 탈퇴하지 않은 회원만 전체 조회
    Page<Member> findAllByIsDeletedFalse(Pageable pageable);

    // 로그인/대출 시 회원 조회용
    // 학번으로 찾되, '탈퇴하지 않은' 사람이어야 함. 
    // (탈퇴한 학번으로 로그인 시도하면 못 찾게 막음)
    Optional<Member> findByStudentNoAndIsDeletedFalse(String studentNo);

    Optional<Member> findByStudentNo(String studentNo);

    long countByIsDeletedFalse();
    
}
