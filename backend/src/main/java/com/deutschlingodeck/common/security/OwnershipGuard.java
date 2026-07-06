package com.deutschlingodeck.common.security;

import com.deutschlingodeck.common.exception.ForbiddenException;
import org.springframework.stereotype.Component;

/** Enforces that the current user owns the resource they are trying to access. */
@Component
public class OwnershipGuard {

	public void requireOwner(Long resourceOwnerId, Long currentUserId) {
		if (!resourceOwnerId.equals(currentUserId)) {
			throw new ForbiddenException("You do not have access to this resource");
		}
	}
}
