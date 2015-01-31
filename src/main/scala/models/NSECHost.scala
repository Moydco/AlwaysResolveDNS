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

package models

import com.fasterxml.jackson.annotation.{JsonCreator, JsonIgnoreProperties, JsonProperty}
import org.slf4j.LoggerFactory
import records.NSEC
import utils.HostnameUtils

@JsonIgnoreProperties(Array("typ"))
case class NSECHost(
                     @JsonProperty("class") cls: String = null,
                     @JsonProperty("name") name: String = null,
                     @JsonProperty("ttl") timeToLive: Long,
                     @JsonProperty("nextName") nextName: String,
                     @JsonProperty("rdata") map: List[NSECBlock]
                     ) extends Host("NSEC") {
  val logger = LoggerFactory.getLogger("app")

  def setName(newName: String) = NSECHost(cls, newName, timeToLive, nextName, map)

  override def equals(other: Any) = other match {
    case h: NSECHost => {
      cls == h.cls && name == h.name && nextName == h.nextName && map == h.map
    }
    case _ => false
  }

  override def toAbsoluteNames(domain: ExtendedDomain) = {
    new NSECHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), timeToLive,
      nextName, map)
  }

  protected def getRData = {
    new NSEC(timeToLive, (nextName.split( """\.""").map(_.getBytes) :+ Array[Byte]()).toList, map)
  }

}

class NSECBlock(@JsonProperty("block") bl: Integer,
                @JsonProperty("length") l: Integer,
                @JsonProperty("types") t: List[Integer]) {
  def block() = bl.toByte
  def length = l.toByte
  def types = t.map(x => x.toByte)
}