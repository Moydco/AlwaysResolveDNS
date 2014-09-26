package models

import enums.RecordType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory

@JsonIgnoreProperties(Array("typ"))
case class ExtendedDomain(
                           @JsonProperty("origin") fullName: String,
                           @JsonProperty("ttl") ttl: Long,
                           @JsonProperty("NS") nameservers: Array[NSHost] = Array(),
                           @JsonProperty("SOA") settings: Array[SoaHost] = null,
                           @JsonProperty("CNAME") cname: Array[CnameHost] = null,
                           @JsonProperty("A") address: Array[AddressHost] = null,
                           @JsonProperty("AAAA") ipv6address: Array[IPv6AddressHost] = null,
                           @JsonProperty("PTR") pointer: Array[PointerHost] = null,
                           @JsonProperty("TXT") text: Array[TxtHost] = null,
                           @JsonProperty("SRV") service: Array[SrvHost] = null,
                           @JsonProperty("MX") mailx: Array[MXHost] = null,
                           @JsonProperty("OH") otherhosts: Array[GenericHost] = null,
                           @JsonProperty("DNSKEY") dnsKey: Array[DNSKEYHost] = null,
                           @JsonProperty("RRSIG") rrsig: Array[RRSIGHost] = null
                           ) extends AbstractDomain {
  val logger = LoggerFactory.getLogger("app")

  @JsonIgnore
  lazy val hosts: List[Host] =
    hostsToList(nameservers) ++ hostsToList(cname) ++ hostsToList(address) ++ hostsToList(ipv6address) ++
      hostsToList(settings) ++ hostsToList(pointer) ++ hostsToList(text) ++ hostsToList(service) ++ hostsToList(mailx) ++
      hostsToList(otherhosts) ++ hostsToList(dnsKey) ++ hostsToList(rrsig)

  @JsonIgnore
  def hasRootEntry(typ: Int = 0) =
    if (typ == RecordType.ALL.id) {
      logger.debug("ALL query search");
      !findHosts(fullName).isEmpty || !findHosts("@").isEmpty
    }
    else if (typ == RecordType.AXFR.id) !findHosts(fullName).isEmpty || !findHosts("@").isEmpty
    else findHost(fullName, typ) != None || findHost("@", typ) != None || findHost(fullName, 5) != None || findHost("@", 5) != None

  @JsonIgnore
  def findHost(name: String = null, typ: Int = 0) = {
    logger.debug("Searching for: " + RecordType(typ).toString + " name: " + name)

    RecordType(typ).toString match {
      case "A" => findInArrayWithNull(address, compareHostName(name))
      case "AAAA" => findInArrayWithNull(ipv6address, compareHostName(name))
      case "CNAME" => findInArrayWithNull(cname, compareHostName(name))
      case "NS" => findInArrayWithNull(nameservers, compareHostName(name))
      case "SOA" => findInArrayWithNull(settings, compareHostName(name))
      case "PTR" => findInArrayWithNull(pointer, compareHostName(name))
      case "TXT" => findInArrayWithNull(text, compareHostName(name))
      case "SRV" => findInArrayWithNull(service, compareHostName(name))
      case "MX" => if (mailx != null) {
        val mx = mailx.filter(compareHostName(name)(_))
        if (mx.isEmpty) None else Some(mx.minBy(_.priority))
      } else None
      case "ALL" => {
        logger.debug("ALL query");
        findInArrayWithNull(hosts.toArray, compareHostName(name));
      }
      case "DNSKEY" => findInArrayWithNull(dnsKey, compareHostName(name))
      case "RRSIG" => findInArrayWithNull(rrsig, compareHostName(name))
      case _ => None
    }
  }

  @JsonIgnore
  def getHost(name: String = null, typ: Int = 0) =
    findHost(name, typ) match {
      case Some(host) => host
      case None => throw new HostNotFoundException
    }

  @JsonIgnore
  def findHosts(name: String) =
    hosts.filter(compareHostName(name)(_))

  @JsonIgnore
  def getHosts(name: String) =
    findHosts(name)

  @JsonIgnore
  private def compareHostName(name: String)(host: Host) = {
    logger.debug("Comparing name: " + name + " with stored: " + host.name + " of type: " + host.typ)
    host.name == name || (name == fullName && (host.name == fullName || host.name == "@"))
  }

  @JsonIgnore
  private def hostsToList[T <: Host](hosts: Array[T]): List[Host] =
    if (hosts != null) hosts.toList else Nil

  @JsonIgnore
  private def findInArrayWithNull[T <: Host](array: Array[T], condition: T => Boolean) = {
    if (array != null) {
      logger.debug("Searching for records of this type")
      array.find(condition)
    }
    else {
      logger.debug("No record to search in the zone of this type")
      None
    }
  }

  def addHost(host: Host) = {
    def add[T <: Host](array: Array[T], host: T) = if (array == null) List(host) else host :: array.toList
    host match {
      case h: NSHost =>
        new ExtendedDomain(fullName, ttl, add(nameservers, h).toArray, settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: SoaHost =>
        new ExtendedDomain(fullName, ttl, nameservers, add(settings, h).toArray, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: CnameHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, add(cname, h).toArray, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: AddressHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, add(address, h).toArray, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: IPv6AddressHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, add(ipv6address, h).toArray, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: PointerHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, add(pointer, h).toArray, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: TxtHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, add(text, h).toArray, service, mailx, otherhosts, dnsKey, rrsig)
      case h: SrvHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, add(service, h).toArray, mailx, otherhosts, dnsKey, rrsig)
      case h: MXHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, add(mailx, h).toArray, otherhosts, dnsKey, rrsig)
      case h: GenericHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, add(otherhosts, h).toArray, dnsKey, rrsig)
      case h: DNSKEYHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, add(dnsKey, h).toArray, rrsig)
      case h: RRSIGHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, add(rrsig, h).toArray)
    }
  }

  def removeHost(host: Host) = {
    def remove[T <: Host](array: Array[T], host: T) = if (array == null) array else array.filterNot(_.equals(host))
    host match {
      case h: NSHost =>
        new ExtendedDomain(fullName, ttl, remove(nameservers, h), settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: SoaHost =>
        new ExtendedDomain(fullName, ttl, nameservers, remove(settings, h), cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: CnameHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, remove(cname, h), address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: AddressHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, remove(address, h), ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: IPv6AddressHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, remove(ipv6address, h), pointer, text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: PointerHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, remove(pointer, h), text, service, mailx, otherhosts, dnsKey, rrsig)
      case h: TxtHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, remove(text, h), service, mailx, otherhosts, dnsKey, rrsig)
      case h: SrvHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, remove(service, h), mailx, otherhosts, dnsKey, rrsig)
      case h: MXHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, remove(mailx, h), otherhosts, dnsKey, rrsig)
      case h: GenericHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, remove(otherhosts, h), dnsKey, rrsig)
      case h: DNSKEYHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, remove(dnsKey, h), rrsig)
      case h: RRSIGHost =>
        new ExtendedDomain(fullName, ttl, nameservers, settings, cname, address, ipv6address, pointer, text, service, mailx, otherhosts, dnsKey, remove(rrsig, h))
    }
  }

  def getFilename = if (fullName.startsWith("*")) "-wildcard" + fullName.substring(1) else fullName
}

class HostNotFoundException extends Exception