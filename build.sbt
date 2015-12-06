lazy val akkaVersion = "2.4.1"
lazy val akkaExperimentalVersion = "2.0-M2"

def runableProject(name: String, d: sbt.File) = Project(name, d).
  settings(
    Revolver.settings ++ 
    Seq(
      scalaVersion      := "2.11.7",
      scalacOptions    ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked"),
      fork in Test      := true,
      fork in run       := true,
      dockerRepository  := Some("jaohaohsuan"),
      libraryDependencies ++= Seq(
        "com.github.scopt" %% "scopt" % "3.3.0"
      )
    )
  ).enablePlugins(JavaAppPackaging)

val protocol = Project("protocol", file("protocol")).settings(
  Seq(
    version := "0.1",
    scalaVersion := "2.11.7"
  )
)

val root = runableProject("root", file(".")).dependsOn(protocol).settings(
  Seq(
    name                   := "backend",
    version                := "0.1",
    packageName in Docker  := "adw-master-worker", 
    libraryDependencies   ++= Seq(
      "com.typesafe.akka"         %% "akka-cluster"            % akkaVersion,
      "com.typesafe.akka"         %% "akka-cluster-tools"      % akkaVersion,
      "com.typesafe.akka"         %% "akka-persistence"        % akkaVersion,
      "com.typesafe.akka"         %% "akka-cluster-metrics"    % akkaVersion,
      "org.iq80.leveldb"          %  "leveldb"                 % "0.7",
      "com.github.romix.akka"     %% "akka-kryo-serialization" % "0.4.0",
      "org.fusesource.leveldbjni" %  "leveldbjni-all"          % "1.8",
      "com.github.scopt"          %% "scopt"                   % "3.3.0",
      "com.typesafe.akka"         %% "akka-testkit"            % akkaVersion  % "test",
      "org.scalatest"             %% "scalatest"               % "2.2.4"      % "test",
      "commons-io"                %  "commons-io"              % "2.4"        % "test")
  )
)

val frontend = runableProject("frontend", file("frontend")).dependsOn(root).settings(
    Seq(
      name := "frontend",
      version := "0.1",
      packageName in Docker := "adw-frontend",
      libraryDependencies ++= Seq(
        "com.typesafe.akka"     %% "akka-stream-experimental"    % akkaExperimentalVersion,
        "com.typesafe.akka"     %% "akka-http-core-experimental" % akkaExperimentalVersion,
        "com.typesafe.akka"     %% "akka-http-experimental"      % akkaExperimentalVersion,
        "com.typesafe.akka"     %% "akka-http-xml-experimental"  % akkaExperimentalVersion,
        "com.typesafe.akka"     %% "akka-cluster-metrics"        % akkaVersion,
        "com.github.romix.akka" %% "akka-kryo-serialization"     % "0.4.0",
        "com.typesafe.akka"     %% "akka-cluster"                % akkaVersion,
        "com.typesafe.akka"     %% "akka-cluster-tools"          % akkaVersion
        )
    )
  )
