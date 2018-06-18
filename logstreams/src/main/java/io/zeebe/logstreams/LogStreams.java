/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.logstreams;

import io.zeebe.logstreams.fs.FsSnapshotStorageBuilder;
import io.zeebe.logstreams.impl.LogStreamBuilder;
import io.zeebe.logstreams.processor.StreamProcessor;
import io.zeebe.logstreams.processor.StreamProcessorBuilder;
import io.zeebe.util.buffer.BufferUtil;
import org.agrona.DirectBuffer;

public class LogStreams
{
    public static LogStreamBuilder createFsLogStream(final DirectBuffer topicName, final int partitionId)
    {
        return new LogStreamBuilder(topicName, partitionId).logName(BufferUtil.bufferAsString(topicName) + "-" + partitionId);
    }

    public static FsSnapshotStorageBuilder createFsSnapshotStore(String rootPath)
    {
        return new FsSnapshotStorageBuilder(rootPath);
    }

    public static StreamProcessorBuilder createStreamProcessor(String name, int id, StreamProcessor streamProcessor)
    {
        return new StreamProcessorBuilder(id, name, streamProcessor);
    }

}