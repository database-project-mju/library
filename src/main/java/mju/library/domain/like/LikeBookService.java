package mju.library.domain.like;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeBookService {

    private final BookRepository bookRepository;
    private final LikeBookRepository likeBookRepository;
    private final MemberRepository memberRepository;

    public void addLike(Long memberId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (!likeBookRepository.existsByMemberAndBook(member, book)) {
            LikeBook likeBook = LikeBook.builder()
                    .member(member)
                    .book(book)
                    .build();
            likeBookRepository.save(likeBook);
        }
    }

    public void removeLike(Long memberId, Long bookId) {
        likeBookRepository.deleteByMemberIdAndBookId(memberId, bookId);
    }
}
