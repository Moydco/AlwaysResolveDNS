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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import records.AAAA
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import utils.HostnameUtils
import scala.util.Random
import configs.ConfigService
import org.slf4j.LoggerFactory

@JsonIgnoreProperties(Array("typ"))
case class IPv6AddressHost(
  @JsonProperty("class") cls: String = null,
  @JsonProperty("name") name: String = null, 
  @JsonProperty("value") ips: Array[WeightedIP] = null,
  @JsonProperty("ttl") timetolive: Long  
) extends Host("AAAA") {
  val logger = LoggerFactory.getLogger("app")
  val randomizeRecords = ConfigService.config.getBoolean("randomizeRecords")

  def setName(newname: String) = IPv6AddressHost(cls, newname, ips, timetolive)
  
  override def toAbsoluteNames(domain: ExtendedDomain) = 
    IPv6AddressHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), ips, timetolive)
  
  override def equals(other: Any) = other match {
    case h: IPv6AddressHost => h.cls == cls && h.name == name && h.ips.forall(wip => ips.exists(_.ip == wip.ip))
    case _ => false
  }
  
  protected def getRData = 
    if(ips.size == 1)
    {
      logger.debug("Single AAAA")
      ips(0).weightIP.map(ip => new AAAA(ipToBytes(ip), timetolive))
    }
    else if(randomizeRecords == true) 
    {
      /**
      Se c'è un array di weighted ip (SBAGLIATO fare più record con lo stesso nome e weight diverso, basta un record
      con più values weighted) scegline uno a caso.
      */
      logger.debug("Collapsing duplicate weighted AAAAs")
      val list = ips.map(wip => wip.weightIP.map(ip => new AAAA(ipToBytes(ip), timetolive))).flatten.toList
      Array[AAAA](Random.shuffle(list).head)
    }
    else ips.map(wip => wip.weightIP.map(ip => new AAAA(ipToBytes(ip), timetolive))).flatten
    
  private def ipToBytes(ip: String) = {
    val parts =
      if (ip.count(_ == ':') == 7) ip.split(""":""").map(part => if(part != "") part else "0")
      else {
        val parts = ip.split(""":""")
        val (partsLeft, partsRight) = parts.filterNot(_ == "").splitAt(parts.indexOf(""))
        partsLeft ++ Array.fill(8 - partsLeft.length - partsRight.length)("0") ++ partsRight
      }
    hexToBytes(parts.map(completeString(_)).mkString)
  }
  
  private def hexToBytes(hexstr: String, bytes: Array[Byte] = Array()): Array[Byte] =
    if(hexstr.length == 0) bytes
    else hexToBytes(hexstr.takeRight(hexstr.length - 2),
      bytes :+ ((Character.digit(hexstr.head, 16) << 4) +
      Character.digit(hexstr.tail.head, 16)).toShort.toByte)
      
  private def completeString(str: String): String =
    if(str.length >= 4) str else completeString("0" + str)
}