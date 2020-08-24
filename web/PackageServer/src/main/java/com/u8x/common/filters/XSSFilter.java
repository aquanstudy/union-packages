package com.u8x.common.filters;

import com.u8x.common.XLogger;
import com.u8x.utils.FileUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 *  对请求进行拦截,防xss处理
 */
@WebFilter(filterName="xssFilter",urlPatterns="/*", asyncSupported = true)
public class XSSFilter implements Filter {

    private static final XLogger logger = XLogger.getLogger(XSSFilter.class);

    FilterConfig filterConfig = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if(request instanceof HttpServletRequest){
            HttpServletRequest r = (HttpServletRequest)request;
            if(r.getRequestURI().endsWith(".html")){
                if(r.getParameterNames() != null){
                    for(Enumeration e = r.getParameterNames(); e.hasMoreElements();){

                        String thisName=e.nextElement().toString();
                        String thisValue=request.getParameter(thisName);
                        String xssValue = XssHttpServletRequestWrapper.cleanXSS(thisValue);
                        if(!xssValue.equals(thisValue)){
                            //如果直接访问html资源判断带xss非法资源，直接跳转login界面重新登录
                            logger.warn("request xss invalid:"+r.getRequestURI()+";invalid param name:"+thisName+";invalid param value:"+thisValue);
                            ((HttpServletResponse)response).sendRedirect("admin/logout");
                        }
                    }
                }
            }
        }

        //对后端api进行XSS过滤
        chain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) request), response);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
