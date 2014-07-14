package models

import records._
import payload.RRData
import scala.annotation.tailrec
import com.fasterxml.jackson.annotation.JsonIgnore

/*class Host(name: String, domain: ExtendedDomain, recordType: Int, ip: List[String] = List.empty[String])*/

abstract class Host(@JsonIgnore val typ: String) {
  val cls: String
  val name: String
  
  override def equals(obj: Any): Boolean
  
  protected def getRData: Any
  
  def toAbsoluteNames(domain: ExtendedDomain): Host
  
  def setName(newname: String): Host
  
  def toRData = 
    getRData match {
      case rd: AbstractRecord => Array(rd)
      case rd: Array[AbstractRecord] => rd
      case _ => throw new Error("Something went wrong")
    }
}