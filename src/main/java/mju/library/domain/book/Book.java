package mju.library.domain.book;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mju.library.domain.common.BaseEntity;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    private String writer;
    private String publisher;
    private LocalDate publishDate;

    @Column(length = 2000)
    private String description;

    private Integer page;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Category category;
}
