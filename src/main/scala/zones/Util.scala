/*******************************************************************************
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
 ******************************************************************************/
package zones
import org.slf4j.LoggerFactory


object Util {
  
  val logger = LoggerFactory.getLogger("app")
  
  /*def fromJsonFile(fileName: String) = {    
    val jsonInput = io.Source.fromFile(fileName).mkString
    val parsedObj = parse[Zone](jsonInput)
    parsedObj    
  }
  
  
  def toJsonFile(fileName: String, zone: Zone) = {    
    val zoneJson = generate(zone)
    val output = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(fileName)))
    output.write(zoneJson)
    output.flush
    output.close    
  }*/
  

}