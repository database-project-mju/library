// TODO: 페이지 테스트를 위해 만든 임시파일 - 해당 파일은 전부 삭제하면 됨.
package mju.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * 🧩 개발/테스트용 전체 접근 허용 설정
     * 로그인, CSRF, 기본 인증 모두 비활성화
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // ✅ 모든 요청 허용
                )
                .csrf(csrf -> csrf.disable())       // ✅ CSRF 비활성화
                .formLogin(form -> form.disable())  // ✅ 로그인 폼 비활성화
                .httpBasic(basic -> basic.disable());// ✅ 기본 인증 비활성화

        return http.build();
    }
}
