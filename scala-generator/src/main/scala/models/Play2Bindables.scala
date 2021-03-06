package scala.models

import lib.Text
import scala.generator.{ScalaEnum, ScalaService}

case class Play2Bindables(ssd: ScalaService) {

  def build(): String = {
    import lib.Text._

    Seq(
      "object Bindables {",
      "",
      "  import play.api.mvc.{PathBindable, QueryStringBindable}",
      "  import org.joda.time.{DateTime, LocalDate}",
      "  import org.joda.time.format.ISODateTimeFormat",
      s"  import ${ssd.namespaces.models}._",
      "",
      buildDefaults().indent(2),
      "",
      ssd.enums.map { e => buildImplicit(e.name) }.mkString("\n\n").indent(2),
      "",
      "}"
    ).mkString("\n")
  }

  private def buildDefaults(): String = {
    """
// Type: date-time-iso8601
implicit val pathBindableTypeDateTimeIso8601 = new PathBindable.Parsing[org.joda.time.DateTime](
  ISODateTimeFormat.dateTimeParser.parseDateTime(_), _.toString, (key: String, e: Exception) => s"Error parsing date time $key. Example: 2014-04-29T11:56:52Z"
)

implicit val queryStringBindableTypeDateTimeIso8601 = new QueryStringBindable.Parsing[org.joda.time.DateTime](
  ISODateTimeFormat.dateTimeParser.parseDateTime(_), _.toString, (key: String, e: Exception) => s"Error parsing date time $key. Example: 2014-04-29T11:56:52Z"
)

// Type: date-iso8601
implicit val pathBindableTypeDateIso8601 = new PathBindable.Parsing[org.joda.time.LocalDate](
  ISODateTimeFormat.yearMonthDay.parseLocalDate(_), _.toString, (key: String, e: Exception) => s"Error parsing date $key. Example: 2014-04-29"
)

implicit val queryStringBindableTypeDateIso8601 = new QueryStringBindable.Parsing[org.joda.time.LocalDate](
  ISODateTimeFormat.yearMonthDay.parseLocalDate(_), _.toString, (key: String, e: Exception) => s"Error parsing date $key. Example: 2014-04-29"
)
""".trim
  }

  private[models] def buildImplicit(
    enumName: String
  ): String = {
    val fullyQualifiedName = ssd.enumClassName(enumName)
    s"// Enum: $enumName\n" +
    """private[this] val enum%sNotFound = (key: String, e: Exception) => s"Unrecognized $key, should be one of ${%s.all.mkString(", ")}"""".format(enumName, fullyQualifiedName) +
    s"""

implicit val pathBindableEnum$enumName = new PathBindable.Parsing[$fullyQualifiedName] (
  $enumName.fromString(_).get, _.toString, enum${enumName}NotFound
)

implicit val queryStringBindableEnum$enumName = new QueryStringBindable.Parsing[$fullyQualifiedName](
  $enumName.fromString(_).get, _.toString, enum${enumName}NotFound
)"""
  }

}
