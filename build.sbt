name := "Access control model"

ThisBuild / organization := "ru.sibsutis"

ThisBuild / version := "0.1"

scalaVersion := "2.13.2"

resolvers ++= Seq(
  "Java.net Maven2 Repository" at "https://download.java.net/maven/2/",
  "JCenter" at "https://jcenter.bintray.com/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.3.0-SNAP2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.3.0-SNAP2" % Test

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
libraryDependencies ++= javaFXModules.map( m =>
  "org.openjfx" % s"javafx-$m" % "15-ea+5" classifier osName
)