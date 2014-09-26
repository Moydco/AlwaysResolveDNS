package enums

object RecordType extends Enumeration {

  val A = Value(1)
  val NS = Value(2)
  val CNAME = Value(5)
  val SOA = Value(6)
  val PTR = Value(12)
  val MX = Value(15)
  val TXT = Value(16)
  val AAAA = Value(28)
  val SRV = Value(33)
  val OPT = Value(41)
  val RRSIG = Value(46)
  val DNSKEY = Value(48)

  val AXFR = Value(252)
  val ALL = Value(255)

}
