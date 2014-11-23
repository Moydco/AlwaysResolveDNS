package datastructures

import scala.collection.immutable.TreeMap
import models.ExtendedDomain
import scala.collection.immutable.Map
import scala.annotation.tailrec

object DNSAuthoritativeSection extends DNSDomainStorage[ExtendedDomain] {

  protected var domains = TreeMap[String,Map[String, ExtendedDomain]]("." -> Map())

  /**
   * Cerca nei domini il nome. Le root entry sono il nome del dominio (record vuoti-> o meglio, i record vuoti non vanno bene,
   * devono essere con la @).
   *
   * @param typ
   * @param parts
   * @param storedMap
   * @param name
   * @return
   */
  @tailrec
  protected def findDomainName(typ: Int, parts: List[String], storedMap: Map[String, ExtendedDomain], name: Seq[String]): Option[ExtendedDomain] = 
    if(name.isEmpty) storedMap.get("").filter(d => parts.size - 1 != name.size || d.hasRootEntry(typ))
    else storedMap.get(name.mkString(".")) match {
      case Some(domain) => {
        if(parts.size - 1 != name.size || domain.hasRootEntry(typ)) Some(domain)
        else findDomainName(typ, parts, storedMap, name.tail)
      }
      case _ => findDomainName(typ, parts, storedMap, name.tail)
    }
  
  protected def addDomainEntry(domain: ExtendedDomain) = (domain.name -> domain)

}