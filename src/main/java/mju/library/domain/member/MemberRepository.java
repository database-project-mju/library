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
            WHERE m.studentNo LIKE %:keyword% OR m.name LIKE %:keyword%
            """)

    Page<Member> findMembersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Member> findByStudentNo(String studentNo);
}
