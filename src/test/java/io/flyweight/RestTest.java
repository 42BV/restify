/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.flyweight;

import io.flyweight.builder.EntityBuilder;
import io.flyweight.builder.OtherBuilder;
import io.flyweight.builder.UserBuilder;
import io.flyweight.model.User;
import io.flyweight.model.WithOtherEntity;
import io.flyweight.model.WithReadOnly;
import io.flyweight.model.WithService;
import io.flyweight.model.WithoutPatch;
import io.flyweight.util.PageableResolver;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
public class RestTest extends AbstractControllerTest {

    @Autowired
    private UserBuilder userBuilder;
    
    @Autowired
    private OtherBuilder otherBuilder;
    
    @Autowired
    private EntityBuilder entityBuilder;

    @Test
    public void testFindAllAsArray() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[{\"name\":\"Jan\"}]", response.getContentAsString());
    }
    
    @Test
    public void testFindAllAsArrayNoData() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.GET.name());

        MockHttpServletResponse response = call(request);
        Assert.assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    public void testFindAllAsPage() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setParameter(PageableResolver.PAGE_PARAMETER, "0");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[{\"name\":\"Jan\"}]"));
        Assert.assertTrue(contents.contains("\"number\":0"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }
    
    @Test
    public void testFindAllAsPageNoData() throws Exception {
        userBuilder.createUser("Jan");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setParameter(PageableResolver.PAGE_PARAMETER, "1");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[]"));
        Assert.assertTrue(contents.contains("\"number\":1"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }
    
    @Test
    public void testFindById() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user/" + henk.getId());
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals("{\"name\":\"Henk\"}", response.getContentAsString());
    }
    
    @Test
    public void testCreate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user");
        request.setMethod(RequestMethod.POST.name());
        
        User piet = new User();
        piet.setName("Piet");
        setContentAsJson(request, piet);

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertTrue(response.getContentAsString().matches("\\d+"));
        
        Assert.assertEquals(Long.valueOf(1), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    }
    
    //    @Test
    //    public void testUpdate() throws Exception {
    //        User henk = userBuilder.createUser("Henk", "henk@42.nl");
    //
    //        MockHttpServletRequest request = new MockHttpServletRequest();
    //        request.setRequestURI("/user/" + henk.getId());
    //        request.setMethod(RequestMethod.PUT.name());
    //        
    //        setValueAsJson(request, "{\"name\":\"Piet\"}");
    //
    //        MockHttpServletResponse response = call(request);
    //        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
    //        Assert.assertEquals("{\"name\":\"Piet\",\"email\":\"henk@42.nl\"}", response.getContentAsString());
    //        
    //        Assert.assertEquals(Long.valueOf(1), 
    //                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user", Long.class));
    //    }

    @Test
    public void testDelete() throws Exception {
        User henk = userBuilder.createUser("Henk");
        userBuilder.createUser("Piet");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/user/" + henk.getId());
        request.setMethod(RequestMethod.DELETE.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("", response.getContentAsString());

        Assert.assertEquals(Long.valueOf(0), 
                            getJdbcTemplate().queryForObject("SELECT count(*) FROM user WHERE id = " + henk.getId(), Long.class));
    }
    
    // Query
    
    @Test
    public void testFindAllWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withotherentity");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[{\"id\":" + entity.getId() + ",\"name\":\"My name\"}]", response.getContentAsString());
    }
    
    @Test
    public void testFindByIdWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withotherentity/" + entity.getId());
        request.setMethod(RequestMethod.GET.name());

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"My name\"}", response.getContentAsString());
    }
    
    @Test
    public void testUpdateWithQuery() throws Exception {
        WithOtherEntity entity = otherBuilder.createOther("My name");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withotherentity/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        setContentAsJson(request, entity);

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"My name\"}", response.getContentAsString());
    }

    // Custom beans

    @Test
    public void testCustomRepository() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withrepository");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    public void testCustomService() throws Exception {
        WithService entity = new WithService();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withservice");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        // In the service we append an '!' to the name 
        Assert.assertEquals("{\"id\":1,\"name\":\"Test!\"}", response.getContentAsString());
    }
    
    // Custom configuration

    @Test
    public void testDisabled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withdisabled");
        request.setMethod(RequestMethod.GET.name());
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test
    public void testSecuredHasRole() throws Exception {
        TestingAuthenticationToken admin = new TestingAuthenticationToken("admin", "admin", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(admin);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/withsecurity");
            request.setMethod(RequestMethod.GET.name());
            
            MockHttpServletResponse response = call(request);
            Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }
    
    @Test(expected = SecurityException.class)
    public void testSecuredWithoutRoleNotLoggedIn() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withsecurity");
        request.setMethod(RequestMethod.GET.name());
        
        call(request);
    }

    @Test
    public void testReadOnly() throws Exception {
        WithReadOnly entity = new WithReadOnly();
        entity.setName("Test");
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withreadonly");
        request.setMethod(RequestMethod.POST.name());
        setContentAsJson(request, entity);
        
        Assert.assertNull(getHandlerChain(request));
    }
    
    @Test
    public void testPagedOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withpagedonly");
        request.setMethod(RequestMethod.GET.name());
        
        MockHttpServletResponse response = call(request);
        String contents = response.getContentAsString();
        Assert.assertTrue(contents.contains("\"content\":[]"));
        Assert.assertTrue(contents.contains("\"number\":0"));
        Assert.assertTrue(contents.contains("\"size\":10"));
    }
    
    @Test
    public void testNoPatch() throws Exception {
        WithoutPatch entity = new WithoutPatch();
        entity.setName("My name");
        entity.setName("email@42.nl");
        entityBuilder.save(entity);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/withoutpatch/" + entity.getId());
        request.setMethod(RequestMethod.PUT.name());
        
        setValueAsJson(request, "{\"name\":\"New name\"}");

        MockHttpServletResponse response = call(request);
        Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());
        Assert.assertEquals("{\"id\":" + entity.getId() + ",\"name\":\"New name\",\"email\":null}", response.getContentAsString());
    }

}
