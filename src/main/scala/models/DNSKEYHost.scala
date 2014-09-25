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
		new DNSKEY(flags, protocol, algorithm, (Array(publicKey.getBytes())).toList, timeToLive)
	}

}