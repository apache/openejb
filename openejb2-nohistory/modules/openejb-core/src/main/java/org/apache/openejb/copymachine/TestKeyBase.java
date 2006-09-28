/**
 *
 *  Copyright 2006 Matt Hogstrom
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

package org.apache.openejb.copymachine;

public class TestKeyBase implements java.io.Serializable {

  public int tkbi1 = 2;
  
  public String tkbfield1 = null;
  public String tkbfield2 = null;
  public Integer tkbinteger1 = null;
  public Integer tkbinteger2 = new Integer(1);

  public TestKey tkbtestKey1 = null;

  public int tkbi2 = 3;
}
