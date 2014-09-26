package records

import io.netty.buffer.ByteBuf
import payload.Name
import org.slf4j.LoggerFactory
import payload.RRData

case class RRSIG(
                  timetolive: Long,
                  typeCovered: Short,
                  algorithm: Byte,
                  labels: Byte,
                  originalTTL: Long,
                  signatureExpiration: Long,
                  signatureInception: Long,
                  keyTag: Short,
                  signerName: List[Array[Byte]],
                  signature: List[Array[Byte]]
                   ) extends AbstractRecord {

  val description = "RRSIG"

  def isEqualTo(any: Any) = any match {
    case r: RRSIG => r.timetolive == timetolive && typeCovered == r.typeCovered &&
      algorithm == r.algorithm && labels == r.labels && originalTTL == r.originalTTL && signatureExpiration == r.signatureExpiration && signatureInception == r.signatureInception &&
      keyTag == r.keyTag && signerName == r.signerName && signature == r.signature
    case _ => false
  }

  def toByteArray = RRData.shortToBytes(typeCovered) ++ Array[Byte](algorithm) ++ Array[Byte](labels) ++ RRData.intToBytes(originalTTL.toInt) ++
    RRData.intToBytes(signatureExpiration.toInt) ++ RRData.intToBytes(signatureInception.toInt) ++ RRData.shortToBytes(keyTag) ++ Name.toByteArray(signerName) ++ signature.head

  /**
   * Non comprime niente in realtà, però bisogna che ci sia.
   * @param input
   * @return
   */
  def toCompressedByteArray(input: (Array[Byte], Map[String, Int])) = {
    (input._1 ++ toByteArray, input._2)
  }
}

object RRSIG {

  val logger = LoggerFactory.getLogger("app")

  def apply(buf: ByteBuf, recordclass: Int, size: Int, offset: Int = 0) = {
    logger.error("Should not be called")

    val typeCovered = buf.readUnsignedShort.toShort
    val algorithm = buf.readByte()
    val labels = buf.readByte()
    val originalTTL = buf.readUnsignedInt()
    val signatureExpiration = buf.readUnsignedInt()
    val signatureInception = buf.readUnsignedInt()
    val keyTag = buf.readUnsignedShort.toShort
    val signerName = List(Array[Byte]())
    val signature = List(Array[Byte]())

    new RRSIG(60, typeCovered, algorithm, labels, originalTTL, signatureExpiration, signatureInception, keyTag, signerName, signature)
  }
}