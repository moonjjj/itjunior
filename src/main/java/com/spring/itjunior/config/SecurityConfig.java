package com.spring.itjunior.config;

import com.spring.itjunior.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableGlobalMethodSecurity(prePostEnabled = true) //특정 주소로 접근하면 권한 및 인증을 미리 체크하겠다.
@EnableWebSecurity //시큐리티 필터 추 = 스프링 시큐리티가 활성화가 되어있고, 그에 해당하는 설정들을 현재 파일에서 하겠다.
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean
    public BCryptPasswordEncoder encodePWD() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //favicon.ico, resources, error 페이지는 로그인을 성공하여 나타나야할 페이지의 정적 리소스를 찾지 못하여 에러 999현상이 나오므로 ignore에 추가해주자
        web.ignoring().antMatchers("/js/**","/css/**","/image/**","/font/**","/favicon.ico", "/resources/**", "/error");
    }

    //1.코드받기(인증), 2.엑세스토큰(권한),
    //3.사용자 프로필 정보를 가져오고
    //4-1. 그 정보로 자동으로 회원가입을 시키기도 하며
    //4-2. 그 정보 외의 추가정보들(핸드폰, 집주소 ..)을 진행 시키기도 함.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //csrf토큰 비활성 (테스트시 걸어두는게 좋음)
                .authorizeRequests()
                    .antMatchers("/member/**","/mypage/**","/boards/**").access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
                    .antMatchers("/auth/**","/qnaboards/**","/","/oauth2/**","/job/**").permitAll()  // /auth로 시작하는 모든 매핑에 대하여 허용한다.
                    .anyRequest()
                    .authenticated() //허용을 제외한 나머지 모든 매핑은 인증(로그인)을 받아야 진입 가능하다.
                .and()
                    .formLogin()
                    .loginPage("/auth/loginForm")
                    .loginProcessingUrl("/auth/loginProc") //스프링 시큐리티가 해당 주소로 요청오는 로그인을 가로채서 대신 로그인 해준다.
                    .usernameParameter("userId")
                    .defaultSuccessUrl("/")
                    .successHandler(new AuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                            String saveChecked = request.getParameter("saveId");
                            Cookie cookie = new Cookie("user_check",request.getParameter("userId"));
                            if (saveChecked != null) {
                                response.addCookie(cookie);
                            } else {
                                cookie.setMaxAge(0);
                                response.addCookie(cookie);
                            }
                            response.sendRedirect("/");
                        }
                    })
                .and()
                    .logout()
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
                .and()
                    .oauth2Login()
                    .loginPage("/auth/loginForm")
                    .defaultSuccessUrl("/oauth/")
                    .userInfoEndpoint()
                    .userService(principalOauth2UserService); //구글 로그인이 완료되고 후처리 필요.(코드X , 엑세스토큰,사용자프로필정보O)

    }

    //암호화된 비번 체킹 메소드 구현 안해도 security가 이 메서드를 default로 생각하여 해쉬된 비밀번호 자동 비교 해줌.
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
//        auth.userDetailsService(principalDetailsService).passwordEncoder(encodePWD());
//    }
}
