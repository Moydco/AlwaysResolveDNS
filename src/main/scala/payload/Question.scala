package payload

import io.netty.buffer.ByteBuf
import org.slf4j.LoggerFactory

case class Question(qname: List[Array[Byte]], qtype: Int, qclass: Int) {
  def toByteArray = 
    Name.toByteArray(qname) ++ RRData.shortToBytes(qtype.toShort) ++ RRData.shortToBytes(qclass.toShort)
    
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    val qnameBytes = Name.toCompressedByteArray(qname, input)
    (qnameBytes._1 ++ RRData.shortToBytes(qtype.toShort) ++ RRData.shortToBytes(qclass.toShort), qnameBytes._2)
  }
    
}
  
  
  
  
  
  
  //  QNAME         a domain name represented as a sequence of labels, where
  //                each label consists of a length octet followed by that
  //                number of octets.  The domain name terminates with the
  //                zero length octet for the null label of the root.  Note
  //                that this field may be an odd number of octets; no
  //                padding is used.
  //var qname = ""
    
  // QTYPE        a two octet code which specifies the type of the query.
  //              The values for this field include all codes valid for a
  //              TYPE field, together with some more general codes which
  //              can match more than one type of RR.  
  //var qtype = ""
    
  // QCLASS       a two octet code that specifies the class of the query.
  //              For example, the QCLASS field is IN for the Internet.
  //var qclass = ""


object Question {
  val logger = LoggerFactory.getLogger("app")
  
  def apply(buf: ByteBuf, offset: Int) = 
    new Question(
      Name.parse(buf, offset), 
      buf.readUnsignedShort,
      buf.readUnsignedShort
    )
}