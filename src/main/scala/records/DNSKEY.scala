/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
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
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class DNSKEY(
					  flags: Short,
					  protocol: Byte,
					  algorithm: Byte,
					  publicKey: List[Array[Byte]],
					  timetolive: Long //niente valore di default perchè scala rompe il cazzo. Impostato a mano sotto
					  ) extends AbstractRecord {

	val description = "DNSKEY"

	def isEqualTo(any: Any) = any match {
		case r: DNSKEY => r.flags == flags && r.protocol == protocol && r.algorithm == algorithm && r.publicKey == publicKey
		case _ => false
	}

	def toByteArray = RRData.shortToBytes(flags.toShort) ++ Array[Byte](protocol) ++ Array[Byte](algorithm) ++ publicKey.head

	/**
	 * Non comprime niente in realtà, però bisogna che ci sia.
	 * @param input
	 * @return
	 */
	def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
		(input._1 ++ RRData.shortToBytes(flags) ++ Array[Byte](protocol) ++ Array[Byte](algorithm) ++ publicKey.head, input._2)
	}
}

object DNSKEY {

	val logger = LoggerFactory.getLogger("app")

	def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
		logger.error("Should not be called")

		val flags = buf.readUnsignedShort.toShort
		val protocol = buf.readByte()
		val algorithm = buf.readByte()
		val publicKey = List(Array[Byte]())

		new DNSKEY(flags, protocol, algorithm, publicKey, 60)
	}
}