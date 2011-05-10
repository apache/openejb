package org.apache.openejb.bval;

import org.apache.openejb.jee.EmptyType;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Romain Manni-Bucau
 */
@RunWith(ApplicationComposer.class)
public class BeanValidationAppendixInterceptorTest {
    @EJB private ValidationManager validationManager;

    @Module public StatelessBean bean() throws Exception {
        final StatelessBean bean = new StatelessBean(ValidationManager.class);
        bean.setLocalBean(new EmptyType());
        return bean;
    }

    @LocalBean
    @Stateless
    @Interceptors(BeanValidationAppendixInterceptor.class)
    public static class ValidationManager {
        @Min(0) public int valid(@Size(max = 1) @NotNull String complicatedName, @Max(5) int ret) {
            return ret;
        }
    }

    @Test public void valid() {
        validationManager.valid("", 1);
    }

    @Test public void paramNotValid() {
        try {
            validationManager.valid(":(", 1);
            fail();
        } catch (Exception exception) {
            assertTrue(exception.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException validationException = (ConstraintViolationException) exception.getCause();
            assertEquals(1, validationException.getConstraintViolations().size());
        }
    }

    @Test public void returnedNotValid() {
        try {
            validationManager.valid("", -1);
            fail();
        } catch (Exception exception) {
            assertTrue(exception.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException validationException = (ConstraintViolationException) exception.getCause();
            assertEquals(1, validationException.getConstraintViolations().size());
        }
    }

    @Test public void paramNotValid2() {
        try {
            validationManager.valid(null, 6);
            fail();
        } catch (Exception exception) {
            assertTrue(exception.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException validationException = (ConstraintViolationException) exception.getCause();
            assertEquals(2, validationException.getConstraintViolations().size());
        }
    }
}
