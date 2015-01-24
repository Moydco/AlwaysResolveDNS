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
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class RRSIG(
                  timetolive: Long,
                  typeCovered: Short,
                  algorithm: Byte,
                  labels: Byte,
                  originalTTL: Long,
                  signatureExpiration: Long,
                  signatureInception: Long,
                  keyTag: Short,
                  signerName: List[Array[Byte]],
                  signature: Array[Byte]
                   ) extends AbstractRecord {

  val description = "RRSIG"

  def isEqualTo(any: Any) = any match {
    case r: RRSIG => r.timetolive == timetolive && typeCovered == r.typeCovered &&
      algorithm == r.algorithm && labels == r.labels && originalTTL == r.originalTTL && signatureExpiration == r.signatureExpiration && signatureInception == r.signatureInception &&
      keyTag == r.keyTag && signerName == r.signerName && signature == r.signature
    case _ => false
  }

  def toByteArray = RRData.shortToBytes(typeCovered) ++ Array[Byte](algorithm) ++ Array[Byte](labels) ++ RRData.intToBytes(originalTTL.toInt) ++
    RRData.intToBytes(signatureExpiration.toInt) ++ RRData.intToBytes(signatureInception.toInt) ++ RRData.shortToBytes(keyTag) ++ Name.toByteArray(signerName) ++ signature

  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    (input._1 ++ toByteArray, input._2)
  }
}

object RRSIG {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    logger.error("Should not be called")

    val typeCovered = buf.readUnsignedShort.toShort
    val algorithm = buf.readByte()
    val labels = buf.readByte()
    val originalTTL = buf.readUnsignedInt()
    val signatureExpiration = buf.readUnsignedInt()
    val signatureInception = buf.readUnsignedInt()
    val keyTag = buf.readUnsignedShort.toShort
    val signerName = List(Array[Byte]())
    val signature = Array[Byte]()

    new RRSIG(60, typeCovered, algorithm, labels, originalTTL, signatureExpiration, signatureInception, keyTag, signerName, signature)
  }
}