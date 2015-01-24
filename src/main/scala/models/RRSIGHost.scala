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
import records.RRSIG
import utils.HostnameUtils

@JsonIgnoreProperties(Array("typ"))
case class RRSIGHost(
                      @JsonProperty("class") cls: String = null,
                      @JsonProperty("name") name: String = null,
                      @JsonProperty("ttl") timeToLive: Long,
                      @JsonProperty("typeCovered") typeCovered: Short,
                      @JsonProperty("algorithm") algorithm: Byte,
                      @JsonProperty("labels") labels: Byte,
                      @JsonProperty("originalTTL") originalTTL: Long,
                      @JsonProperty("signatureExpiration") signatureExpiration: Long,
                      @JsonProperty("signatureInception") signatureInception: Long,
                      @JsonProperty("keyTag") keyTag: Short,
                      @JsonProperty("signerName") signerName: String,
                      @JsonProperty("signature") signature: String
                      ) extends Host("RRSIG") {
  val logger = LoggerFactory.getLogger("app")

  def setName(newName: String) = RRSIGHost(cls, newName, timeToLive, typeCovered, algorithm, labels, originalTTL, signatureExpiration, signatureInception, keyTag, signerName, signature)

  override def equals(other: Any) = other match {
    case h: RRSIGHost => {
      cls == h.cls && name == h.name && typeCovered == h.typeCovered &&
        algorithm == h.algorithm && labels == h.labels && originalTTL == h.originalTTL && signatureExpiration == h.signatureExpiration && signatureInception == h.signatureInception &&
        keyTag == h.keyTag && signerName == h.signerName && signature == h.signature
    }
    case _ => false
  }

  override def toAbsoluteNames(domain: ExtendedDomain) =
    new RRSIGHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), timeToLive, typeCovered, algorithm, labels, originalTTL, signatureExpiration, signatureInception, keyTag, signerName, signature)

  protected def getRData = {
    new RRSIG( timeToLive, typeCovered, algorithm, labels, originalTTL, signatureExpiration, signatureInception, keyTag, (signerName.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, signature.getBytes() )
  }

}