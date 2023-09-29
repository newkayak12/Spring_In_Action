package com.example.security.config;

import com.example.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration

public class SecurityConfig extends WebSecurityConfigurerAdapter{
    @Autowired
    private UserService service;
    @Autowired
    private DataSource dataSource;

    /**
     * 사용자의 HTTP 요청 경로에 대해 접근 제한과 같은 보안 관련 처리를 우리가 원하는 대로 할 수 있게 해준다.
     */


    /**
     * 웹 요청에 대한 보안 처리
     *
     * 1. HTTP 요청 처리를 허용하기 전 충족되어야 할 특정 보안 조건을 구성
     * 2. 커스텀 로그인 페이지를 수어
     * 3. 사용자가 애플리케이션의 로그아웃을 할 수 있도록한다.
     * 4. CSRF 공격으로부터 보호
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        /**
         * 메소드
         * access(String)  : 인자로 전달된 SpEL 표현이 true이면 접근 허용
         * anonymous() : 익명 사용자에게 접근을 허용
         * authenticated() : 익명이 아닌 사용자로 인증된 경우 접근 허용
         * denyAll(): 무조건 접근 거부
         * fullyAuthenticated(): 익명이 아니거나 remember-me가 아닌 사용자로 인증되면 접근 허용
         * hasAnyAuthority(String...): 지정된 권한 중 어떤 것이라도 사용자가 같고 있으면 접근을 허용
         * hasAnyRole(String...): 지정된 역할 중 어느 하나라도 사용자가 갖고 있으면 접근을 허용
         * hasAuthority(String) : 지정된 권한을 가지고 있으면 접근을 허용
         * hasIpAddress(String) : 지정된 IP 주소로부터 요청이 오면 접근 허용
         * hasRole(String) : 지정된 역할을 사용자가 갖고 있으면 접근 허용
         * not() : 다른 메소드들의 효력을 무효와
         * permitAll() : 무조건 접근을 허용
         * rememberMe() : 이전 로그인 정보를 쿠키 혹은 DB에 저장한 후 일정 기간 내에 다시 접근 시 저장된 정보로 자동 로그인하고 이를 통해 인증된 사용자 접근 허용
         *
         *
         * SpEL
         * authentication : 해당 사용자의 인증 객체
         * denyAll: 항상 false 산출
         * hasAnyRole(역할 내역) : 지정된 역할 중 어느 하나라도 해당 사용자가 갖고 있으면 true
         * hasRole(역할) : 지정된 역할을 해당 사용자가 갖고 있으면 true
         * hasIpAddress(IP): 지정된 IP 주소로 온 요청이면 true
         * isAnonymous() : 해당 사용자가 익명 사용자이면 true
         * isAuthenticated() : 해당 사요앚가 익명이 아닌 사용자이면 true
         * isFullyAuthenticated() : 해당 사용자가 익명이 아니거나 remember-me가 아닌 사용자로 인증됐으면
         * isRememberMe() : 해당 사용자가 remember-me로 인증되었으면 true
         * permitAll : 항상 true
         * principal : 해당 사용자의 principal 객체
         *
         */
        http
            .authorizeRequests()
            .antMatchers("/design", "/order")
            //이런 조건도 가능하다.
            .access(
                    "hasRole('ROLE_USER') &&" +
                            " T(java.util.Calendar).getInstance().get(T(java.util.Calendar).DAY_OF_WEEK) ==" +
                            " T(java.util.Calendar).TUESDAY"
            )
            .antMatchers("/", "/**").access("permitAll()")
        .and()
            .formLogin()
            .loginPage("/login")
        .and()
            .csrf();
    }


    /**
     *
     * 사용자 인증관련된 처리
     */

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        super.configure(auth);
//        auth.inMemoryAuthentication()
//                .withUser("user1")
//                .password("{noop}password1")
//                .authorities("ROLE_USER")
//                .and()
//                .withUser("user2")
//                .password("{noop}password2")
//                .authorities("ROLE_USER");

        /**
         * 위의 예시는 인메모리 사용자 스토어이다.
         * 변경이 필요 없는 사용자만 미리 정해 놓고 애플리케이션을 사용한다면 적합하다.
         */




/**
 * ldap
 */
//        auth.ldapAuthentication()
//                .userSearchFilter("(uid={0})")
//                .groupSearchFilter("member={0}");





//        auth.jdbcAuthentication()
//                .dataSource(dataSource)

        /**
         * JDBC 기반이다. 별다른 설정알 하지 않으면 지정된 쿼리로 권한을 탐색한다.
         *
         * <생성 테이블>
         *  CREATE TABLE IF NOT EXISTS users (
         *       username VARCHAR2(50) NOT NULL PRIMARY KEY,
         *       password VARCHAR2(50) NOT NULL,
         *       enable CHAR(1) DEFAULT '1'
         *   );
         *
         *   CREATE TABLE IF NOT EXISTS authorities (
         *      username VARCHAR2(50) NOT NULL,
         *      authority VARCHAR2(50) NOT NULL,
         *      constraints fk_authorities_users foreign key (username) references users (username));
         *   )
         *
         *
         */

//                .usersByUsernameQuery(
//                        "select username, password, enable from users where username = ?"
//                )
//                .authoritiesByUsernameQuery(
//                        "select username, authority from authorities where username = ?"
//                )
        /**
         * 와 같이 커스텀 쿼리를 던질 수도 있다.
         */

//                .passwordEncoder(new BCryptPasswordEncoder()); // bcrypt
//                .passwordEncoder(new NoEncoding());//custom
        /**
         * 인코딩된 패스워드를 검증하려면 passwordEncoder를 지정하면 된다.
         * 암호화 알고리즘을 구현한 스프링 시큐리티의 모듈에는
         *
         *  1. BCryptPasswordEncoder: bcrypt를 해싱 암호화
         *  2. NoOpPasswordEncoder: 암호화 X
         *  3. Pbkdf2PasswordEncoder: PBKDF2를 암호화한다.
         *  4. SCryptPasswordEncoder: scrypt를 해싱 암호화
         *  5. StandardPasswordEncoder: SHA-256을 해싱 암호화
         */




        auth
                .userDetailsService(service)
                .passwordEncoder(new BCryptPasswordEncoder());
        /**
         * 커스텀
         * 커스텀 로직으로 진행할 수 있으며, 패스워드 인코더로 패스워드 검증을 진핼할 수 있다.
         */

    }

}
