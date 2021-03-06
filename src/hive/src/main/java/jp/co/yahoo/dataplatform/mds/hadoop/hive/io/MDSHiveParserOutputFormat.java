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
package jp.co.yahoo.dataplatform.mds.hadoop.hive.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.util.Progressable;

import org.apache.hadoop.hive.ql.io.HiveOutputFormat;

import jp.co.yahoo.dataplatform.config.Configuration;

public class MDSHiveParserOutputFormat extends FileOutputFormat<NullWritable,ParserWritable> implements HiveOutputFormat<NullWritable,ParserWritable>{

  @Override
  public RecordWriter<NullWritable, ParserWritable> getRecordWriter( final FileSystem ignored, final JobConf job, final String name, final Progressable progress) throws IOException {
    throw new RuntimeException("Should never be used");
  }

  @Override
  public org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter getHiveRecordWriter( final JobConf job , final Path outputPath , final Class<? extends Writable> valueClass , final boolean isCompressed , final Properties tableProperties , final Progressable progress ) throws IOException {
    FileSystem fs =   outputPath.getFileSystem( job );
    long dfsBlockSize = Math.max( fs.getDefaultBlockSize( outputPath ) , 1024 * 1024 * 256 );
    OutputStream out = fs.create(  outputPath , true , 4096 , fs.getDefaultReplication( outputPath ) , dfsBlockSize );

    Configuration config = new Configuration();
    return new MDSHiveRecordWriter( out , config );
  }

}
