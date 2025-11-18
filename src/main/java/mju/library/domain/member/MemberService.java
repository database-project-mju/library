package mju.library.domain.member;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.dto.MemberResDto.MemberInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // (R) 학생 목록 조회
    @Transactional(readOnly = true) 
    public Page<Member> findMembers(String keyword, Pageable pageable) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return memberRepository.findMembersByKeyword(keyword, pageable);
        } else {
            return memberRepository.findAll(pageable);
        }
    }

    // (C) 학생 추가
    public void createMember(String studentNo, String name, String password, MemberRole role) {

        // 학번 중복 검사
        memberRepository.findByStudentNo(studentNo).ifPresent(member -> {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        });

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // Member 엔티티 생성
        Member newMember = Member.builder()
                .studentNo(studentNo)
                .name(name)
                .password(encodedPassword) 
                .memberRole(role)
                .build();

        // DB에 저장
        memberRepository.save(newMember);
    }

    // (R) 관리자 대시보드용 '총 회원 수' 조회 
    @Transactional(readOnly = true)
    public long getMemberCount() {
        // JpaRepository의 기본 'count' 기능을 호출합니다.
        return memberRepository.count();
    }
    @Transactional(readOnly = true)
    public MemberInfoDto getMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
        return MemberInfoDto.builder()
                .studentNo(member.getStudentNo())
                .name(member.getName())
                .password(member.getPassword())
                .build();
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
        // 현재 비번 검증 → 서비스 로직
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        member.updatePassword(passwordEncoder.encode(newPassword)); //더티체킹

    }
}