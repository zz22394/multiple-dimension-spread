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
package jp.co.yahoo.dataplatform.mds.binary.maker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import jp.co.yahoo.dataplatform.mds.binary.BinaryDump;
import jp.co.yahoo.dataplatform.mds.binary.ColumnBinary;
import jp.co.yahoo.dataplatform.mds.binary.ColumnBinaryMakerConfig;
import jp.co.yahoo.dataplatform.mds.binary.ColumnBinaryMakerCustomConfigNode;
import jp.co.yahoo.dataplatform.mds.blockindex.BlockIndexNode;
import jp.co.yahoo.dataplatform.mds.blockindex.StringRangeBlockIndex;
import jp.co.yahoo.dataplatform.mds.binary.maker.cache.ByteBufferCache;
import jp.co.yahoo.dataplatform.mds.binary.maker.index.RangeStringIndex;
import jp.co.yahoo.dataplatform.mds.constants.PrimitiveByteLength;
import jp.co.yahoo.dataplatform.mds.spread.column.IColumn;
import jp.co.yahoo.dataplatform.mds.spread.column.ICell;
import jp.co.yahoo.dataplatform.mds.spread.column.PrimitiveCell;
import jp.co.yahoo.dataplatform.mds.spread.column.ColumnType;
import jp.co.yahoo.dataplatform.mds.inmemory.IMemoryAllocator;

public class RangeIndexStringToUTF8BytesColumnBinaryMaker extends UniqStringToUTF8BytesColumnBinaryMaker{

  @Override
  public ColumnBinary toBinary(final ColumnBinaryMakerConfig commonConfig , final ColumnBinaryMakerCustomConfigNode currentConfigNode , final IColumn column , final MakerCache makerCache ) throws IOException{
    ColumnBinaryMakerConfig currentConfig = commonConfig;
    if( currentConfigNode != null ){
      currentConfig = currentConfigNode.getCurrentConfig();
    }
    Map<String,Integer> dicMap = new HashMap<String,Integer>();
    List<Integer> columnIndexList = new ArrayList<Integer>();
    List<byte[]> stringList = new ArrayList<byte[]>();

    dicMap.put( null , Integer.valueOf(0) );
    stringList.add( new byte[0] );

    int totalLength = 0;
    int logicalTotalLength = 0;
    int rowCount = 0;
    int columnSize = column.size();
    String max = "";
    String min = "";
    byte hasNull = (byte)0;
    for( int i = 0 ; i < columnSize ; i++ ){
      ICell cell = column.get(i);
      String targetStr = null;
      if( cell.getType() != ColumnType.NULL ){
        hasNull = (byte)1;
        rowCount++;
        PrimitiveCell stringCell = (PrimitiveCell) cell;
        targetStr = stringCell.getRow().getString();
        if( targetStr != null ){
          logicalTotalLength += targetStr.length() * PrimitiveByteLength.CHAR_LENGTH;
        }
      }
      if( ! dicMap.containsKey( targetStr ) ){
        dicMap.put( targetStr , stringList.size() );
        byte[] stringBytes = targetStr.getBytes( "UTF-8" );
        stringList.add( stringBytes );
        totalLength += stringBytes.length;
        if( max.compareTo( targetStr ) < 0 ){
          max = targetStr;
        }
        if( min.isEmpty() || 0 < min.compareTo( targetStr ) ){
          min = targetStr;
        }
      }
      columnIndexList.add( dicMap.get( targetStr ) );
    }

    int rawSize = ( PrimitiveByteLength.INT_LENGTH * columnIndexList.size() ) + ( totalLength + ( PrimitiveByteLength.INT_LENGTH * stringList.size() ) ) + ( PrimitiveByteLength.INT_LENGTH * 2 );
    ICache toBinaryCache = makerCache.getCache( "to_binary_raw_cache" );
    if( ! ( toBinaryCache instanceof ByteBufferCache) ){
      toBinaryCache = new ByteBufferCache();
      makerCache.registerCache( "to_binary_raw_cache" , toBinaryCache );
    }
    ByteBuffer rawByteBuffer = ( (ByteBufferCache)toBinaryCache ).get();
    if( rawByteBuffer == null || rawByteBuffer.capacity() < rawSize ){
      rawByteBuffer = ByteBuffer.allocate( rawSize );
      toBinaryCache.register( rawByteBuffer );
    }
    rawByteBuffer.position(0);
    rawByteBuffer.putInt( PrimitiveByteLength.INT_LENGTH * columnIndexList.size() );
    BinaryDump.appendIntegerToByteBuffer( columnIndexList , rawByteBuffer );
    rawByteBuffer.putInt( totalLength + ( PrimitiveByteLength.INT_LENGTH * stringList.size() ) );
    BinaryDump.appendBytesToByteBuffer( stringList , totalLength , rawByteBuffer );

    int rawLength = rawByteBuffer.position();
    byte[] binaryRaw = rawByteBuffer.array();

    byte[] binary = currentConfig.compressorClass.compress( binaryRaw , 0 , rawLength );
    int minLength = PrimitiveByteLength.CHAR_LENGTH * min.length();
    int maxLength = PrimitiveByteLength.CHAR_LENGTH * max.length();
    int indexBinaryLength = PrimitiveByteLength.INT_LENGTH + PrimitiveByteLength.CHAR_LENGTH * min.length() + PrimitiveByteLength.INT_LENGTH + PrimitiveByteLength.CHAR_LENGTH * max.length() + PrimitiveByteLength.BYTE_LENGTH + PrimitiveByteLength.INT_LENGTH + binary.length;
    byte[] indexBinary = new byte[indexBinaryLength];
    ByteBuffer wrapBuffer = ByteBuffer.wrap( indexBinary , 0 , indexBinary.length );
    CharBuffer viewCharBuffer = wrapBuffer.asCharBuffer();
    int offset = 0;
    wrapBuffer.putInt( offset , minLength );
    offset += PrimitiveByteLength.INT_LENGTH;
    wrapBuffer.putInt( offset , maxLength );
    offset += PrimitiveByteLength.INT_LENGTH;
    viewCharBuffer.position( offset / PrimitiveByteLength.CHAR_LENGTH );
    viewCharBuffer.put( min.toCharArray() );
    offset += minLength;
    viewCharBuffer.position( offset / PrimitiveByteLength.CHAR_LENGTH );
    viewCharBuffer.put( max.toCharArray() );
    offset += maxLength;
    wrapBuffer.put( offset , hasNull );
    offset++;
    wrapBuffer.putInt( offset , binary.length );
    offset += PrimitiveByteLength.INT_LENGTH;
    wrapBuffer.position( offset );
    wrapBuffer.put( binary );

    return new ColumnBinary( this.getClass().getName() , currentConfig.compressorClass.getClass().getName() , column.getColumnName() , ColumnType.STRING , rowCount , binaryRaw.length , logicalTotalLength , dicMap.size() , indexBinary , 0 , indexBinaryLength , null );
  }

  @Override
  public IColumn toColumn( final ColumnBinary columnBinary , final IPrimitiveObjectConnector primitiveObjectConnector ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    CharBuffer viewCharBuffer = wrapBuffer.asCharBuffer();
    int offset = columnBinary.binaryStart;
    int minLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    int maxLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    viewCharBuffer.position( ( offset - columnBinary.binaryStart ) / PrimitiveByteLength.CHAR_LENGTH );
    char[] minCharArray = new char[ minLength / PrimitiveByteLength.CHAR_LENGTH ];
    viewCharBuffer.get( minCharArray );
    offset += minLength;
    viewCharBuffer.position( ( offset - columnBinary.binaryStart ) / PrimitiveByteLength.CHAR_LENGTH );
    char[] maxCharArray = new char[ maxLength / PrimitiveByteLength.CHAR_LENGTH ];
    viewCharBuffer.get( maxCharArray );
    offset += maxLength;
    byte hasNull = wrapBuffer.get( offset );
    offset++;
    int binaryLength = wrapBuffer.getInt( offset ); 
    offset += PrimitiveByteLength.INT_LENGTH;
    return new HeaderIndexLazyColumn( columnBinary.columnName , columnBinary.columnType , new StringColumnManager( columnBinary , primitiveObjectConnector , offset , binaryLength ) , new RangeStringIndex( new String( minCharArray ) , new String( maxCharArray ) , hasNull == (byte)1 ) );
  }

  @Override
  public void loadInMemoryStorage( final ColumnBinary columnBinary , final IMemoryAllocator allocator ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    int offset = columnBinary.binaryStart;
    int minLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    int maxLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    offset += minLength;
    offset += maxLength;
    offset++;
    int compressBinaryLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    loadInMemoryStorage( columnBinary , allocator , offset , compressBinaryLength );
  }

  @Override
  public void setBlockIndexNode( final BlockIndexNode parentNode , final ColumnBinary columnBinary ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    CharBuffer viewCharBuffer = wrapBuffer.asCharBuffer();
    int offset = columnBinary.binaryStart;
    int minLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    int maxLength = wrapBuffer.getInt( offset );
    offset += PrimitiveByteLength.INT_LENGTH;
    viewCharBuffer.position( ( offset - columnBinary.binaryStart ) / PrimitiveByteLength.CHAR_LENGTH );
    char[] minCharArray = new char[ minLength / PrimitiveByteLength.CHAR_LENGTH ];
    viewCharBuffer.get( minCharArray );
    offset += minLength;
    viewCharBuffer.position( ( offset - columnBinary.binaryStart ) / PrimitiveByteLength.CHAR_LENGTH );
    char[] maxCharArray = new char[ maxLength / PrimitiveByteLength.CHAR_LENGTH ];
    viewCharBuffer.get( maxCharArray );

    BlockIndexNode currentNode = parentNode.getChildNode( columnBinary.columnName );
    currentNode.setBlockIndex( new StringRangeBlockIndex( new String( minCharArray ) , new String( maxCharArray ) ) );
  }

}
