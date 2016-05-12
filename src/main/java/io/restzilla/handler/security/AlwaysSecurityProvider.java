/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.handler.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation that grants everbody access.
 *
 * @author Jeroen van Schagen
 * @since Sep 8, 2015
 */
public class AlwaysSecurityProvider implements SecurityProvider {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String[] roles, HttpServletRequest request) {
        return true;
    }
    
}
