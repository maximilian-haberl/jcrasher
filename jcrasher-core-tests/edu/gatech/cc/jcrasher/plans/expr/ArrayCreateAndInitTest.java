/*
 * Copyright (C) 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.gatech.cc.jcrasher.plans.expr;


import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import junit.framework.TestCase;
import client.Client;
import client.sub.Loadee;
import client.sub.LoadeeCall;
import client.sub.Needed;
import edu.gatech.cc.jcrasher.plans.expr.literals.IntLiteral;
import edu.gatech.cc.jcrasher.plans.expr.literals.NullLiteral;
import edu.gatech.cc.jcrasher.plans.expr.literals.StringLiteral;
import edu.gatech.cc.jcrasher.types.ClassWrapper;


/**
 * @author csallner@gatech.edu (Christoph Csallner)
 */
public class ArrayCreateAndInitTest extends TestCase {

  protected final ArrayCreateAndInit<int[]> int1_fieldsSetToNull = 
    new ArrayCreateAndInit<int[]>(int[].class, Client.class);
  protected final ArrayCreateAndInit<int[][]> int2_fieldsSetToNull = 
    new ArrayCreateAndInit<int[][]>(int[][].class, Client.class);
  protected final ArrayCreateAndInit<int[][][][][][][][][][]> int10_fieldsSetToNull = 
    new ArrayCreateAndInit<int[][][][][][][][][][]>(int[][][][][][][][][][].class, Client.class);  

  protected final ArrayCreateAndInit<int[][][][][][][][][][]> int10 = 
    new ArrayCreateAndInit<int[][][][][][][][][][]>(int[][][][][][][][][][].class, Client.class);
  
  protected final ArrayCreateAndInit<String[][][]> string3 = 
    new ArrayCreateAndInit<String[][][]>(String[][][].class, Client.class);
  
  protected final ArrayCreateAndInit<Set[][]> set2 = 
    new ArrayCreateAndInit<Set[][]>(Set[][].class, Client.class);
  
  protected final ArrayCreateAndInit<int[]> int1_length3 = 
    new ArrayCreateAndInit<int[]>(int[].class, Client.class);

  protected final ArrayCreateAndInit<int[][]> int2_length2 = 
    new ArrayCreateAndInit<int[][]>(int[][].class, Client.class);  
  
  protected final int[] int1_length2_val = new int[]{0,1};
  protected final int[] int1_length3_val = new int[]{0,1,2};
  protected final int[] int1_length4_val = new int[]{0,1,2,3};
  
  /* {{0,1,2}, {0,1,2}} */
  protected final int[][] int2_length2_val = new int[][]{
      int1_length3_val, int1_length3_val};
  
  protected final String string1_length3_val = 
  	"new java.lang.String[]{(java.lang.String)null, \"\", \"hallo\"}";
  protected final ArrayCreateAndInit<String[]> string1_length3 =
      new ArrayCreateAndInit<String[]>(String[].class, Client.class);
  
  protected final ArrayCreateAndInit<Loadee[]> loadeeForClient =
  	new ArrayCreateAndInit<Loadee[]>(Loadee[].class, Client.class);
  	
    protected final ArrayCreateAndInit<Loadee[]> loadeeForNeeded =
    	new ArrayCreateAndInit<Loadee[]>(Loadee[].class, Needed.class);  	
      
      
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    int1_fieldsSetToNull.leafType = null;
    int1_fieldsSetToNull.dimensionality = 1;
    
    int2_fieldsSetToNull.leafType = null;
    int2_fieldsSetToNull.dimensionality = 1;
    
    int10_fieldsSetToNull.leafType = null;
    int10_fieldsSetToNull.dimensionality = 1;
    
    int1_length3.setComponentPlans(new Expression[]{
      new IntLiteral(0), new IntLiteral(1), new IntLiteral(2)
    });
    
    int2_length2.setComponentPlans(new Expression[] {
        int1_length3, int1_length3});
    
    string1_length3.setComponentPlans(new Expression[] {
        new NullLiteral<String>(String.class, Client.class),
        new StringLiteral(""),
        new StringLiteral("hallo")});
    
    loadeeForClient.setComponentPlans(new Expression[] {
    		new NullLiteral<Loadee>(Loadee.class, Client.class),
    		(new LoadeeCall(Client.class)).constructor()});
    
    loadeeForNeeded.setComponentPlans(new Expression[] {
    		new NullLiteral<Loadee>(Loadee.class, Needed.class),
    		(new LoadeeCall(Needed.class)).constructor()});    
  }


  /***/
  public void testDiscoverLeafLevel() {
    int1_fieldsSetToNull.discoverLeafLevel();
    assertEquals(1, int1_fieldsSetToNull.dimensionality);
    assertEquals(int.class, int1_fieldsSetToNull.leafType);
    
    int2_fieldsSetToNull.discoverLeafLevel();
    assertEquals(2, int2_fieldsSetToNull.dimensionality);
    assertEquals(int.class, int2_fieldsSetToNull.leafType);
    
    int10_fieldsSetToNull.discoverLeafLevel();
    assertEquals(10, int10_fieldsSetToNull.dimensionality);
    assertEquals(int.class, int10_fieldsSetToNull.leafType);
  }

  /***/
  public void testArrayCreateAndInitClassWrapperNull() {
    try {
      new ArrayCreateAndInit<Object>((ClassWrapper<Object>)null, Client.class);
      fail("null argument");
    }
    catch(RuntimeException e) {  //expected
    }
    
    try {
      new ArrayCreateAndInit<Object>((ClassWrapper<Object>)null, null);
      fail("null argument");
    }
    catch(RuntimeException e) {  //expected
    }
  }

  
  /***/
  public void testArrayCreateAndInitNull() {
    try {
      new ArrayCreateAndInit<int[]>(int[].class, null);
      fail("null argument");
    }
    catch(RuntimeException e) {  //expected
    }
  }

  /***/
  public void testArrayCreateAndInitClass() {
    assertEquals(10, int10.dimensionality);
    assertEquals(int.class, int10.leafType);
    
    assertEquals(3, string3.dimensionality);
    assertEquals(String.class, string3.leafType);
    
    assertEquals(2, set2.dimensionality);
    assertEquals(Set.class, set2.leafType);    
  }  
  
  /***/
  public void testGetReturnType() {
    assertEquals(int[][][][][][][][][][].class, int10.getReturnType());    
    assertEquals(String[][][].class, string3.getReturnType());    
    assertEquals(Set[][].class, set2.getReturnType());
    assertEquals(int[].class, int1_length3.getReturnType());
    
    assertEquals(Loadee[].class, loadeeForClient.getReturnType());
    assertEquals(Loadee[].class, loadeeForNeeded.getReturnType());
  }

  /***/
  public void testExecute() throws InvocationTargetException,
      IllegalAccessException, InstantiationException
  {
    assertTrue(Arrays.equals(
        int1_length3_val, int1_length3.execute()));
    assertFalse(Arrays.equals(
        int1_length4_val, int1_length3.execute()));
    assertFalse(Arrays.equals(
        new int[]{0,1,1}, int1_length3.execute()));
    assertFalse(Arrays.equals(
        int1_length2_val, int1_length3.execute()));
     
    assertTrue(Arrays.deepEquals(
        int2_length2_val, int2_length2.execute()));
  }

  /***/
  public void testText() {
    assertEquals(
    		string1_length3_val,
    		string1_length3.text());
    
    assertEquals(
        "new int[][]{new int[]{0, 1, 2}, new int[]{0, 1, 2}}",
        int2_length2.text());

    assertEquals(
    		"new client.sub.Loadee[]{(client.sub.Loadee)null, new client.sub.Loadee()}",
    		loadeeForClient.text());
    
    assertEquals(
    		"new Loadee[]{(Loadee)null, new Loadee()}",
    		loadeeForNeeded.text());
  }

  
  /***/
  public void testToString() {
    assertEquals(
    		string1_length3.toString(),
    		string1_length3.text());
    
    assertEquals(
    		int2_length2.toString(),
        int2_length2.text());
    
    assertEquals(
    		loadeeForClient.toString(),
    		loadeeForClient.text());
    
    assertEquals(
    		loadeeForNeeded.toString(),
    		loadeeForNeeded.text());    
  }  
}
