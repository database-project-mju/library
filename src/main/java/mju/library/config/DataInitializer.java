package mju.library.config;

import lombok.RequiredArgsConstructor;
import mju.library.domain.book.Book;
import mju.library.domain.book.BookRepository;
import mju.library.domain.book.Category;
import mju.library.domain.member.Member;
import mju.library.domain.member.MemberRepository;
import mju.library.domain.member.MemberRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final BCryptPasswordEncoder passwordEncoder; // ✅ 주입받기

    @Bean
    CommandLineRunner initDatabase(BookRepository bookRepository, MemberRepository memberRepository) {
        return args -> {

            // 🚀 이미 데이터가 있으면 중복 삽입 방지
            if (bookRepository.count() > 0) return;

            // 👩‍🎓 회원 더미데이터
            Member admin = Member.builder()
                    .studentNo("admin")
                    .name("관리자")
                    .password(passwordEncoder.encode("1234"))
                    .memberRole(MemberRole.ADMIN)
                    .build();

            Member student = Member.builder()
                    .studentNo("60210022")
                    .name("홍길동")
                    .password(passwordEncoder.encode("1111"))
                    .memberRole(MemberRole.STUDENT)
                    .build();

            memberRepository.save(admin);
            memberRepository.save(student);

            // 📚 도서 더미데이터
            for (int i = 1; i <= 20; i++) {
                Book book = Book.builder()
                        .isbn("978-12345-" + i)
                        .title("도서 제목 " + i)
                        .writer("저자 " + i)
                        .publisher("출판사 " + i)
                        .publishDate(LocalDate.of(2020 + (i % 3), 5, 10))
                        .description("이것은 더미 설명입니다." +
                                "돈은 법인보다 더 정교하고 구체적인 인격체다. 어떤 돈은 사람과 같이 어울리기 좋아하고 몰려다니며," +
                                "어떤 돈은 숨어서 평생을 지내기도 한다. 자기들끼리 주로 가는 곳이 따로 있고 유행에 따라 모이고 흩어진다." +
                                "자기를 소중히 여기는 사람에게 붙어 있기를 좋아하고, 함부로 대하는 사람에겐 패가망신의 보복을 퍼붓기도 한다." +
                                "작은 돈을 함부로 하는 사람에게선 큰돈이 몰려서 떠나고 자신에게 합당한 대우를 하는 사람 곁에서는 자식(이자)을 낳기도 한다." +
                                " (19쪽)비정규적인 수입은 한 번에 몰려온 돈이라 실제 가치보다 커 보이는 착각을 일으킨다. 그래서 자신이 많은 돈을 벌게 된 줄 알" +
                                "고 사치하고 함부로 사용하게 돼 결국 모으지 못하게 된다. 흔한 생각으론 돈이 또 언제 들어올지 모르니 저축을 해가며 살 것" +
                                " 같아도 실제로 그렇게 조정하는 사람은 별로 없다. (34쪽)재산 증식 과정을 보면 1, 2, 3, 4, 5처럼 양의 정수(자연수)로 " +
                                "늘어나는 것이 아니라, 1, 2, 4, 8, 16과 같이 배수로 늘어난다. 이 원리를 이해하면 누구나 부자가 될 수 있다. (39쪽)" + i)
                        .page(200 + i)
                        .imageUrl("/images/book" + i + ".jpg")
                        .category(Category.values()[i % Category.values().length])
                        .build();

                bookRepository.save(book);
            }

            System.out.println("✅ 초기 더미데이터 삽입 완료!");
        };
    }
}
