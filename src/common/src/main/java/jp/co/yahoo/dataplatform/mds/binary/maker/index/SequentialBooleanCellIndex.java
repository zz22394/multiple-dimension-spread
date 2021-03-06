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
package jp.co.yahoo.dataplatform.mds.binary.maker.index;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import jp.co.yahoo.dataplatform.mds.spread.column.filter.FilterType;
import jp.co.yahoo.dataplatform.mds.spread.column.filter.IFilter;
import jp.co.yahoo.dataplatform.mds.spread.column.filter.BooleanFilter;
import jp.co.yahoo.dataplatform.mds.spread.column.index.ICellIndex;

public class SequentialBooleanCellIndex implements ICellIndex{

  private final byte[] buffer;

  public SequentialBooleanCellIndex( final byte[] buffer ){
    this.buffer = buffer;
  }

  @Override
  public List<Integer> filter( final IFilter filter ) throws IOException{
    if( filter == null ){
      return null;
    }
    if( filter.getFilterType() == FilterType.BOOLEAN ){
      byte target;
      if( ( (BooleanFilter)filter ).getFlag() ){
        target = 1;
      }
      else{
        target = 0;
      }
      List<Integer> result = new ArrayList<Integer>();
      for( int i = 0 ; i < buffer.length ; i++ ){
        if( buffer[i] == target ){
          result.add( Integer.valueOf( i ) );
        }
      }
      return result;
    }
    return null;
  }
}
