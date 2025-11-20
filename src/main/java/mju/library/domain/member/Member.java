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
}
