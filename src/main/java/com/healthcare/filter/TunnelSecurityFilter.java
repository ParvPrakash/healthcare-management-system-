package com.healthcare.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Ensures secure cookie behavior and protocol preservation behind HTTPS reverse proxies
 * (Cloudflare Tunnel, ngrok, AWS ALB, NGINX).
 *
 * Requirements:
 * - Session cookies: HttpOnly, SameSite=Lax
 * - Secure attribute: Attached ONLY when request arrived over HTTPS
 */
@WebFilter(filterName = "TunnelSecurityFilter", urlPatterns = "/*")
public class TunnelSecurityFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TunnelSecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("TunnelSecurityFilter initialized (HTTPS proxy & cookie hardening)");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        boolean isHttps = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));

        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(res) {
            @Override
            public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value != null) {
                    value = processCookieHeader(value, isHttps);
                }
                super.addHeader(name, value);
            }

            @Override
            public void setHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value != null) {
                    value = processCookieHeader(value, isHttps);
                }
                super.setHeader(name, value);
            }

            private String processCookieHeader(String cookie, boolean secure) {
                String lower = cookie.toLowerCase();

                // 1. Ensure HttpOnly for session identifiers
                if (cookie.contains("JSESSIONID") && !lower.contains("httponly")) {
                    cookie += "; HttpOnly";
                }

                // 2. Ensure SameSite=Lax for standard CSRF protection across tunnels
                if (!lower.contains("samesite")) {
                    cookie += "; SameSite=Lax";
                }

                // 3. Attach Secure ONLY when the request came over HTTPS
                if (secure && !lower.contains("secure")) {
                    cookie += "; Secure";
                }

                return cookie;
            }
        };

        chain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {}
}
