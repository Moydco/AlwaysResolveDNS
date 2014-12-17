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

package utils

import models.ExtendedDomain

object HostnameUtils {
  def relativeHostName(qname: List[String], domain: ExtendedDomain) = {
    val hnm = qname.take(qname.lastIndexOfSlice(domain.nameParts)).mkString(".")
    if (hnm.length == 0 || hnm == "@") domain.fullName else hnm
  }

  def absoluteHostName(name: String, basename: String) = 
    if (name == "@") basename
    else if (name == null || name.endsWith(".")) name
    else name + "." + basename
}