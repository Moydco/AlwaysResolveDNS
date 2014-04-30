package models

import records.CNAME
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import utils.HostnameUtils
import scala.util.Random

@JsonIgnoreProperties(Array("typ"))
case class CnameHost(
  @JsonProperty("class") cls: String = null,
  @JsonProperty("name") name: String = null, 
  @JsonProperty("value") hostnames: Array[WeightedCNAME] = null,
  @JsonProperty("ttl") timetolive: Long  
) extends Host("CNAME") {
  protected def getRData = /*new CNAME((hostname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive)*/
  if(hostnames.size == 1)
  {
    hostnames(0).weightCNAME.map(cname => new CNAME((cname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive))
  }
  else if(randomizeRecords == true) 
  {
    /**
    Vedere commento per record A
    */
    val list = hostnames.map(wcname => wcname.weightCNAME.map(cname => new CNAME((cname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive) )).flatten.toList
    Random.shuffle(list).head
  }
  else hostnames.map(wcname => wcname.weightCNAME.map(cname => new CNAME((cname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive))).flatten

  def setName(newname: String) = CnameHost(cls, newname, hostnames, timetolive)
  
  def changeHostname(hostname: String) = CnameHost(cls, name, hostnames, timetolive)
  
  override def toAbsoluteNames(domain: ExtendedDomain) = 
    new CnameHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), HostnameUtils.absoluteHostName(hostname, domain.fullName), timetolive)
  
  override def equals(other: Any) = other match {
    case h: CnameHost => h.cls == cls && h.name == name && h.hostnames == hostnames
    case _ => false
  }
}

case class WeightedCNAME(
  @JsonProperty("weight") weight: Int = 1,
  @JsonProperty("cname") cname: String = null
) {

  def weightCNAME = 
    if(weight < 1) Array[String]() else Array.tabulate(weight) {i => cname}
}