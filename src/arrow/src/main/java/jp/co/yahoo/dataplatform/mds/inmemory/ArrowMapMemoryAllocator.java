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
package jp.co.yahoo.dataplatform.mds.inmemory;

import java.io.IOException;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.complex.MapVector;

import jp.co.yahoo.dataplatform.mds.spread.column.ColumnType;

public class ArrowMapMemoryAllocator implements IMemoryAllocator{

  private final MapVector vector;
  private final BufferAllocator allocator;

  public ArrowMapMemoryAllocator( final BufferAllocator allocator , final MapVector vector ){
    this.allocator = allocator;
    this.vector = vector;
    vector.allocateNew();
  }

  @Override
  public void setNull( final int index ) throws IOException{
  }

  @Override
  public void setBoolean( final int index , final boolean value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setBoolean()" );
  }

  @Override
  public void setByte( final int index , final byte value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setByte()" );
  }

  @Override
  public void setShort( final int index , final short value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setShort()" );
  }

  @Override
  public void setInteger( final int index , final int value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setInteger()" );
  }

  @Override
  public void setLong( final int index , final long value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setLong()" );
  }

  @Override
  public void setFloat( final int index , final float value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setFloat()" );
  }

  @Override
  public void setDouble( final int index , final double value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setDouble()" );
  }

  @Override
  public void setBytes( final int index , final byte[] value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setBytes()" );
  }

  @Override
  public void setBytes( final int index , final byte[] value , final int start , final int length ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setBytes()" );
  }

  @Override
  public void setString( final int index , final String value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setString()" );
  }

  @Override
  public void setString( final int index , final char[] value ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setString()" );
  }

  @Override
  public void setString( final int index , final char[] value , final int start , final int length ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setString()" );
  }

  @Override
  public void setArrayIndex( final int index , final int start , final int length ) throws IOException{
    throw new UnsupportedOperationException( "Unsupported method setArrayIndex()" );
  }

  @Override
  public void setValueCount( final int count ) throws IOException{
    vector.getMutator().setValueCount( count );
  }

  @Override
  public int getValueCount() throws IOException{
    return vector.getAccessor().getValueCount();
  }

  @Override
  public IMemoryAllocator getChild( final String columnName , final ColumnType type ) throws IOException{
    return ArrowMemoryAllocatorFactory.getFromMapVector( type , columnName , allocator , vector );
  }

}
