package mju.library.config; 

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 컨트롤러에 공통적으로 모델 속성을 추가하는 클래스
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    /**
     * 모든 Thymeleaf 템플릿에서 'currentUri' 변수를 사용할 수 있도록 추가합니다.
     * @param request
     * @return 현재 요청의 URI (예: "/admin/members")
     */
    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}