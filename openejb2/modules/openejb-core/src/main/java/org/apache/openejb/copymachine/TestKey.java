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

public class TestKey extends TestKeyBase implements java.io.Serializable  {

  public int i1 = 2;
  public String field1 = null;
  public String field2 = null;
  public Integer integer1 = null;
  transient public Integer integer2 = new Integer(1);
  public TestKey testKey1 = null;
  public final int a=2;

}
