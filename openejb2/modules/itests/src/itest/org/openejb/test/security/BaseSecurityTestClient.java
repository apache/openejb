/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.test.security;

import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.util.Properties;

import org.openejb.test.NamedTestCase;
import org.openejb.test.TestManager;


/**
 * @version $Revision$ $Date$
 */
public class BaseSecurityTestClient extends NamedTestCase {

    protected InitialContext initialContext;


    protected LoginContext public_david_login_context;
    protected Subject public_david_subject;
    protected LoginContext public_alan_login_context;
    protected Subject public_alan_subject;
    protected LoginContext public_dain_login_context;
    protected Subject public_dain_subject;
    protected LoginContext public_noel_login_context;
    protected Subject public_noel_subject;
    protected LoginContext public_geir_login_context;
    protected Subject public_geir_subject;
    protected LoginContext public_george_login_context;
    protected Subject public_george_subject;
    protected LoginContext public_gracie_login_context;
    protected Subject public_gracie_subject;
    protected LoginContext black_david_login_context;
    protected Subject black_david_subject;
    protected LoginContext black_dain_login_context;
    protected Subject black_dain_subject;

    public BaseSecurityTestClient(String testName) {
        super("BaseSecurity." + testName);
    }

    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();

        initialContext = new InitialContext(properties);

        public_david_login_context = new LoginContext("public", new UsernamePasswordCallback("david", "guitar"));
        public_david_login_context.login();
        public_david_subject = public_david_login_context.getSubject();

        public_alan_login_context = new LoginContext("public", new UsernamePasswordCallback("alan", "cat"));
        public_alan_login_context.login();
        public_alan_subject = public_alan_login_context.getSubject();

        public_dain_login_context = new LoginContext("public", new UsernamePasswordCallback("dain", "dog"));
        public_dain_login_context.login();
        public_dain_subject = public_dain_login_context.getSubject();

        public_noel_login_context = new LoginContext("public", new UsernamePasswordCallback("noel", "nudge"));
        public_noel_login_context.login();
        public_noel_subject = public_noel_login_context.getSubject();

        public_geir_login_context = new LoginContext("public", new UsernamePasswordCallback("geir", "apple"));
        public_geir_login_context.login();
        public_geir_subject = public_geir_login_context.getSubject();

        public_george_login_context = new LoginContext("public", new UsernamePasswordCallback("george", "bone"));
        public_george_login_context.login();
        public_george_subject = public_george_login_context.getSubject();

        public_gracie_login_context = new LoginContext("public", new UsernamePasswordCallback("gracie", "biscuit"));
        public_gracie_login_context.login();
        public_gracie_subject = public_gracie_login_context.getSubject();

        black_david_login_context = new LoginContext("black", new UsernamePasswordCallback("david", "deltaforce"));
        black_david_login_context.login();
        black_david_subject = black_david_login_context.getSubject();

        black_dain_login_context = new LoginContext("black", new UsernamePasswordCallback("dain", "secret"));
        black_dain_login_context.login();
        black_dain_subject = black_dain_login_context.getSubject();
    }

    protected void tearDown() throws Exception {
        public_david_login_context.logout();
        public_alan_login_context.logout();
        public_dain_login_context.logout();
        public_noel_login_context.logout();
        public_geir_login_context.logout();
        public_george_login_context.logout();
        public_gracie_login_context.logout();
        black_david_login_context.logout();
        black_dain_login_context.logout();
    }

    public static class UsernamePasswordCallback implements CallbackHandler {

        private final String username;
        private final String password;

        public UsernamePasswordCallback(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                } else if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback) callbacks[i]).setName(username);
                }
            }
        }
    }
}
