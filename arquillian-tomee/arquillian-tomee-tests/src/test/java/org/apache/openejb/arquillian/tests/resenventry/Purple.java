package org.apache.openejb.arquillian.tests.resenventry;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@ManagedBean
public class Purple {

    @Resource
    private Validator validator;

    @Resource
    private ValidatorFactory validatorFactory;

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Resource
    private UserTransaction userTransaction;

    @Resource
    private BeanManager beanManager;

    public void test() throws IllegalAccessException {
        ServletResourceEnvEntryInjectionTest.assertFields(this);
    }
}