package org.apache.openejb.assembler.classic;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * @author Romain Manni-Bucau
 */
public class ValidatorFactoryWrapper implements ValidatorFactory {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, ValidatorFactoryWrapper.class);

    private ValidatorFactory factory() {
        try {
            return (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
        } catch (NamingException e) {
            logger.warning("validator factory not found for current module ");
            return null;
        }
    }

    @Override
    public Validator getValidator() {
        return factory().getValidator();
    }

    @Override
    public ValidatorContext usingContext() {
        return factory().usingContext();
    }

    @Override
    public MessageInterpolator getMessageInterpolator() {
        return factory().getMessageInterpolator();
    }

    @Override
    public TraversableResolver getTraversableResolver() {
        return factory().getTraversableResolver();
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return factory().getConstraintValidatorFactory();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {
        return factory().unwrap(tClass);
    }
}
