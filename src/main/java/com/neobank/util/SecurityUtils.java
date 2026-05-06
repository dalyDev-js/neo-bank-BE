package com.neobank.util;

import com.neobank.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new IllegalStateException("No authenticated user found");
    }

    public static boolean isOwner(User resourceOwner) {
        return resourceOwner.getId().equals(getCurrentUser().getId());
    }
}