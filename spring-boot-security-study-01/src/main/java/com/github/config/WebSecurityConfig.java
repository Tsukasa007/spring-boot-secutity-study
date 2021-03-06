package com.github.config;

import com.github.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;


/**
 * @Description: Spring Security的Java 配置类。
 *
 *
 * @date 2020/4/17 下午5:25
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    /**
     * 密码验证器
     */
    @Autowired
    private PassWordEncorder passWordEncorder;

    /**
     * 成功处理器
     */
    @Autowired
    private AuthenctiationSuccessHandler authenctiationSuccessHandler;

    /**
     * 失败处理器
     */
   @Autowired
   private AuthenctiationFailHandler authenctiationFailHandler;


   /**
    * 向Security注入用户信息
    */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passWordEncorder);
    }

    /**
     * 配置规则
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //开启登陆配置
        http.authorizeRequests()
                // 登录之后就能访问
                .antMatchers("/no-authorize").authenticated()
                // 表示访问/admin/** 需要校长角色权限
                .antMatchers("/need-authorize").hasRole("校长")
                // 其他的路径都是登录后即可访问
                .anyRequest().authenticated()
                .and().formLogin()
                // 定义登录页面，未登录时，访问一个需要登录之后才能访问的接口，会自动跳转到该页面
                .loginPage("/login_page")
                //登录成功的处理器
                .successHandler(authenctiationSuccessHandler)
                //登录失败的处理器
                .failureHandler(authenctiationFailHandler)
                // 登录处理接口
                .loginProcessingUrl("/login")
                // 定义登录时，用户名的 key，默认为 username
                .usernameParameter("username")
                //定义登录时，用户密码的 key，默认为 password
                .passwordParameter("password").permitAll()
                .and().logout()
                ////和表单登录相关的接口统统都直接通过
                .permitAll()
                .and().csrf().disable().exceptionHandling().accessDeniedHandler(getAccessDeniedHandler());
    }

    /**
     * 对于/static/ 和 index.html 下的路径都不用认证
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers( "/no-login");
    }

    /**
     * 用户未认证异常拦截
     */
    @Bean
    AccessDeniedHandler getAccessDeniedHandler() {
        return new AuthenticationAccessDeniedHandler();
    }
}
