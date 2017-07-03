/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.yahoo.dataplatform.mds.spread.column;

public final class NullCell implements ICell<Object,Object>{

  private static final NullCell CELL = new NullCell();

  private NullCell(){}

  public static ICell getInstance(){
    return CELL;
  }

  @Override
  public Object getRow(){
    return null;
  }

  @Override
  public ColumnType getType(){
    return ColumnType.NULL;
  }

  @Override
  public void setRow( final Object object ){
  }

  @Override
  public String toString(){
    StringBuffer result = new StringBuffer();
    result.append( String.format( "(%s)" , getType() ) );
    result.append( "NULL" );

    return result.toString();
  }

  @Override
  public boolean isPrimitive(){
    return false;
  }

}