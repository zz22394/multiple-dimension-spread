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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;

import jp.co.yahoo.dataplatform.mds.compressor.FindCompressor;
import jp.co.yahoo.dataplatform.mds.compressor.ICompressor;
import jp.co.yahoo.dataplatform.mds.constants.PrimitiveByteLength;
import jp.co.yahoo.dataplatform.mds.spread.column.ICell;
import jp.co.yahoo.dataplatform.mds.spread.column.PrimitiveCell;
import jp.co.yahoo.dataplatform.mds.spread.column.IColumn;
import jp.co.yahoo.dataplatform.mds.spread.column.ColumnType;
import jp.co.yahoo.dataplatform.mds.spread.column.PrimitiveColumn;

import jp.co.yahoo.dataplatform.schema.objects.ByteObj;
import jp.co.yahoo.dataplatform.schema.objects.PrimitiveType;
import jp.co.yahoo.dataplatform.schema.objects.PrimitiveObject;

import jp.co.yahoo.dataplatform.mds.binary.ColumnBinary;
import jp.co.yahoo.dataplatform.mds.binary.ColumnBinaryMakerConfig;
import jp.co.yahoo.dataplatform.mds.binary.ColumnBinaryMakerCustomConfigNode;
import jp.co.yahoo.dataplatform.mds.binary.maker.index.RangeByteIndex;
import jp.co.yahoo.dataplatform.mds.blockindex.BlockIndexNode;
import jp.co.yahoo.dataplatform.mds.blockindex.ByteRangeBlockIndex;
import jp.co.yahoo.dataplatform.mds.inmemory.IMemoryAllocator;

public class RangeDumpByteColumnBinaryMaker extends DumpByteColumnBinaryMaker{

  private static final int HEADER_SIZE = ( PrimitiveByteLength.BYTE_LENGTH * 2 ) + PrimitiveByteLength.INT_LENGTH;

  @Override
  public ColumnBinary toBinary(final ColumnBinaryMakerConfig commonConfig , final ColumnBinaryMakerCustomConfigNode currentConfigNode , final IColumn column , final MakerCache makerCache ) throws IOException{
    ColumnBinaryMakerConfig currentConfig = commonConfig;
    if( currentConfigNode != null ){
      currentConfig = currentConfigNode.getCurrentConfig();
    }
    ByteBuffer nullFlagBuffer = ByteBuffer.allocate( column.size() );
    ByteBuffer floatBuffer = ByteBuffer.allocate( column.size() * PrimitiveByteLength.BYTE_LENGTH );
    int rowCount = 0;
    boolean hasNull = false;
    Byte min = Byte.MAX_VALUE;
    Byte max = Byte.MIN_VALUE;
    for( int i = 0 , n = 0 ; i < column.size() ; i++ , n += PrimitiveByteLength.BYTE_LENGTH ){
      ICell cell = column.get(i);
      if( cell.getType() == ColumnType.NULL ){
        hasNull = true;
        nullFlagBuffer.put( i , (byte)1 );
        continue;
      }
      rowCount++;
      PrimitiveCell byteCell = (PrimitiveCell) cell;
      Byte target = Byte.valueOf( byteCell.getRow().getByte() );
      floatBuffer.put( n , target );
      if( 0 < min.compareTo( target ) ){
        min = Byte.valueOf( target );
      }
      if( max.compareTo( target ) < 0 ){
        max = Byte.valueOf( target );
      }
    }

    byte[] binary;
    int rawLength;
    if( hasNull ){
      byte[] binaryRaw = convertBinary( nullFlagBuffer , floatBuffer , currentConfig );
      byte[] compressBinaryRaw = currentConfig.compressorClass.compress( binaryRaw , 0 , binaryRaw.length );
      rawLength = binaryRaw.length;
      
      binary = new byte[ HEADER_SIZE + compressBinaryRaw.length ];
      ByteBuffer wrapBuffer = ByteBuffer.wrap( binary );
      wrapBuffer.put( min );
      wrapBuffer.put( max );
      wrapBuffer.putInt( 0 );
      wrapBuffer.put( compressBinaryRaw );
    }
    else{
      byte[] binaryRaw = floatBuffer.array();
      rawLength = binaryRaw.length;
      byte[] compressBinaryRaw = currentConfig.compressorClass.compress( binaryRaw , 0 , binaryRaw.length );

      binary = new byte[ HEADER_SIZE + compressBinaryRaw.length ];
      ByteBuffer wrapBuffer = ByteBuffer.wrap( binary );
      wrapBuffer.put( min );
      wrapBuffer.put( max );
      wrapBuffer.putInt( 1 );
      wrapBuffer.put( compressBinaryRaw );
    }
    return new ColumnBinary( this.getClass().getName() , currentConfig.compressorClass.getClass().getName() , column.getColumnName() , ColumnType.BYTE , rowCount , rawLength , rowCount * PrimitiveByteLength.BYTE_LENGTH , -1 , binary , 0 , binary.length , null );
  }

  @Override
  public IColumn toColumn( final ColumnBinary columnBinary , final IPrimitiveObjectConnector primitiveObjectConnector ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    Byte min = Byte.valueOf( wrapBuffer.get() );
    Byte max = Byte.valueOf( wrapBuffer.get() );
    int type = wrapBuffer.getInt();
    if( type == 0 ){
      return new HeaderIndexLazyColumn( 
        columnBinary.columnName , 
        columnBinary.columnType , 
        new ByteColumnManager( 
          columnBinary , 
          primitiveObjectConnector , 
          columnBinary.binaryStart + HEADER_SIZE , 
          columnBinary.binaryLength - HEADER_SIZE ) , 
          new RangeByteIndex( min , max ) 
      );
    }
    else{
      return new HeaderIndexLazyColumn(
        columnBinary.columnName ,
        columnBinary.columnType ,
        new RangeByteColumnManager(
          columnBinary ,
          primitiveObjectConnector ,
          columnBinary.binaryStart + HEADER_SIZE ,
          columnBinary.binaryLength - HEADER_SIZE ) ,
          new RangeByteIndex( min , max )
      );
    }
  }

  @Override
  public void loadInMemoryStorage( final ColumnBinary columnBinary , final IMemoryAllocator allocator ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    wrapBuffer.position( PrimitiveByteLength.BYTE_LENGTH * 2 );
    int type = wrapBuffer.getInt();
    if( type == 0 ){
      loadInMemoryStorage( columnBinary , columnBinary.binaryStart + HEADER_SIZE , columnBinary.binaryLength - HEADER_SIZE , allocator );
      return;
    }
    ICompressor compressor = FindCompressor.get( columnBinary.compressorClassName );
    byte[] binary = compressor.decompress( columnBinary.binary , columnBinary.binaryStart + HEADER_SIZE , columnBinary.binaryLength - HEADER_SIZE );
    wrapBuffer = wrapBuffer.wrap( binary );
    for( int i = 0 ; i < columnBinary.rowCount ; i++ ){
      allocator.setByte( i , wrapBuffer.get() );
    }
    allocator.setValueCount( columnBinary.rowCount );
  }

  @Override
  public void setBlockIndexNode( final BlockIndexNode parentNode , final ColumnBinary columnBinary ) throws IOException{
    ByteBuffer wrapBuffer = ByteBuffer.wrap( columnBinary.binary , columnBinary.binaryStart , columnBinary.binaryLength );
    Byte min = Byte.valueOf( wrapBuffer.get() );
    Byte max = Byte.valueOf( wrapBuffer.get() );
    BlockIndexNode currentNode = parentNode.getChildNode( columnBinary.columnName );
    currentNode.setBlockIndex( new ByteRangeBlockIndex( min , max ) );
  }

  public class RangeByteDicManager implements IDicManager{

    private final IPrimitiveObjectConnector primitiveObjectConnector;
    private final byte[] buffer;
    private final int start;
    private final int length;

    public RangeByteDicManager( final IPrimitiveObjectConnector primitiveObjectConnector , final byte[] buffer , final int start , final int length ){
      this.primitiveObjectConnector = primitiveObjectConnector;
      this.buffer = buffer;
      this.start = start;
      this.length = length;
    }

    @Override
    public PrimitiveObject get( final int index ) throws IOException{
      return primitiveObjectConnector.convert( PrimitiveType.BYTE , new ByteObj( buffer[index+start] ) );
    }

    @Override
    public int getDicSize() throws IOException{
      return length;
    }

  }

  public class RangeByteColumnManager implements IColumnManager{

    private final IPrimitiveObjectConnector primitiveObjectConnector;
    private final ColumnBinary columnBinary;
    private final int binaryStart;
    private final int binaryLength;
    private PrimitiveColumn column;
    private boolean isCreate;

    public RangeByteColumnManager( final ColumnBinary columnBinary , final IPrimitiveObjectConnector primitiveObjectConnector , final int binaryStart , final int binaryLength ) throws IOException{
      this.columnBinary = columnBinary;
      this.primitiveObjectConnector = primitiveObjectConnector;
      this.binaryStart = binaryStart;
      this.binaryLength = binaryLength;
    }

    private void create() throws IOException{
      if( isCreate ){
        return;
      }
      ICompressor compressor = FindCompressor.get( columnBinary.compressorClassName );
      byte[] binary = compressor.decompress( columnBinary.binary , binaryStart , binaryLength );

      column = new PrimitiveColumn( columnBinary.columnType , columnBinary.columnName );
      IDicManager dicManager = new RangeByteDicManager( primitiveObjectConnector , binary , 0 , binary.length );
      column.setCellManager( new BufferDirectCellManager( ColumnType.INTEGER , dicManager , columnBinary.rowCount ) );

      isCreate = true;
    }

    @Override
    public IColumn get(){
      try{
        create();
      }catch( IOException e ){
        throw new UncheckedIOException( e );
      }
      return column;
    }

    @Override
    public List<String> getColumnKeys(){
      return new ArrayList<String>();
    }

    @Override
    public int getColumnSize(){
      return 0;
    }

  }

}
