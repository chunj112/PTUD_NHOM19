package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.auth.UserSession;

class DemoSessionTest {
    @Test void startsEmptyAndAcceptsExactlyTheSelectedDemoRole() {
        UserSession session = new UserSession();
        assertFalse(session.isActive());
        assertNull(session.getRole());
        for (Role role : Role.values()) {
            session.startDemo(role);
            assertTrue(session.isActive());
            assertEquals(role, session.getRole());
        }
        assertThrows(IllegalArgumentException.class, () -> session.startDemo(null));
    }

    @Test void logoutClearsRoleAndInvalidatesTheOldControllerVersion() {
        UserSession session = new UserSession();
        session.startDemo(Role.MANAGER);
        long oldVersion = session.version();
        session.clear();
        assertFalse(session.isActive());
        assertNull(session.getRole());
        assertThrows(IllegalStateException.class, () -> session.requireRole(oldVersion));
        session.startDemo(Role.NURSE);
        assertThrows(IllegalStateException.class, () -> session.requireRole(oldVersion));
        assertEquals(Role.NURSE, session.requireRole(session.version()));
    }
}
