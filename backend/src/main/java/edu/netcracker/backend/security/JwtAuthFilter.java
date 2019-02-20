package edu.netcracker.backend.security;

import edu.netcracker.backend.model.User;

import edu.netcracker.backend.service.UserService;
import edu.netcracker.backend.service.impl.UserInformationHolderService;
import edu.netcracker.backend.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserInformationHolderService userInformationHolderService;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = JwtUtils.getAccessToken(httpServletRequest);

        if (jwtProvider.validateToken(accessToken, httpServletRequest) && jwtProvider.isAccessToken(accessToken)) {
            handleToken(accessToken, httpServletRequest);
        } else if (httpServletRequest.getAttribute("isExpired") != null &&
                (Boolean) httpServletRequest.getAttribute("isExpired")) {
            handleExpiredToken(httpServletRequest, httpServletResponse);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void handleExpiredToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String newAccessToken = createNewAccessTokenIfRefreshTokenIsValid(httpServletRequest);

        if (newAccessToken == null) return;

        httpServletResponse.setHeader("New-Access-Token", newAccessToken);
        handleToken(newAccessToken, httpServletRequest);
    }

    private String createNewAccessTokenIfRefreshTokenIsValid(HttpServletRequest request) {
        String refreshToken = JwtUtils.getRefreshToken(request);
        if (!jwtProvider.validateToken(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) return null;

        User user = userService.findByUsername(jwtProvider.retrieveSubject(refreshToken));
        if (isNotMatchedWithUsersRefreshToken(refreshToken, user)) return null;

        return jwtProvider.generateAccessToken(user);
    }

    private void handleToken(String accessToken, HttpServletRequest request) {
        UserDetails userDetails = userService.
                createUserDetails(userInformationHolderService.
                        convertAsUserInfo(jwtProvider.retrieveSubject(accessToken)));
        createAuthentication(userDetails, request);
    }

    private void createAuthentication(UserDetails userDetails, HttpServletRequest request) {
        if (userDetails == null) {
            logger.error("User send invalid token");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isNotMatchedWithUsersRefreshToken(String refreshToken, User user) {
        return user == null || !refreshToken.equals(user.getUserRefreshToken());
    }
}