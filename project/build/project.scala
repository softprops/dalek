import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  import Process._

  val uf_version = "0.3.2"
  val ufws = "net.databinder" %% "unfiltered-websockets" % uf_version
  val ufn = "net.databinder" %% "unfiltered-netty" % uf_version
  val androidHome = System.getenv("ANDROID_HOME")
  if(androidHome == null) error("export ANDROID_HOME")
  override def unmanagedClasspath = super.unmanagedClasspath +++ androidSdk +++ androidDdmlib
  def androidSdk = Path.fromFile(
    "%s/tools/lib/sdklib.jar" format androidHome
  )
  def androidDdmlib = Path.fromFile(
    "%s/tools/lib/ddmlib.jar" format androidHome
  )
  println("umcp %s" format unmanagedClasspath)
}
