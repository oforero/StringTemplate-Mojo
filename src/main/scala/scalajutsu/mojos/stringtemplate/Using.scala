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
import java.sql.{Connection, DriverManager}
import java.io._
import File._

/**
 * Utility object providing resource management control structures
 *
 * @author Oscar Forero
 * @version 1.0
 *
 */
object Using {

  /**
   * Opens a Final in the requested repository with the provided encoding
   * then executes the function passing a Writer to the file as parameter
   * it handles exceptions and close the file automatically
   *
   * @param   name:     a String representing the JDBC driver to use
   * @param   outDir:   a valid connection string for the driver
   * @param   encoding: the user name used to connect to the DB
   *
   * @param   lambda:   a function taking a Writer as a parameter.
   *
   * @return  Unit
   */
  def using(name: String, outDir: File, encoding: String = "UTF8")
           (lambda: (Writer) => Unit) {
    if ( !outDir.exists() ) {
        outDir.mkdirs();
    }

    val outFile = outDir.getAbsolutePath + separator + name
    var out: Writer = null
    try {
      out = new OutputStreamWriter(new FileOutputStream(outFile), encoding)
      lambda(out)
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

  /**
   * Opens a connection to the DB described by the parameters
   * then executes the passed function with the connection as a paramter
   * it handles exceptions and close the connection automatically
   *
   * @param   driver:     a String representing the JDBC driver to use
   * @param   connString: a valid connection string for the driver
   * @param   user:       the user name used to connect to the DB
   * @param   password:   the password used to connect to the DB
   *
   * @param   lambda:     a Function taking a JDBC connection as a parametter
   *
   * @return  Unit
   */
  def using(driver: String, user: String, password: String, connString: String)
           (lambda: (Connection) => Unit) {
    try {
      Class.forName(driver).newInstance;
      val connection = DriverManager getConnection (connString, user, password)

      try {
        lambda(connection)
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
