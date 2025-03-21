package com.example.movie_booking_system.config;

import jakarta.servlet.Filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.List;

public class JwtTokenValidator extends OncePerRequestFilter {

    static Key key = KeyConfig.key;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader!=null && authHeader.startsWith("Bearer")){
            String jwt = authHeader.substring(7);
            try{
                JwtParserBuilder builder = Jwts.parser().setSigningKey(key);
                System.out.println("Builder created Successfully");
                JwtParser parser = builder.build();
                System.out.println("Build Successful");
                Jwt jwtToken = parser.parseClaimsJws(jwt.trim());
                System.out.println("Got the token");
                System.out.println(jwtToken.getBody());
                Claims claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(jwt.trim()).getBody();

                String email = String.valueOf(claims.get("email"));
                String authorities = String.valueOf(claims.get("authorities"));

                List<GrantedAuthority> auths = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(email,null,auths);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            catch(Exception e){
                e.printStackTrace();
                throw new BadCredentialsException("Invalid token..");
            }
        }

        filterChain.doFilter(request,response);

    }
}
