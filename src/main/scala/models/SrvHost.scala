package models

import scala.annotation.tailrec
import records.SRV
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import utils.HostnameUtils

@JsonIgnoreProperties(Array("typ"))
case class SrvHost(
  @JsonProperty("class") cls: String = null,
  @JsonProperty("name") name: String = null,
  @JsonProperty("priority") priority: Long = 0,
  @JsonProperty("weight") weight: Long = 0,
  @JsonProperty("port") port: Long = 0,
  @JsonProperty("target") target: String = null,
  @JsonProperty("ttl") timetolive: Long  
) extends Host("SRV") {
  
  def setName(newname: String) = SrvHost(cls, newname, priority, weight, port, target, timetolive)
  
  def updatePriority(newp: Long) = SrvHost(cls, name, newp, weight, port, target, timetolive)
  def updateWeight(neww: Long) = SrvHost(cls, name, priority, neww, port, target, timetolive)
  def updatePort(newpo: Long) = SrvHost(cls, name, priority, weight, newpo, target, timetolive)
  def updateTarget(newt: String) = SrvHost(cls, name, priority, weight, port, newt, timetolive)
  
  override def toAbsoluteNames(domain: ExtendedDomain) = 
    SrvHost(cls, HostnameUtils.absoluteHostName(name, domain.fullName), priority, weight, port, target, timetolive)
  
  override def equals(other: Any) = other match {
    case h: SrvHost => h.cls == cls && h.name == name && 
      h.priority == priority && h.weight == weight && h.port == port && h.target == target
    case _ => false
  }
    
  @JsonIgnore
  protected def getRData = 
    new SRV(priority, weight, port, (target.split("""\.""").map(_.getBytes) :+ Array(0.toByte)).toList , timetolive)
}