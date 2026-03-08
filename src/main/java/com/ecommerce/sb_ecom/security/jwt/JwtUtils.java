package com.ecommerce.sb_ecom.security.jwt;

import com.ecommerce.sb_ecom.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int  jwtExpirationMs;

    @Value("${spring.app.cookie.name}")
    private String jwtCookie;

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public String getJwtTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public String getJwtTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie responseCookie = ResponseCookie.from(jwtCookie,"")
                .path("/api")
                .httpOnly(true)
                .build();
        return responseCookie;
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userDetails) {
        String jwtToken = generateJwtTokenFromUsername(userDetails.getUsername());
        ResponseCookie responseCookie = ResponseCookie.from(jwtCookie,jwtToken)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(true)
                .build();
        return responseCookie;
    }

    public String generateJwtTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        String username = Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
        return username;
    }

    public boolean validateJwtToken(String token) {
        try{
            Jwts.parser().verifyWith((SecretKey) key())
            .build().parseSignedClaims(token);

            return true;
        }
        catch (MalformedJwtException exception){
            logger.error("Invalid JWT Token: {}", exception.getMessage());
        }
        catch (ExpiredJwtException exception){
            logger.error("Expired JWT Token: {}", exception.getMessage());
        }
        catch (UnsupportedJwtException exception){
            logger.error("Unsupported JWT Token: {}", exception.getMessage());
        }
        catch(IllegalArgumentException exception){
            logger.error("JWT Claims String Is Empty!!! : {}", exception.getMessage());
        }
        return false;
    }


    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
    }
}
