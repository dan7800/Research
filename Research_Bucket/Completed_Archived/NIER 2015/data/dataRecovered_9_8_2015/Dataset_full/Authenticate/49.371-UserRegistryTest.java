package net.larsan.urd.service.user.test;

import java.security.*;
import javax.security.auth.*;
import net.larsan.norna.service.user.*;
import net.larsan.urd.service.user.*;
import net.larsan.urd.service.user.jaas.*;

public class UserRegistryTest extends TestBase {

	public UserRegistryTest(String name) {
		super(name);
	}


    // test to get a registry instance
    public void testGetInstance() {
        UserRegistry reg = UserRegistry.getInstance(users);
        super.assertTrue(reg != null);
    }
    
    
    // check that we have a test1 user
    public void testHaveUser() {
        UserRegistry reg = UserRegistry.getInstance(users);
        try {
            Indirection ind = new NullIndirection();
            ind.setCredentials("test1".toCharArray());
            Principal p = reg.getUser("test1", ind);
            super.assertEquals("test1", p.getName());
        } catch(Exception e) {
            e.printStackTrace();
            super.fail();   
        }
    }
    
    
    // check that we do not have a test3 user
    public void testHaveNotUser() {
        UserRegistry reg = UserRegistry.getInstance(users);
        try {
            Indirection ind = new NullIndirection();
            ind.setCredentials("test1".toCharArray());
            Principal p = reg.getUser("test3", ind);
            super.fail();
        } catch(NoSuchUserException e) {
            super.assertTrue(true);  
        } catch(Exception e) {
            e.printStackTrace();
            super.fail();
        }
    }
    
    
    // check that we a wrong pass doesn't get through
    public void testWrongPass() {
        UserRegistry reg = UserRegistry.getInstance(users);
        try {
            Indirection ind = new NullIndirection();
            ind.setCredentials("test1".toCharArray());
            Principal p = reg.getUser("test2", ind);
            super.fail();
        } catch(AuthenticationFailedException e) {
            super.assertTrue(true);  
        } catch(Exception e) {
            e.printStackTrace();
            super.fail();
        }
    }
}
