/*
 *      _____            __          _        __
 *     / ___/_________ _/ /____ _   (_)__  __/ /________  __
 *     \__ \/ ___/ __ `/ // __ `/  / // / / / __/ ___/ / / /
 *    ___/ / /__/ /_/ / // /_/ /  / // /_/ / /_(__  ) /_/ /
 *   /____/\___/\__,_/_/ \__,_/__/ / \__,_/\__/____/\__,_/
 *                            /___/
 *
 *   Copyright (c) 2010, Oscar Forero & Scalajutsu Contributors
 *   All rights reserved.
 *
 */

package scalajutsu.mojos.stringtemplate

import org.apache.maven.plugin.{MojoExecutionException, AbstractMojo}
import org.clapper.scalasti.{StringTemplate, StringTemplateGroup}
import java.io.{Writer, File}

import org.scala_tools.maven.mojo.annotations._

/**
 * Maven compliant Mojo Implementation
 *
 * @author Oscar Forero
 * @version 1.0
 *
 */

/**
 * The Mojo execute on Maven's goals: __generate__ and __generate-sources__
 */
@goal("generate")
@phase("generate-sources")
class StringTemplateMojo extends AbstractMojo {
  @parameter
  @description("The place where the generated code will be written")
  @expression("${project.build.directory}/generated-sources/main/scala")
  @readOnly
  protected var outputDirectory: File = _

  @parameter
  @description("The place where the template files live")
  @expression("${basedir}/src/main/resources")
  @readOnly
  protected var templateDirectory: File = _

  @parameter
  @description("The name of the Template group")
  @required
  protected var groupName: String = _

  @parameter
  @description("A list of headers to apply before each template")
  protected var headers: Array[String] = Array()

  @parameter
  @description("A list of templates to repeatedly applied")
  @required
  protected var templates: Array[String] = Array()

  @parameter
  @description("A list of templates to apply after each template")
  protected var footers: Array[String] = Array()

  @parameter
  @description("A commaseparated list of pairs with the parameters")
  protected var parameters: Array[String] = Array()

  @parameter
  @description("A select statement to obtain the parameters")
  protected var query: String = ""

  @parameter
  @description("The database driver class")
  protected var driver: String = ""

  @parameter
  @description("The connection string to use to connect to the DB")
  protected var connString: String = ""

  @parameter
  @description("DB User name")
  protected var user: String = "<no user>"

  @parameter
  @description("DB User password")
  protected var password: String = "<no password>"

  @parameter
  @description("Fail hard in case of errors")
  protected var failHard: Boolean = true
  
  private implicit val log = getLog()
  import log._
  
  private def parse(parameter: String) = {
    val Parameter = """([^=,]+)={1}([^=,]+)""".r
    val pairs = for (Parameter(name, value) <- Parameter findAllIn parameter) yield (name, value)
    info("Parameters extracted from the configuration: " + pairs)
    Map(pairs.toSeq:_*)
  }

  private lazy val params: Seq[Map[String, String]] = {
    if (parameters.isEmpty) {
      assert(!(driver.isEmpty || connString.isEmpty || query.isEmpty),
        "driver, connString and query are required if explicit parameters are not given")

      SqlParameters(driver, connString, user, password, query)
    } else {
      assert(driver.isEmpty && connString.isEmpty && query.isEmpty,
        "driver, connString and query are required not to be present if explicit parameters are given")

      parameters.map(parse(_))
    }
  }

  private def applyTemplate(t: StringTemplate) {
    import Using._

    using(t.name.get + ".scala", outputDirectory) {
      out ⇒
          write(out, t, params)
    }
  }

  private def applyTemplateWithHeader(h: StringTemplate, t: StringTemplate) {
    import Using._

    using(t.name.get + ".scala", outputDirectory) {
      out ⇒
        write(out, h)
        write(out, t, params)
    }
  }

  private def applyTemplateWithHeaderAndFooter(h: StringTemplate, t: StringTemplate, f: StringTemplate) {
    import Using._

    using(t.name.get + ".scala", outputDirectory) {
      out ⇒
        write(out, h)
        write(out, t, params)
        write(out, f)
    }
  }

  private def applyTemplateWithFooter(t: StringTemplate, f: StringTemplate) {
    import Using._

    using(t.name.get + ".scala", outputDirectory) {
      out ⇒
        write(out, t, params)
        write(out, f)
    }
  }

  private def write(out: Writer, template: StringTemplate) {
    out.write(template.toString)
    out.write("\n")
  }

  private def write(out: Writer, template: StringTemplate, params: Seq[Map[String,String]]) {
    for (paramSet <- params) {
      template.reset
      info("Setting parameters: " + paramSet)
      for((name, value) <- paramSet) template.setAttribute(name, value)
      out.write(template.toString)
      out.write("\n")
    }
  }

  /**
   * This is meant to be called by Maven
   */
  @throws(classOf[MojoExecutionException])
  def execute() {
    require(templates.length > 0, "At least one template is required")
    require(templates.length >= headers.length, "The numbers of templates must be bigger or equal to the number of headers")
    require(templates.length >= footers.length, "The numbers of templates must be bigger or equal to the number of footers")

    val grp = new StringTemplateGroup("StringTemplateMojo", templateDirectory)
    def st(name: String) = name match {
      case "" => None
      case _ => Some(grp.template(name))
    }

    headers = headers padTo (templates.length, "")
    footers = footers padTo (templates.length, "")

    val templateSets = (headers.map(st) , templates.map(st) , footers.map(st)).zipped.map(Tuple3.apply)

    for(templateSet <- templateSets) {
      templateSet match {
        case (None, Some(t), None) => applyTemplate(t)
        case (Some(h), Some(t), None) => applyTemplateWithHeader(h, t)
        case (Some(h), Some(t), Some(f)) => applyTemplateWithHeaderAndFooter(h, t, f)
        case (None, Some(t), Some(f)) => applyTemplateWithFooter(t, f)
        case _ => throw new IllegalArgumentException("Invalid convination of templates")
      }
    }
  }

}