package utils
 
import java.util.Calendar
import scala.collection.mutable.StringBuilder
/*
    Serial Format: YYYYMMDDII where II is an integer between 0 and 9
 */


object SerialParser {


  def serialHasChanged(originalSerial: String, newSerial: String):Boolean = {
    val oSerial = originalSerial.toLong
    val nSerial = newSerial.toLong
    nSerial > oSerial
  }

  def updateSerial(originalSerial: String): Long = {
    originalSerial.toLong + 1
  }

  def generateNewSerial():Long = {

    val year = Calendar.getInstance().get(Calendar.YEAR)
    val month = Calendar.getInstance().get(Calendar.MONTH)
    val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val serial = new StringBuilder().append(year.toString).append(month.toString).append(day.toString).append("00").toString
    serial.toLong
  }


}
