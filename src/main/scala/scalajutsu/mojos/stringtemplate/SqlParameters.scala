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

import java.sql.{ResultSet}
import org.apache.maven.plugin.logging.Log;


/**
 * Object (Function) used to extract the parameters from a database query
 *
 * @author Oscar Forero
 * @version 1.0
 * 
 */

object SqlParameters {

  /**
   *  Get the column names of a ResultSet
   *
   * @param   rs: a @see java.sql.ResultSet
   * @param   log: a Maven logger that can be passed implicitly. @see org.apache.maven.plugin.logging.Log
   *
   * @return  a List[(String)] with the column names
   */
  private def getFieldList(rs: ResultSet)(implicit log: Log) = {
    import log._

    // Get result set meta data
    val rsmd = rs.getMetaData();
    val numColumns = rsmd.getColumnCount();
    val fields = for(i <- 1 until numColumns + 1) yield rsmd.getColumnName(i)
    info("Parameters extracted from the database: " + fields)
    fields.toList
  }

  /**
   *  Returns a list of maps ColumnName -> Value
   *
   * @param   driver:     a String representing the JDBC driver to use
   * @param   connString: a valid connection string for the driver
   * @param   user:       the user name used to connect to the DB
   * @param   password:   the password used to connect to the DB
   * @param   query:      a String containing an SQL query
   *
   * @return  a List[Map[String, String]] with an item per row of the query result,
   *          each item is a map from ColumnName -> Value 
   */
  def apply(driver: String, connString: String, user:String, password: String, query: String)
           (implicit log: Log): Seq[Map[String,String]]= {
    import Using._
    var params: List[Map[String,String]] = Nil

    using(driver, user, password, connString) {
      conn =>
        val statement = conn createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        val rs = statement executeQuery query

        val fields = getFieldList(rs)
        while(rs.next) {
          val row = for(field <- fields) yield (field, rs getString field)

          params = Map(row.toSeq:_*) :: params
        }
    }

    params.toSeq
  }
}