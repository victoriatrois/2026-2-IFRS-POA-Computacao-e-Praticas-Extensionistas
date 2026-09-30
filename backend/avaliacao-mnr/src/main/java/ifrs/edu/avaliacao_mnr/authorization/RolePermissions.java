package ifrs.edu.avaliacao_mnr.authorization;

import ifrs.edu.avaliacao_mnr.enums.Role;

import java.util.EnumSet;
import java.util.Set;

/** Central role-to-permission mapping; the mapping can later be persisted without changing endpoint policy names. */
public final class RolePermissions {

    private RolePermissions() {
    }

    public static Set<Permission> forRole(Role role) {
        if (role == Role.ADMIN) {
            return EnumSet.allOf(Permission.class);
        }
        if (role == Role.EVALUATOR) {
            return EnumSet.of(
                    Permission.EVENT_READ,
                    Permission.PROJECT_READ,
                    Permission.EVALUATION_READ,
                    Permission.EVALUATION_WRITE
            );
        }
        return EnumSet.noneOf(Permission.class);
    }
}