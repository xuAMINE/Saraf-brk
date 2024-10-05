package com.saraf.security.config;

import com.saraf.security.exception.ExpiredTokenException;
import com.saraf.security.exception.InvalidTokenException;
import com.saraf.security.token.TokenRepository;
import com.saraf.security.user.Role;
import com.saraf.security.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

  @Value("${application.security.jwt.secret-key}")
  private String secretKey;
  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;
  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration;

  private final TokenRepository tokenRepository;
  private final UserRepository userRepository;


  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    try {
      final Claims claims = extractAllClaims(token);
      return claimsResolver.apply(claims);
    } catch (ExpiredJwtException e) {
      logger.error("JWT is expired: {}", e.getMessage());
      throw new ExpiredTokenException("JWT expired at " + e.getClaims().getExpiration());
    } catch (UnsupportedJwtException e) {
      logger.error("Unsupported JWT: {}", e.getMessage());
      throw new InvalidTokenException("Unsupported JWT");
    } catch (MalformedJwtException e) {
      logger.error("Malformed JWT: {}", e.getMessage());
      throw new InvalidTokenException("Malformed JWT");
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
      throw new InvalidTokenException("Invalid JWT");
    } catch (NullPointerException e) {
      logger.error("JWT claims string is null: {}", e.getMessage());
      throw new InvalidTokenException("null pointer");
    }
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(
      Map<String, Object> extraClaims,
      UserDetails userDetails
  ) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(
      UserDetails userDetails
  ) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  private String buildToken(
          Map<String, Object> extraClaims,
          UserDetails userDetails,
          long expiration
  ) {
    return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && isTokenExpired(token);
  }

  public boolean isTokenValid(String token) {
    var tokenEntity = tokenRepository.findByToken(token).orElse(null);
    return tokenEntity != null && !tokenEntity.isExpired() && !tokenEntity.isRevoked() && isTokenExpired(token);
  }

  public Role getUserRoleFromToken(String token) {
    String email = extractUsername(token);
    var user =  userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email));
    return user.getRole();
  }

  private boolean isTokenExpired(String token) {
    return !extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    try {
      return extractClaim(token, Claims::getExpiration);
    } catch (InvalidTokenException e) {
        logger.error("Invalid token when extracting expiration: {}", e.getMessage());
        return null;
    } catch (JwtException e) {
        logger.error("JWT Exception when extracting expiration: {}", e.getMessage());
        throw new InvalidTokenException("Failed to extract expiration due to invalid token", e);
    }
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts
              .parserBuilder()
              .setSigningKey(getSignInKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
    } catch (JwtException e) {
      logger.error("Failed to extract claims: {}", e.getMessage());
      return null;
    }
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

}
