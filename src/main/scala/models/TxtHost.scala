package models

import records.TXT
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import utils.HostnameUtils

@JsonIgnoreProperties(Array("typ"))
case class TxtHost(
  @JsonProperty("class") cls: String = null,
  @JsonProperty("name") name: String = null, 
  @JsonProperty("value") strings: Array[String],
  @JsonProperty("ttl") timetolive: Long  
) extends Host("TXT") {

  def setName(newname: String) = TxtHost(cls, newname, strings, timetolive)
  
  override def toAbsoluteNames(domain: ExtendedDomain) = 
    TxtHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), strings, timetolive)
  
  override def equals(other: Any) = other match {
    case h: TxtHost => h.cls == cls && h.name == name && h.strings.forall(str => strings.exists(_ == str))
    case _ => false
  }
  
  def getRData = new TXT(strings.map(_.getBytes), timetolive)

}