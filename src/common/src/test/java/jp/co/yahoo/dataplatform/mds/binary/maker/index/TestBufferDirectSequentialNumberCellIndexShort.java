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
import java.nio.IntBuffer;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;

import jp.co.yahoo.dataplatform.mds.binary.maker.IDicManager;
import jp.co.yahoo.dataplatform.mds.spread.column.*;
import jp.co.yahoo.dataplatform.mds.spread.column.filter.*;

import jp.co.yahoo.dataplatform.schema.objects.*;

import jp.co.yahoo.dataplatform.mds.spread.column.index.ICellIndex;

public class TestBufferDirectSequentialNumberCellIndexShort{

  private class TestDicManager implements IDicManager {

    private final List<PrimitiveObject> dic;

    public TestDicManager( final List<PrimitiveObject> dic ){
      this.dic = dic;
    }

    @Override
    public PrimitiveObject get( final int index ) throws IOException{
      return dic.get( index );
    }

    @Override
    public int getDicSize() throws IOException{
      return dic.size();
    }

  }

  @Test
  public void T_short_filter_1() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.EQUAL , new ShortObj( (short) 1000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 20 );
    for( int i = 0,n=0 ; n < 100 ; i++,n+=5 ){
      assertEquals( result.get(i).intValue() , n );
    }
  }

  @Test
  public void T_short_filter_2() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.NOT_EQUAL , new ShortObj( (short) 1000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 80 );
    for( int i = 0,n=0 ; n < 100 ; i+=4,n+=5 ){
      assertEquals( result.get(i).intValue() , n + 1 );
      assertEquals( result.get(i+1).intValue() , n + 2 );
      assertEquals( result.get(i+2).intValue() , n + 3 );
      assertEquals( result.get(i+3).intValue() , n + 4 );
    }
  }

  @Test
  public void T_short_filter_3() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.LT , new ShortObj( (short) 2000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 20 );
    for( int i = 0,n=0 ; n < 100 ; i++,n+=5 ){
      assertEquals( result.get(i).intValue() , n );
    }
  }

  @Test
  public void T_short_filter_4() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.LE , new ShortObj( (short) 2000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 40 );
    for( int i = 0,n=0 ; n < 100 ; i+=2,n+=5 ){
      assertEquals( result.get(i).intValue() , n );
      assertEquals( result.get(i+1).intValue() , n+1 );
    }
  }

  @Test
  public void T_short_filter_5() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.GT , new ShortObj( (short) 4000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 20 );
    for( int i = 0,n=0 ; n < 100 ; i++,n+=5 ){
      assertEquals( result.get(i).intValue() , n+4 );
    }
  }

  @Test
  public void T_short_filter_6() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.GE , new ShortObj( (short) 4000 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result.size() , 40 );
    for( int i = 0,n=0 ; n < 100 ; i+=2,n+=5 ){
      assertEquals( result.get(i).intValue() , n+3 );
      assertEquals( result.get(i+1).intValue() , n+4 );
    }
  }

  @Test
  public void T_short_filter_7() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.GE , new LongObj( Long.valueOf( Short.MAX_VALUE ) + (long)1 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result , null );
  }

  @Test
  public void T_short_filter_8() throws IOException{
    List<PrimitiveObject> dic = new ArrayList<PrimitiveObject>();
    dic.add( new ShortObj( (short) 1000 ) );
    dic.add( new ShortObj( (short) 2000 ) );
    dic.add( new ShortObj( (short) 3000 ) );
    dic.add( new ShortObj( (short) 4000 ) );
    dic.add( new ShortObj( (short) 5000 ) );
    IntBuffer buffer = IntBuffer.allocate( 100 );
    for( int i = 0 ; i < 100 ; i++ ){
      buffer.put( i % 5 );
    }
    ICellIndex index = new BufferDirectSequentialNumberCellIndex( ColumnType.SHORT , new TestDicManager( dic ) , buffer );
    IFilter filter = new NumberFilter( NumberFilterType.GE , new LongObj( Long.valueOf( Short.MIN_VALUE ) - (long)1 ) );

    List<Integer> result = index.filter( filter );
    assertEquals( result , null );
  }

}
