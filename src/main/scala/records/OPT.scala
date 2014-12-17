/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

case class OPT(timetolive: Long = 0) extends AbstractRecord {

   val description = "OPT"

   lazy val toByteArray = Array[Byte]()

   def isEqualTo(any: Any) = any match {
      case _ => false
   }

   def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = (input._1 ++ toByteArray, input._2)
}

object OPT {

   val logger = LoggerFactory.getLogger("app")

   def apply(buf: ByteBuf, recordclass: Int, size: Int) = {
      new OPT()
   }
}