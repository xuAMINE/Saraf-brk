package com.saraf.security.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testGetAuthorities_user() {
        User user = User.builder()
                .role(Role.USER)
                .build();

        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(5, authorities.size());
        assertEquals("ROLE_USER", authorities.get(4).getAuthority());
    }

    @Test
    public void testGetAuthorities_manager() {
        User user = User.builder()
                .role(Role.MANAGER)
                .build();

        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(9, authorities.size());
        assertEquals("ROLE_MANAGER", authorities.get(8).getAuthority());
    }

    @Test
    public void testGetAuthorities_admin() {
        User user = User.builder()
                .role(Role.ADMIN)
                .build();

        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) user.getAuthorities();
        assertNotNull(authorities);
        assertEquals(13, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.get(12).getAuthority());
    }

    @Test
    public void testGetFullName() {
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .build();

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    public void testIsEnabled() {
        User user = User.builder()
                .enabled(true)
                .build();

        assertTrue(user.isEnabled());
    }
}
