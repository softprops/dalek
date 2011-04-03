import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val uf_version = "0.3.2"
  val ufws = "net.databinder" %% "unfiltered-websockets" % uf_version
  val ufn = "net.databinder" %% "unfiltered-netty" % uf_version
}
