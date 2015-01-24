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

package models

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import org.slf4j.LoggerFactory
import records.DNSKEY
import utils.HostnameUtils

@JsonIgnoreProperties(Array("typ"))
case class DNSKEYHost(
								@JsonProperty("class") cls: String = null,
								@JsonProperty("name") name: String = null,
								@JsonProperty("ttl") timeToLive: Long,
								@JsonProperty("flags") flags: Short,
								@JsonProperty("protocol") protocol: Byte,
								@JsonProperty("algorithm") algorithm: Byte,
								@JsonProperty("publicKey") publicKey: String
								) extends Host("DNSKEY") {
	val logger = LoggerFactory.getLogger("app")

	def setName(newName: String) = DNSKEYHost(cls, newName, timeToLive, flags, protocol, algorithm, publicKey)

	override def equals(other: Any) = other match {
		case h: DNSKEYHost => {
			cls == h.cls && name == h.name && flags == h.flags &&
				protocol == h.protocol && algorithm == h.algorithm && publicKey == h.publicKey
		}
		case _ => false
	}

	override def toAbsoluteNames(domain: ExtendedDomain) =
		new DNSKEYHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), timeToLive, flags, protocol, algorithm, publicKey)

	protected def getRData = {
		new DNSKEY(flags, protocol, algorithm, publicKey.getBytes(), timeToLive)
	}

}