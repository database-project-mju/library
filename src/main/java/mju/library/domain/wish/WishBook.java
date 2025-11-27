package mju.library.domain.wish;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.library.domain.common.BaseEntity;
import mju.library.domain.member.Member;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishBook extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    // 구매 링크
    @Column(nullable = false, length = 2000) 
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 상태 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WishBookStatus status;

    // 초기값 설정 (DB 저장 전 실행)
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = WishBookStatus.APPLIED;
        }
    }

    // 상태 변경 메서드
    public void updateStatus(WishBookStatus status) {
        this.status = status;
    }

    // 기부자 정보 필드 추가 (비회원 기부 가능하므로 Member 연관관계 X)
    private String donorName;
    private String donorPhone;

    // 배송/주문 관련 URL (기부자가 입력)
    @Column(length = 2000)
    private String deliveryUrl;

    // 기부 신청 메서드 (정보 업데이트 + 상태 변경)
    public void confirmDonation(String donorName, String donorPhone) {
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.deliveryUrl = deliveryUrl;
        this.status = WishBookStatus.DONATION; // 상태 변경
    }
}