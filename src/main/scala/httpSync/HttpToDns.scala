/**
 * Copyright 2013-2015, AlwaysResolve Project (alwaysresolve.org), MOYD.CO LTD
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

package httpSync

import java.text.SimpleDateFormat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import configs.ConfigService
import datastructures.DNSAuthoritativeSection
import models.ExtendedDomain
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory

import scalaj.http.{Http, HttpOptions}

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
			case e: Exception => {
				logger.error("Unable to retrieve zones list"); System.exit(1);
			}
		}
		zones = temp.split(" ")
		logger.debug(zones.toString)
	}

	def getZoneFromHttp(zonename: String): Option[ExtendedDomain] = {
		val temp = Http(HTTP_REQUEST_ZONE).param("zone", zonename).param("region", REGION)
			.param("api_key", API_KEY).param("api_secret", API_SECRET)
			.option(HttpOptions.connTimeout(HTTP_TIMEOUT)).option(HttpOptions.readTimeout(HTTP_TIMEOUT))
		logger.debug("ResponseCode from " + zonename + " update: " + temp.responseCode.toString)
		if (temp.responseCode != 404) {
			try {
				Option(Json.readValue(temp.asString, classOf[ExtendedDomain]))
			}
			catch {
				case ex: JsonParseException => {
					logger.error("Broken json: " + ExceptionUtils.getMessage(ex) + " " + ExceptionUtils.getStackTrace(ex))
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
		loadDataOfType(zones, classOf[ExtendedDomain]) {
			DNSAuthoritativeSection.setDomain(_)
		}
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
			case ex: JsonParseException => logger.warn("Broken json: " + ExceptionUtils.getMessage(ex) + " " + ExceptionUtils.getStackTrace(ex))
		}

}