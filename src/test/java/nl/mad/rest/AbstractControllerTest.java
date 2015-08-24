/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl.mad.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Template for testing the dynamic REST endpoints.
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public abstract class AbstractControllerTest extends AbstractSpringTest {
    
    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;
    
    @Autowired
    private HandlerMapping handlerMapping;
    
    public MockHttpServletResponse call(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Object handler = handlerMapping.getHandler(request).getHandler();
        handlerAdapter.handle(request, response, handler);
        return response;
    }

}
