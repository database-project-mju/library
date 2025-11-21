package mju.library.domain.member;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.library.domain.common.BaseEntity;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole memberRole;

    public void updatePassword(String encode) {
        this.password = encode;

    }

    // 탈퇴 여부 필드
    @Column(nullable = false)
    private boolean isDeleted = false;

    // 회원 탈퇴 처리 메서드
    public void withdraw() {
        this.isDeleted = true;  // 탈퇴한 상태. (로그인,조회차단)
        this.password = "";     // 비밀번호 삭제 (접속 불가)
    }
}
