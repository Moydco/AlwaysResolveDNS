package httpSync

import scalaj.http.{Http, HttpOptions}
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.JsonInclude
import java.text.SimpleDateFormat

import datastructures.DNSAuthoritativeSection
import domainio.JsonIO
import domainio.DomainValidationService
import models.ExtendedDomain
import utils.{NotifyUtil, SerialParser}
import configs.ConfigService

object HttpToDns {
	var zones = Array("")

	val logger = LoggerFactory.getLogger("Httprequests")

	val HTTP_REQUEST_LIST = ConfigService.config.getString("httpRequestList")
	val HTTP_REQUEST_ZONE = ConfigService.config.getString("httpRequestZone")
	val API_KEY = ConfigService.config.getString("apiKey")
	val API_SECRET = ConfigService.config.getString("apiSecret")
	val REGION = ConfigService.config.getString("region")
	val HTTP_TIMEOUT = ConfigService.config.getInt("httpTimeoutForZoneUpdate")

	private val Json = {
	    val m = new ObjectMapper()
	    m.registerModule(DefaultScalaModule)
	    m.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	    m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
	    m
	}	

	def getZonesNames = {
		var temp = ""
		try { 
		  temp = Http(HTTP_REQUEST_LIST).param("api_key", API_KEY).param("api_secret", API_SECRET)
			.option(HttpOptions.connTimeout(HTTP_TIMEOUT)).option(HttpOptions.readTimeout(HTTP_TIMEOUT)).asString
		} catch {
		  case e: Exception => {logger.error("Unable to retrieve zones list"); System.exit(1);}
		}
		zones = temp.split(" ")
		logger.debug(zones.toString)
	}

	def getZoneFromHttp(zonename: String):Option[ExtendedDomain] = {
    	val temp = Http(HTTP_REQUEST_ZONE).param("zone", zonename).param("region", REGION)
    		.param("api_key", API_KEY).param("api_secret", API_SECRET)
			.option(HttpOptions.connTimeout(HTTP_TIMEOUT)).option(HttpOptions.readTimeout(HTTP_TIMEOUT))
		logger.debug("ResponseCode from "+zonename+" update: "+temp.responseCode.toString)
		if(temp.responseCode != 404)
		{
			try {
				Option(Json.readValue(temp.asString, classOf[ExtendedDomain]))
			}
			catch {
				case ex: JsonParseException => {
					logger.error("Broken json: " + temp)
					None
				}
			}
		}
		else
			None
	}

	def loadZonesInMemory = {
		loadData
	}

	/*def loadSingleZone(zone: String) = {
		loadDataOfType(Array(zone), classOf[ExtendedDomain]) { DNSAuthoritativeSection.setDomain(_) }
	}*/
  
  	private def loadData = {
	    loadDataOfType(zones, classOf[ExtendedDomain]) { DNSAuthoritativeSection.setDomain(_) }
	    //loadDataOfType(cacheDataPath, classOf[ExtendedDomain]) { DNSCache.setDomain(_) }
	    DNSAuthoritativeSection.logDomains
	}
  
	private def loadDataOfType[T](zones: Array[String], typ: Class[T])(fn: T => Unit) = {
		zones.foreach(loadItem(_, typ)(fn))
	}
  
  	private def loadItem[T](zonename: String, typ: Class[T])(fn: T => Any) = 
    try {
    	val temp = Http(HTTP_REQUEST_ZONE).param("zone", zonename).param("region", REGION)
    		.param("api_key", API_KEY).param("api_secret", API_SECRET)
			.option(HttpOptions.connTimeout(HTTP_TIMEOUT)).option(HttpOptions.readTimeout(HTTP_TIMEOUT)).asString
		logger.debug(temp)
		val item = Json.readValue(temp, typ)
		fn(item)
    } catch {
      case ex: JsonParseException => logger.warn("Broken json: " /*+ file.getAbsolutePath*/)
    }

}