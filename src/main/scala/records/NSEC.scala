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
import models.NSECBlock
import org.slf4j.LoggerFactory
import payload.Name

/**
 * Created by manuel on 31/01/15.
 */
case class NSEC(
                  timetolive: Long,
                  nextName: List[Array[Byte]],
                  map: List[NSECBlock]
                  ) extends AbstractRecord {

  val description = "NSEC"

  def isEqualTo(any: Any) = any match {
    case r: NSEC => r.timetolive == timetolive && nextName == r.nextName && map == r.map
    case _ => false
  }

  def toByteArray = Name.toByteArray(nextName) ++ map.map(block => Array[Byte](block.block, block.length)
    ++ block.types).flatten

  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    (input._1 ++ toByteArray, input._2)
  }
}

object NSEC {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    logger.error("Should not be called")

    new NSEC(60, List[Array[Byte]](), List[NSECBlock]())
  }
}