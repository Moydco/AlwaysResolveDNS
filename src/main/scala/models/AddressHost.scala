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

package models

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import configs.ConfigService
import org.slf4j.LoggerFactory
import records.A
import utils.HostnameUtils

import scala.util.Random

@JsonIgnoreProperties(Array("typ"))
case class AddressHost(
								 @JsonProperty("class") cls: String = null,
								 @JsonProperty("name") name: String = null,
								 @JsonProperty("value") ips: Array[WeightedIP] = null,
								 @JsonProperty("ttl") timetolive: Long
								 ) extends Host("A") {
	val logger = LoggerFactory.getLogger("app")
	val randomizeRecords = ConfigService.config.getBoolean("randomizeRecords")

	def setName(newname: String) = AddressHost(cls, newname, ips, timetolive)

	override def equals(other: Any) = other match {
		case h: AddressHost => cls == h.cls && name == h.name && h.ips.forall(wip => ips.exists(_.ip == wip.ip))
		case _ => false
	}

	override def toAbsoluteNames(domain: ExtendedDomain) =
		new AddressHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), ips, timetolive)

	private def ipToLong(ip: String) = ip.split( """\.""").reverse.foldRight(0L) { case (part, total) => (total << 8) + part.toLong}

	protected def getRData =
		if (ips.size == 1) {
			logger.debug("Single A")
			ips(0).weightIP.map(ip => new A(ipToLong(ip), timetolive))
		}
		else if (randomizeRecords == true) {
			/**
			Se c'è un array di weighted ip (SBAGLIATO fare più record con lo stesso nome e weight diverso, basta un record
      con più values weighted) scegline uno a caso.
			  */
			logger.debug("Collapsing duplicate weighted As")
			val list = ips.map(wip => wip.weightIP.map(ip => new A(ipToLong(ip), timetolive))).flatten.toList
			Array[A](Random.shuffle(list).head)
		}
		else ips.map(wip => wip.weightIP.map(ip => new A(ipToLong(ip), timetolive))).flatten
}

case class WeightedIP(
								@JsonProperty("weight") weight: Int = 1,
								@JsonProperty("ip") ip: String = null
								) {

	def weightIP =
		if (weight < 1) Array[String]() else Array.tabulate(weight) { i => ip}
}