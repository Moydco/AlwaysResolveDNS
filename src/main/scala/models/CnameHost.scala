package models

import records.CNAME
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import configs.ConfigService
import utils.HostnameUtils
import scala.util.Random

@JsonIgnoreProperties(Array("typ"))
case class CnameHost(
  @JsonProperty("class") cls: String = null,
  @JsonProperty("name") name: String = null, 
  @JsonProperty("value") hostnames: Array[WeightedCNAME] = null,
  @JsonProperty("ttl") timetolive: Long  
) extends Host("CNAME") {

  val randomizeRecords = ConfigService.config.getBoolean("randomizeRecords")

  def hostname = {
    if(hostnames.size == 1)
    {
      hostnames(0).cname
    }
    else if(randomizeRecords == true) 
    {
      val rand = Random.nextInt(hostnames.length)
      hostnames(rand).cname
    }
    else hostnames(0).cname
  }

  // In teoria questo metodo randomizza il cname in caso di query di un record cname puro, mentra l'altro metodo serve per
  // randomizzare la risoluzione standard.
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
    val list = hostnames.map(wcname => wcname.weightCNAME.map(cname => 
      new CNAME((cname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive) )).flatten
    val rand = Random.nextInt(hostnames.length)
    list(rand)
  }
  else hostnames.map(wcname => 
    wcname.weightCNAME.map(cname => new CNAME((cname.split("""\.""").map(_.getBytes) :+ Array[Byte]()).toList, timetolive))).flatten

  def setName(newname: String) = CnameHost(cls, newname, hostnames, timetolive)
  
  def changeHostname(hostname: String) = CnameHost(cls, name, hostnames, timetolive)

  override def toAbsoluteNames(domain: ExtendedDomain) = 
    new CnameHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), 
      hostnames.map{hostname => new WeightedCNAME(hostname.weight, HostnameUtils.absoluteHostName(hostname.cname, domain.fullName))}, timetolive)
  
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