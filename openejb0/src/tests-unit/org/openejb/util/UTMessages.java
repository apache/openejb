/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.openejb.test.NamedTestCase;

/**
 *
 * @author <a href="mailto:adc@toolazydogs.com">Alan Cabrera</a>
 */
public class UTMessages extends NamedTestCase {
	ResourceBundle testMessages;

    public UTMessages(){
        super("org.openejb.util.Messages.");
    }

    public void setUp() throws Exception {
        testMessages = ResourceBundle.getBundle( Messages.ResourceName, Locale.FRANCE );

		// Let's test the i18n code
		Messages.setLocale( Locale.FRANCE );
	}

    public void tearDown() throws Exception{
		// restore locale
        Messages.setLocale( Locale.getDefault() );
    }

    public void test01_format1() {
		String originalMessage = testMessages.getString("sa0001");
		String formattedOriginalMessage = null;
        String formattedTestMessage = Messages.format("sa0001", "foo");
		Object[] args = new Object[] { "foo" };
        MessageFormat mf;

        try {
            mf = new MessageFormat( originalMessage );
            formattedOriginalMessage =  mf.format( args );
        } catch ( Exception except ) {
            assert("An internal error occured while processing message " + originalMessage, false);
        }

		assert("Messages do not equal: original: '" + formattedOriginalMessage + "' test: '" + formattedTestMessage + "'",
				formattedOriginalMessage.equals(formattedTestMessage));
    }

    public void test02_format2() {
		String originalMessage = testMessages.getString("ge0001");
		String formattedOriginalMessage = null;
        String formattedTestMessage = Messages.format("ge0001", "foo", "bar");
		Object[] args = new Object[] { "foo", "bar" };
        MessageFormat mf;

        try {
            mf = new MessageFormat( originalMessage );
            formattedOriginalMessage =  mf.format( args );
        } catch ( Exception except ) {
            assert("An internal error occured while processing message " + originalMessage, false);
        }

		assert("Messages do not equal: original: '" + formattedOriginalMessage + "' test: '" + formattedTestMessage + "'",
				formattedOriginalMessage.equals(formattedTestMessage));
    }

    public void test03_format3() {
		String originalMessage = testMessages.getString("ge0006");
		String formattedOriginalMessage = null;
        String formattedTestMessage = Messages.format("ge0006", "foo", "bar", "bin");
		Object[] args = new Object[] { "foo", "bar", "bin" };
        MessageFormat mf;

        try {
            mf = new MessageFormat( originalMessage );
            formattedOriginalMessage =  mf.format( args );
        } catch ( Exception except ) {
            assert("An internal error occured while processing message " + originalMessage, false);
        }

		assert("Messages do not equal: original: '" + formattedOriginalMessage + "' test: '" + formattedTestMessage + "'",
				formattedOriginalMessage.equals(formattedTestMessage));
    }

    public void test04_format4() {
		Object[] args = new Object[] { "foo", "bar", "bin" };
		String originalMessage = testMessages.getString("ge0006");
		String formattedOriginalMessage = null;
        String formattedTestMessage = Messages.format("ge0006", args);
        MessageFormat mf;

        try {
            mf = new MessageFormat( originalMessage );
            formattedOriginalMessage =  mf.format( args );
        } catch ( Exception except ) {
            assert("An internal error occured while processing message " + originalMessage, false);
        }

		assert("Messages do not equal: original: '" + formattedOriginalMessage + "' test: '" + formattedTestMessage + "'",
				formattedOriginalMessage.equals(formattedTestMessage));
    }

    public void test05_message() {
		String key;
		String originalMessage;
        String testMessage;

     	for ( Enumeration e = testMessages.getKeys(); e.hasMoreElements() ; ) {
			key = (String)e.nextElement();

			originalMessage = testMessages.getString(key);
        	testMessage = Messages.message(key);

			assert("Messages do not equal: original: '" + originalMessage + "' test: '" + testMessage + "'",
					originalMessage.equals(testMessage));
		}
	}
}


