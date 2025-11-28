package mju.library.domain.wish;

import lombok.RequiredArgsConstructor;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishBookService {

    private final WishBookRepository wishBookRepository;
    private final MemberRepository memberRepository;

    // (C) 희망도서 신청 
    public void requestWishBook(Long memberId, String title, String writer, String url) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        WishBook wishBook = WishBook.builder()
                .member(member)
                .title(title)
                .writer(writer)
                .url(url) 
                .build();

        wishBookRepository.save(wishBook);
    }

    // (R) 목록 조회
    @Transactional(readOnly = true)
    public Page<WishBook> getWishBookList(Pageable pageable) {
        return wishBookRepository.findAllWithMember(pageable);
    }

    // (U) 기부 신청 처리 (상태 변경: APPLIED -> DONATION)
    public void applyDonation(Long wishBookId, String donorName, String donorPhone, String deliveryUrl) {
        WishBook wishBook = wishBookRepository.findById(wishBookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 희망도서를 찾을 수 없습니다."));
        
        if (wishBook.getStatus() != WishBookStatus.APPLIED) {
            throw new IllegalStateException("이미 기부 신청이 완료된 도서입니다.");
        }

        // 엔티티의 메서드 호출
        wishBook.confirmDonation(donorName, donorPhone, deliveryUrl);
    }

    // (D) 삭제 (관리자용)
    public void deleteWishBook(Long id) {
        wishBookRepository.deleteById(id);
    }

    // (U) 관리자용 상태 변경 (배송중, 입고완료 등)
    public void updateStatus(Long id, WishBookStatus status) {
        WishBook wishBook = wishBookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("희망도서를 찾을 수 없습니다."));
        
        wishBook.updateStatus(status);
    }
}