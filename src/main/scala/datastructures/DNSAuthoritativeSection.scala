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