package com.atguigu.gmall.interceptors;

import Utill.HttpClientUtil;
import Utill.MD5Utils;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 注解判断
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);
        String token="";

        String newToken = request.getParameter("newToken");
        String oldToken= CookieUtil.getCookieValue(request,"oldToken",true);

        if (StringUtils.isNotBlank(oldToken)){
            token=oldToken;
        }
        if (StringUtils.isNotBlank(newToken)){
            token=newToken;
        }
        //有注解
        if (methodAnnotation != null) {
            //账号密码校验默认false
            boolean loginCheck = false;

            if (StringUtils.isNotBlank(token)) {

                String nip = request.getHeader("request-forwared-for");//nginx ip
                if (StringUtils.isBlank(nip)) {
                    nip = request.getLocalAddr();
                    if (StringUtils.isBlank(nip)) {
                        nip = "127.0.0.1";
                    }
                }
                //内部http请求工具 校验账号密码
                String checkResult= HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token="+token+"&saltUrl="+nip);
                if (checkResult!=null&&checkResult.equals("success")){
                    //验证成功
                    loginCheck=true;
                    //老token更新
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*24,true);
                    //将userId发到域中
                    Map<String,String> gmall0906key = JwtUtil.decode("gmall0906key", token, MD5Utils.md5(nip));
                    String userId = gmall0906key.get("userId");
                    request.setAttribute("userId",userId);
                }

            }
            // 校验不通过，并且必须登陆
            if (loginCheck == false && methodAnnotation.isNeedLogin() == true) {
                response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl=" + request.getRequestURL());
                return false;
            }
        }
        return true;
    }
}
