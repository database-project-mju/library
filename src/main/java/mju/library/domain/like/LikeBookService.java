package mju.library.domain.like;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import mju.library.domain.mypage.dto.LikeResDto;
import mju.library.domain.mypage.dto.ReserveResDto;
import mju.library.domain.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeBookService {

    private final BookRepository bookRepository;
    private final LikeBookRepository likeBookRepository;
    private final MemberRepository memberRepository;

    public void addLike(Member member, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다."));

        if (!likeBookRepository.existsByMemberAndBook(member, book)) {
            LikeBook likeBook = LikeBook.builder()
                    .member(member)
                    .book(book)
                    .build();
            likeBookRepository.save(likeBook);
        }
    }

    public void removeLike(Member member, Long bookId) {
        likeBookRepository.deleteByMemberIdAndBookId(member.getId(), bookId);
    }

    @Transactional(readOnly = true)
    public LikeResDto.LikeListDto getLikeList(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LikeBook> likeListWithMember = likeBookRepository.findByMemberIdFetch(memberId, pageable);

        List<LikeResDto.LikeDto> likeDto = likeListWithMember.getContent().stream()
                .map(l -> LikeResDto.LikeDto.builder()
                        .likeId(l.getId())
                        .bookId(l.getBook().getId())
                        .bookName(l.getBook().getTitle())
                        .bookAuthor(l.getBook().getPublisher())
                        .publisher(l.getBook().getPublisher())
                        .imageUrl(l.getBook().getImageUrl())
                        .publishDate(l.getBook().getPublishDate())
                        .description(l.getBook().getDescription())
                        .build()
                )
                .toList();
        return LikeResDto.LikeListDto.builder()
                .totalCount((int) likeListWithMember.getTotalElements())
                .totalPage(likeListWithMember.getTotalPages())
                .currentPage(page)
                .likeList(likeDto)
                .build();
    }
}
