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

package records

import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory

case class NULL(record: Array[Byte], timetolive: Long = 60) extends AbstractRecord {

  val description = "NULL"
  
  lazy val toByteArray = record

  def isEqualTo(any: Any) = any match {
    case r: NULL => r.record.deep == record.deep
    case _ => false
  }
  
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)
}

object NULL {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
    val record = recordclass match {
      // IN
      case 1 => {
        val marray = new Array[Byte](size) // A 128 bit IPv6 address = network byte order (high-order byte first).
        buf.readBytes(marray);
        marray
      }
      // *
      case 255 => null // not implemented yet
      case _ => throw new Error("Unknown record type")
    }
    new NULL(record)
  }
}
