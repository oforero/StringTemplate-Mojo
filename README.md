# Scalajutsu's String Template Mojo

This is a Maven mojo that executes in the generate and/or generate-sources phase,
it process StringTemplate files using parameters passed by hand
or from a Database query.

It was created to cover the need of generating Scala code but it should be possible to use it
for other purposes.

## Building and Installing
For the moment you need to clone this project and build it locally.

    mvn clean install


## Simple configuration

A minimal configuration can pass the parameters for the StringTemplate directly, each parameter tag results in a
template application.

    <plugin>
      <groupId>scalajutsu.mojos</groupId>
      <artifactId>stringtemplate</artifactId>
      <version>0.5-SNAPSHOT</version>
      <executions>
        <execution>
          <goals>
            <goal>generate</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <groupName>currency</groupName>
        <headers>
          <header>CurrencyCompanion_header</header>
          <header>CurrencyBasedFactory_header</header>
          <header>info_header</header>
          <header>currency_header</header>
        </headers>
        <templates>
          <template>CurrencyCompanion</template>
          <template>CurrencyBasedFactory</template>
          <template>info</template>
          <template>currency</template>
        </templates>
        <footers>
          <footer>CurrencyCompanion_footer</footer>
          <footer>CurrencyBasedFactory_footer</footer>
        </footers>
        <parameters>
          <parameter>Name=Euro,Iso=EUR,Symbol=€</parameter>
          <parameter>Name=Dollar,Iso=USD,Symbol=$</parameter>
        </parameters>
      </configuration>
    </plugin>

## Database based configuration

It s possible to execute the templates against the results of a JDBC query

      <plugin>
        <groupId>scalajutsu.mojos</groupId>
        <artifactId>stringtemplate</artifactId>
        <version>0.5-SNAPSHOT</version>
        <dependencies>
          <dependency>
            <groupId>${st.driver.groupId}</groupId>
            <artifactId>${st.driver.artifactId}</artifactId>
            <version>${st.driver.version}</version>
          </dependency>
        </dependencies>
        <executions>
          <execution>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <groupName>currency</groupName>
          <headers>
            <header>CurrencyCompanion_header</header>
            <header>CurrencyBasedFactory_header</header>
            <header>info_header</header>
            <header>currency_header</header>
          </headers>
          <templates>
            <template>CurrencyCompanion</template>
            <template>CurrencyBasedFactory</template>
            <template>info</template>
            <template>currency</template>
          </templates>
          <footers>
            <footer>CurrencyCompanion_footer</footer>
            <footer>CurrencyBasedFactory_footer</footer>
          </footers>
          <driver>${st.driver}</driver>
          <connString>${st.connString}</connString>
          <user>${st.user}</user>
          <password>${st.password}</password>
          <query>${st.query}</query>
        </configuration>
      </plugin>

## Adding the generated files as sources

If having the generated files available for compilation is required then add the build helper plugin

      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <version>1.5</version>
        <executions>
          <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>add-source</goal>
            </goals>
            <configuration>
              <sources>
                <source>${project.build.directory}/generated-sources/main/scala/</source>
              </sources>
            </configuration>
          </execution>
        </executions>
      </plugin>

And possible add the goal add-source to your compiler plugin

       <plugin>
         <groupId>org.scala-tools</groupId>
         <artifactId>maven-scala-plugin</artifactId>
         <executions>
           <execution>
             <goals>
               <goal>add-source</goal>
               <goal>compile</goal>
               <goal>testCompile</goal>
             </goals>
           </execution>
         </executions>
         <configuration>
           <scalaVersion>${scala.version}</scalaVersion>
           <args>
             <arg>-target:jvm-1.5</arg>
             <arg>-unchecked</arg>
           </args>
         </configuration>
       </plugin>

## Sample Templates

You can use any valid StringTemplate with this mojo, for example from the following templates:

### Header template
    package uk.ac.liv.oforero.currency

    import uk.ac.liv.oforero.coretypes.{GenericValueCompanion}

### Body template

    /**
     * Generated code for Currency: $Name$
     */

    class $Iso$(value: BigDecimal) extends Currency[$Iso$](value, $Name$Info)

    object $Iso$ extends CurrencyCompanion[$Iso$]{
      protected def newBuilder(myVal: BigDecimal) = new $Iso$(myVal)

      class PimpWith$Name$(value: BigDecimal) {
        def $Iso$ = new $Iso$(value)
      }

      implicit def numeric2$Iso$[V <% BigDecimal](value: V) = new PimpWith$Name$(value)
    }

The following code is generated when passing the appropriate parameters:

    package uk.ac.liv.oforero.currency

    import uk.ac.liv.oforero.coretypes.{GenericValueCompanion}
    /**
     * Generated code for Currency: Pound
     */

    class GBP(value: BigDecimal) extends Currency[GBP](value, PoundInfo)

    object GBP extends CurrencyCompanion[GBP]{
      protected def newBuilder(myVal: BigDecimal) = new GBP(myVal)

      class PimpWithPound(value: BigDecimal) {
        def GBP = new GBP(value)
      }

      implicit def numeric2GBP[V <% BigDecimal](value: V) = new PimpWithPound(value)
    }
    /**
     * Generated code for Currency: Dollar
     */

    class USD(value: BigDecimal) extends Currency[USD](value, DollarInfo)

    object USD extends CurrencyCompanion[USD]{
      protected def newBuilder(myVal: BigDecimal) = new USD(myVal)

      class PimpWithDollar(value: BigDecimal) {
        def USD = new USD(value)
      }

      implicit def numeric2USD[V <% BigDecimal](value: V) = new PimpWithDollar(value)
    }
    /**
     * Generated code for Currency: Euro
     */

    class EUR(value: BigDecimal) extends Currency[EUR](value, EuroInfo)

    object EUR extends CurrencyCompanion[EUR]{
      protected def newBuilder(myVal: BigDecimal) = new EUR(myVal)

      class PimpWithEuro(value: BigDecimal) {
        def EUR = new EUR(value)
      }

      implicit def numeric2EUR[V <% BigDecimal](value: V) = new PimpWithEuro(value)
    }
