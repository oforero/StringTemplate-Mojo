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

import org.apache.maven.plugin.MojoExecutionException
import java.io._
import File._

/**
 * TODO: Write description here!!
 *
 * @author Oscar Forero
 * @version 1.0
 *
 * Date: Apr 11, 2010
 * Time: 7:38:29 PM
 *
 */

object Using {
  def using(name: String, outDir: File, encoding: String = "UTF8")(exec: (Writer) => Unit) {
    if ( !outDir.exists() ) {
        outDir.mkdirs();
    }

    val outFile = outDir.getAbsolutePath + separator + name
    var out: Writer = null
    try {
      out = new OutputStreamWriter(new FileOutputStream(outFile), encoding)
      exec(out)
    } catch {
      case e: IOException  =>
        throw new MojoExecutionException("Error creating file " + outFile, e)
    } finally {
      if ( out != null ) {
        try
        {
          out.close();
        } catch {
          case e: IOException =>
            throw new MojoExecutionException("Error closing file " + outFile, e)
        }
      }
    }
  }

  import java.sql.{Connection, DriverManager}
  def using(driver: String, user: String, password: String, connString: String)(exec: (Connection) => Unit){
    try {
      Class.forName(driver).newInstance;
      val connection = DriverManager getConnection (connString, user, password)

      try {
        exec(connection)
      } catch {
        case e =>
          throw new MojoExecutionException("Error querying the provided database", e)
      } finally {
        connection close
      }
    } catch {
      case e =>
        throw new MojoExecutionException("Error stablishing connection: " + connString, e)
    }
  }
}
