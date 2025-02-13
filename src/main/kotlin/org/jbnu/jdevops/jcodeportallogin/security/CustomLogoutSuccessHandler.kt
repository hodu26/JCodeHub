package org.jbnu.jdevops.jcodeportallogin.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomLogoutSuccessHandler : LogoutSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: org.springframework.security.core.Authentication?
    ) {
        // 1. 세션 무효화 (있다면)
        request.session?.invalidate()

        // 2. SecurityContext 클리어
        SecurityContextHolder.clearContext()

        // 3. (선택) JWT를 사용중이라면, 블랙리스트에 추가하는 등 추가 처리를 할 수 있습니다.
        // 예: jwtBlacklistService.add(authentication.getToken());

        // 4. 로그아웃 후 원하는 URL로 리다이렉트
        response.sendRedirect("/logout/success")
    }
}