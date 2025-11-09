package mju.library.config;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.book.Category;
import mju.library.domain.lending.Lending;
import mju.library.domain.lending.LendingRepository;
import mju.library.domain.lending.LendingStatus;
import mju.library.domain.like.LikeBook;
import mju.library.domain.like.LikeBookRepository;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import mju.library.domain.member.MemberRole;
import mju.library.domain.reservation.Reservation;
import mju.library.domain.reservation.ReservationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final BCryptPasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase(BookRepository bookRepository,
                                   MemberRepository memberRepository,
                                   LendingRepository lendingRepository,
                                   LikeBookRepository likeBookRepository,
                                   ReservationRepository reservationRepository) {
        return args -> {

            // 🚀 이미 데이터가 있으면 중복 삽입 방지
            if (bookRepository.count() > 0) {
                System.out.println("ℹ️ 이미 초기 데이터가 존재합니다. 삽입을 건너뜁니다.");
                return;
            }

            /* -----------------------------------
             👩‍🎓 회원 더미 데이터
             ----------------------------------- */
            Member admin = Member.builder()
                    .studentNo("admin")
                    .name("관리자")
                    .password(passwordEncoder.encode("1234"))
                    .memberRole(MemberRole.ADMIN)
                    .build();

            Member student1 = Member.builder()
                    .studentNo("60210022")
                    .name("홍길동")
                    .password(passwordEncoder.encode("1111"))
                    .memberRole(MemberRole.STUDENT)
                    .build();

            Member student2 = Member.builder()
                    .studentNo("60210033")
                    .name("김철수")
                    .password(passwordEncoder.encode("2222"))
                    .memberRole(MemberRole.STUDENT)
                    .build();

            memberRepository.save(admin);
            memberRepository.save(student1);
            memberRepository.save(student2);

            /* -----------------------------------
             📚 도서 더미 데이터
             ----------------------------------- */
            for (int i = 1; i <= 20; i++) {
                Book book = Book.builder()
                        .isbn("978-12345-" + i)
                        .title("도서 제목 " + i)
                        .writer("저자 " + i)
                        .publisher("출판사 " + i)
                        .publishDate(LocalDate.of(2020 + (i % 3), 5, 10))
                        .description("이것은 더미 설명입니다. (테스트용)" + i)
                        .page(200 + i)
                        .imageUrl("/images/book" + i + ".jpg")
                        .category(Category.values()[i % Category.values().length])
                        .build();
                bookRepository.save(book);
            }

            /* -----------------------------------
             📖 대출 / 예약 / 찜 테스트 데이터
             ----------------------------------- */

            // 1️⃣ student2 → 도서 1,2,3 대출 중
            for (long i = 1; i <= 3; i++) {
                Book borrowedBook = bookRepository.findById(i).orElseThrow();
                Lending lending = Lending.builder()
                        .book(borrowedBook)
                        .member(student2)
                        .lendDate(LocalDateTime.now().minusDays(3))
                        .dueDate(LocalDateTime.now().plusDays(7))
                        .status(LendingStatus.BORROWED)
                        .build();
                lendingRepository.save(lending);
            }

            // 2️⃣ student1 → 도서 1번 예약
            Book reservedBook = bookRepository.findById(1L).orElseThrow();
            Reservation reservation = Reservation.builder()
                    .book(reservedBook)
                    .member(student1)
                    .reservationDate(LocalDateTime.now())
                    .build();
            reservationRepository.save(reservation);

            // 3️⃣ student1 → 도서 5,6 찜
            for (long bookId : new long[]{5L, 6L}) {
                Book likedBook = bookRepository.findById(bookId).orElseThrow();
                LikeBook likeBook = LikeBook.builder()
                        .member(student1)
                        .book(likedBook)
                        .build();
                likeBookRepository.save(likeBook);
            }

            /* -----------------------------------
             ✅ 콘솔 출력
             ----------------------------------- */
            System.out.println("✅ 초기 데이터 삽입 완료!");
            System.out.println("👩‍💻 로그인 테스트 계정");
            System.out.println(" - 관리자 ID: admin / PW: 1234");
            System.out.println(" - 학생1 ID: 60210022 / PW: 1111");
            System.out.println(" - 학생2 ID: 60210033 / PW: 2222");
            System.out.println("📘 테스트 시나리오");
            System.out.println(" - [도서1~3]: student2이 대출 중");
            System.out.println(" - [도서1]: student1가 예약 중 → 예약 버튼 비활성화");
            System.out.println(" - [도서5,6]: student1이 찜함 → ❤️ 표시");
        };
    }
}
