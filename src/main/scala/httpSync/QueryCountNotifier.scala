/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package httpSync

import scalaj.http.{Http, HttpOptions}
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.JsonInclude
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.TimerTask
import scala.collection.concurrent.TrieMap

import datastructures.DNSAuthoritativeSection
import models.ExtendedDomain
import configs.ConfigService

object QueryCountNotifier {
	val logger = LoggerFactory.getLogger("Httprequests")

	val API_KEY = ConfigService.config.getString("apiKey")
	val API_SECRET = ConfigService.config.getString("apiSecret")
	
	var queryCountMap = new TrieMap[String, Long]()

	// Inizializza a zero tutti i domini. In caso di aggiunta di nuovi domini andranno refreshati.
	DNSAuthoritativeSection.getDomainNames.foreach(queryCountMap.put(_, 0))

	// Controlla solo i domini che non ci sono, e rimuovi quelli cancellati
	def refreshDomains() = {
		DNSAuthoritativeSection.getDomainNames.filter(!queryCountMap.contains(_)).foreach(queryCountMap.put(_, 0))
		queryCountMap.filterNot(x=>DNSAuthoritativeSection.getDomainNames.contains(x._1)).foreach(x=>queryCountMap.remove(x._1))
		queryCountMap.foreach(x=>logger.debug(x._1))
	}
	
	def incrementDomain(domain: String) = {
		val dom = queryCountMap.get(domain)
		dom match {
			case Some(count) => queryCountMap.put(domain, count+1)
			case None => queryCountMap.put(domain, 1)
		}
	}
}

class QueryCountNotifier extends TimerTask {
	import QueryCountNotifier._
	val logger = LoggerFactory.getLogger("app")

	val SERVER_ID = ConfigService.config.getString("serverID")
	val REGION = ConfigService.config.getString("region")
	val HTTP_SEND_QUERY_COUNT = ConfigService.config.getString("httpSendQueryCount")
	val HTTP_TIMEOUT = ConfigService.config.getInt("httpTimeoutForQuerySend")

	override def run() {
		val m = new ObjectMapper()
		m.registerModule(DefaultScalaModule)
		val values = queryCountMap.toIterator
		val json = m.writeValueAsString(new JsonMessage(SERVER_ID, REGION, values))
		logger.debug(json)
		try {
	   	//  	val temp = Http.postData(HTTP_SEND_QUERY_COUNT, json).header("content-type", "application/json")
				// .option(HttpOptions.connTimeout(HTTP_TIMEOUT))

			// Attenzione che l'indirizzo deve essere comprensivo di http://
	    	val temp = Http.post(HTTP_SEND_QUERY_COUNT).params("json" -> json, "api_key" -> API_KEY, "api_secret" -> API_SECRET)
				.option(HttpOptions.connTimeout(HTTP_TIMEOUT)).asString
			//logger.debug(temp.toString)
	    } catch {
      		case ex: Exception => logger.warn("Error in query count post." + ex.getMessage());
	    }

	    // Resetta il contatore per ogni dominio
	    for( domain <- queryCountMap.keys ) queryCountMap.put(domain, 0)
	}
}

case class JsonMessage (serverID: String, region: String, queryCount: Iterator[(String, Long)])