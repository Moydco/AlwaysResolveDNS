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
import scala.collection.JavaConversions._

import datastructures.DNSAuthoritativeSection
import models.ExtendedDomain
import configs.ConfigService

object QueryCountNotifier {
	var queryCountMap = new ConcurrentHashMap[String, Long]()

	val logger = LoggerFactory.getLogger("Httprequests")

	val API_KEY = ConfigService.config.getString("apiKey")
	val API_SECRET = ConfigService.config.getString("apiSecret")
	

	def incrementDomain(domain: String) = {
		val temp = queryCountMap.get(domain)
		if(temp==null)
			queryCountMap.put(domain, 1)
		else
			queryCountMap.put(domain, temp+1)
	}
}

class QueryCountNotifier extends TimerTask {
	import QueryCountNotifier._
	val logger = LoggerFactory.getLogger("app")

	val SERVER_ID = ConfigService.config.getString("serverID")
	val REGION = ConfigService.config.getString("region")
	val HTTP_SEND_QUERY_COUNT = ConfigService.config.getString("httpSendQueryCount")
	val HTTP_TIMEOUT = ConfigService.config.getInt("httpTimeout")

	override def run() {
		val m = new ObjectMapper()
		m.registerModule(DefaultScalaModule)
		val values = for( domain <- queryCountMap.keys ) yield (domain, queryCountMap.get(domain))
		val json = m.writeValueAsString(new JsonMessage(SERVER_ID, REGION, values))
		logger.debug(json)
		try {
	    	val temp = Http.postData(HTTP_SEND_QUERY_COUNT, json).header("content-type", "application/json")
				.option(HttpOptions.connTimeout(HTTP_TIMEOUT))
			//logger.debug(temp.toString)
	    } catch {
      		case ex: Exception => logger.warn("Error in query count post.")
	    }

	    // Resetta il contatore per ogni dominio
	    for( domain <- queryCountMap.keys ) queryCountMap.put(domain, 1)
	}
}

case class JsonMessage (serverID: String, region: String, queryCount: Iterator[(String, Long)])