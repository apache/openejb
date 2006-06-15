/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.entity.cmp;

import org.tranql.cache.InTxCache;
import org.tranql.field.FieldTransform;
import org.tranql.field.Row;
import org.tranql.identity.IdentityDefiner;
import org.tranql.ql.Query;
import org.tranql.ql.QueryException;
import org.tranql.query.QueryCommand;
import org.tranql.query.ResultHandler;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class CMPSelectMethod implements InstanceOperation {
    protected final QueryCommand command;
    protected final FieldTransform resultAccessor;
    private final boolean flushCache;
    private final IdentityDefiner idDefiner;
    private final IdentityDefiner idInjector;

    public CMPSelectMethod(QueryCommand command, boolean flushCache, IdentityDefiner idDefiner, IdentityDefiner idInjector) {
        this.command = command;
        this.flushCache = flushCache;
        this.idDefiner = idDefiner;
        this.idInjector = idInjector;

        Query query = command.getQuery();
        resultAccessor = query.getResultAccessors()[0];
    }

    protected Object execute(CMPInstanceContext instCtx, ResultHandler handler, Object[] args, Object ctx) throws QueryException {
        InTxCache cache = (InTxCache) instCtx.getTransactionContext().getInTxCache();
        if (flushCache) {
            cache.flush();
        }

        if (null != idDefiner) {
            handler = new CacheFiller(handler, idDefiner, idInjector, cache);
        }
        return command.execute(cache, handler, new Row(args), ctx);
    }
}