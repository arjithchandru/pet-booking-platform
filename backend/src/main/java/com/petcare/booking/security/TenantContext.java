package com.petcare.booking.security;

import com.petcare.booking.domain.entity.Tenant;
import com.petcare.booking.domain.entity.User;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<Tenant> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(Tenant tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static Tenant getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static UUID getTenantId() {
        Tenant tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant.getId() : null;
    }

    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}