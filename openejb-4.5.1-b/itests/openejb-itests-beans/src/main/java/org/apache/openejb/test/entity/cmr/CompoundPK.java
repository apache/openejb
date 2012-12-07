/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmr;

import java.io.Serializable;


/**
 *
 * @version $Revision$ $Date$
 */
public class CompoundPK implements Serializable {
    private static final long serialVersionUID = 293670445102052793L;
    public Integer field1;
    public String field2;

    public CompoundPK() {}
    
    public CompoundPK(Integer field1, String field2) {
        this.field1 = field1;
        this.field2 = field2;
    }
    
    public boolean equals(Object other) {
      if (!(other instanceof CompoundPK) ) {
          return false;
      }
      CompoundPK otherPK = (CompoundPK) other;
      return field1.equals(otherPK.field1) && field2.equals(otherPK.field2);
    }
    
    public int hashCode() {
      return field1.hashCode() ^ field2.hashCode();
    }

}
