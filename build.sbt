name := "library-app-zio"
version := "1.0"

lazy val `library-app-zio` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "org.iq80.leveldb"       % "leveldb"             % "0.12",
  "org.typelevel"          %% "cats-effect"        % "2.1.3",
  "dev.zio"                %% "zio"                % "1.0.0-RC21",
  "dev.zio"                %% "zio-interop-cats"   % "2.1.3.0-RC16",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
