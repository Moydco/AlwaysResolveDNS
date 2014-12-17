/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
 * This file incorporates work covered by the following copyright and permission notice:
 *
 * Copyright 2012 silenteh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// package frames;


// import org.jboss.netty.buffer.ChannelBuffer;
// import org.jboss.netty.channel.Channel;
// import org.jboss.netty.channel.ChannelHandlerContext;
// import org.jboss.netty.handler.codec.frame.FrameDecoder;

// import payload.Message;

// public class DnsMessageDecoder extends FrameDecoder {

// 	   protected Message decode(ChannelHandlerContext ctx,
// 	                           Channel channel,
// 	                           ChannelBuffer buf) throws Exception {

// 	     // Make sure if the length field was received.
// 	     if (buf.readableBytes() < 4) {
// 	        // The length field was not received yet - return null.
// 	        // This method will be invoked again when more packets are
// 	        // received and appended to the buffer.
// 	        return null;
// 	     }

// 	     // The length field is in the buffer.

// 	     // Mark the current buffer position before reading the length field
// 	     // because the whole frame might not be in the buffer yet.
// 	     // We will reset the buffer position to the marked position if
// 	     // there's not enough bytes in the buffer.
// 	     buf.markReaderIndex();

// 	     // Read the length field.
// 	     int length = buf.readInt();

// 	     // Make sure if there's enough bytes in the buffer.
// 	     if (buf.readableBytes() < length) {
// 	        // The whole bytes were not received yet - return null.
// 	        // This method will be invoked again when more packets are
// 	        // received and appended to the buffer.

// 	        // Reset to the marked position to read the length field again
// 	        // next time.
// 	        buf.resetReaderIndex();

// 	        return null;
// 	     }

// 	     // There's enough bytes in the buffer. Read it.
// 	     //ChannelBuffer frame = buf.readBytes(length);

// 	     // Successfully decoded a frame.  Return the decoded frame.
// 	     return null;
// 	   }

// }
