package messaging

import com.rabbitmq.client.{ConnectionFactory, QueueingConsumer}
import configs.ConfigService
import datastructures.DNSAuthoritativeSection
import domainio.{DomainValidationService, JsonIO}
import httpSync.{HttpToDns, QueryCountNotifier}
import models.ExtendedDomain
import org.slf4j.LoggerFactory
import utils.{NotifyUtil, SerialParser}
import org.apache.commons.lang3.exception.ExceptionUtils

import scala.collection.mutable

class Rabbit extends Runnable {

	val logger = LoggerFactory.getLogger("Rabbit notifier")

	val EXCHANGE_NAME = ConfigService.config.getString("exchangeName")
	val HOST = ConfigService.config.getString("rabbitmqHost")
	val LOAD_FROM_HTTP = ConfigService.config.getBoolean("httpRetrievalEnabled")

	def run() {
		try {

			val factory = new ConnectionFactory()
			factory.setHost(HOST)
			val connection = factory.newConnection()
			val channel = connection.createChannel()

			channel.exchangeDeclare(EXCHANGE_NAME, "fanout")
			val queueName = channel.queueDeclare().getQueue()
			channel.queueBind(queueName, EXCHANGE_NAME, "")

			logger.debug("Rabbitmq waiting for messages.")

			val consumer = new QueueingConsumer(channel)
			channel.basicConsume(queueName, true, consumer)

			while (true) {
				try {
					val delivery = consumer.nextDelivery()
					val message = new String(delivery.getBody())
					logger.debug(message)

					val key = message.substring(0, message.indexOf("+"))
					val content = message.substring(message.indexOf("+") + 1)

					// if(key != "data" && key != "delete" && key != "update")
					//   throw new java.text.ParseException("Rabbit message not starting with data, update or delete.", 0)

					key match {
						case "update" => updateZone(content)
						case "delete" => deleteZone(content)
						case "data" => domainUpdate(mutable.Map(key -> content))
						case _ => throw new java.text.ParseException("Rabbit message not starting with data, update or delete.", 0)
					}

					QueryCountNotifier.refreshDomains()

					// val map = mutable.Map.empty[String, String]
					// map(key)=content

					// domainUpdate(map)
				} catch {
					case e: java.lang.StringIndexOutOfBoundsException => logger.error("Wrong format of Rabbit message. In most cases, the plus is missing.")
					case e: java.text.ParseException => logger.error(e.getMessage)
					case e: com.rabbitmq.client.ShutdownSignalException => logger.error("Rabbit server was turned off."); System.exit(1);
					case e:Throwable => logger.error("Error in zone update request: " + ExceptionUtils.getMessage(e) + " " + ExceptionUtils.getStackTrace(e))
				}
			}
		} catch {
			case e: java.net.ConnectException => logger.error("Unable to connect to Rabbit server."); System.exit(1);
			// La prossima eccezione è "shadowed" dal catch più interno
			case e: com.rabbitmq.client.ShutdownSignalException => logger.error("Rabbit server was turned off."); System.exit(1);
			//case _:Throwable => logger.error("Generic error, turning off."); System.exit(1);
		}
	}

	def updateZone(zone: String) = {
		try {
			if (LOAD_FROM_HTTP == true) {
				val temp = HttpToDns.getZoneFromHttp(zone)
				temp match {
					case Some(zoneUpdated) => {
						DNSAuthoritativeSection.removeDomain(zone.split( """\.""").toList)
						DNSAuthoritativeSection.setDomain(zoneUpdated)
						logger.debug("Zone " + zone + " set")
					}
					case None => logger.error("Zone " + zone + " not updated")
				}
			}
			else {
				val map = mutable.Map.empty[String, String]
				map("data") = zone
				domainUpdate(map)
			}
		} catch {
			case e: Throwable => logger.error("Error in zone update for: " + zone + " of type: " + ExceptionUtils.getMessage(e) + " " + ExceptionUtils.getStackTrace(e))
		}
	}

	def deleteZone(zone: String) = {
		DNSAuthoritativeSection.removeDomain(zone.split( """\.""").toList)
		logger.debug("Zone list before removing: " + HttpToDns.zones.foldLeft("")((a, b) => a + " " + b))
		HttpToDns.zones = HttpToDns.zones diff Array(zone)
		logger.debug("Zone list after removing: " + HttpToDns.zones.foldLeft("")((a, b) => a + " " + b))
	}

	def domainUpdate(updateMap: scala.collection.mutable.Map[String, String]) = {
		val data = updateMap
		if (data.get("data") != None) {
			val domainCandidate = try {
				val domain = JsonIO.Json.readValue(data("data"), classOf[ExtendedDomain])
				domain.settings.foldRight(domain) {
					case (soa, domain) =>
						val newSoa = soa.updateSerial(
							if (soa.serial == null || soa.serial == "") SerialParser.generateNewSerial.toString
							else SerialParser.updateSerial(soa.serial).toString)
						domain.removeHost(soa).addHost(newSoa)
				}
			} catch {
				case ex: Exception => throw ex
			}
			val replaceFilename = data.get("replace_filename").getOrElse(null)

			if (replaceFilename != null) {
				//DNSCache.removeDomain(replaceFilename.split("""\.""").toList)
				DNSAuthoritativeSection.removeDomain(replaceFilename.split( """\.""").toList)
				//JsonIO.removeAuthData(replaceFilename)
			}
			val domains = DomainValidationService.reorganize(domainCandidate)
			//DNSCache.setDomain(domains.head)
			DNSAuthoritativeSection.setDomain(domains.head)
			//JsonIO.storeAuthData(domains.head)
			NotifyUtil.notify(domainCandidate)

		} else {
			throw new Exception("Unidentified error")
		}
	}

	// NON usare, contiene ancora la validazione della zona che è rotta.
	// def domainUpdate(updateMap: scala.collection.mutable.Map[String, String]) = {
	//   val data = updateMap
	//   // if (data.get("delete") != None)
	//   // {
	//   //   DNSAuthoritativeSection.removeDomain(data("delete").split("""\.""").toList)
	//   //   logger.debug("Zone list before removing: " + HttpToDns.zones.foldLeft("")((a,b)=> a+" "+b) )
	//   //   HttpToDns.zones = HttpToDns.zones diff Array(data("delete"))
	//   //   logger.debug("Zone list after removing: "+ HttpToDns.zones.foldLeft("")((a,b)=> a+" "+b) )
	//   // }
	//   // else
	//   if (data.get("data") != None) {
	//     val domainCandidate = try {
	//       val domain = JsonIO.Json.readValue(data("data"), classOf[ExtendedDomain])
	//       domain.settings.foldRight(domain) {
	//         case (soa, domain) =>
	//           val newSoa = soa.updateSerial(
	//             if (soa.serial == null || soa.serial == "") SerialParser.generateNewSerial.toString
	//             else SerialParser.updateSerial(soa.serial).toString)
	//           domain.removeHost(soa).addHost(newSoa)
	//       }
	//     } catch {
	//       case ex: Exception => null
	//     }
	//     val replaceFilename = data.get("replace_filename").getOrElse(null)
	//     val (validcode, messages) = DomainValidationService.validate(domainCandidate, replaceFilename)
	//     if (validcode < 2) {
	//       if (replaceFilename != null) {
	//         //DNSCache.removeDomain(replaceFilename.split("""\.""").toList)
	//         DNSAuthoritativeSection.removeDomain(replaceFilename.split("""\.""").toList)
	//         //JsonIO.removeAuthData(replaceFilename)
	//       }
	//       val domains = DomainValidationService.reorganize(domainCandidate)
	//       //DNSCache.setDomain(domains.head)
	//       DNSAuthoritativeSection.setDomain(domains.head)
	//       //JsonIO.storeAuthData(domains.head)
	//       val response = "{\"code\":" + validcode + ",\"messages\":" + messages.mkString("[\"", "\",\"", "\"]") + ",\"data\":" +
	//        JsonIO.Json.writeValueAsString(domains) + "}"
	//       NotifyUtil.notify(domainCandidate)

	//       //response

	//     } else {
	//       "{\"code\":" + validcode + ",\"messages\":" + messages.mkString("[\"", "\",\"", "\"]") + "}"
	//     }
	//   } else {
	//     throw new Exception("Unidentified error")
	//     // "{\"code\":2,\"message\":\"Error: Unknown request\"}"
	//   }
	// }
}