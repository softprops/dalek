abstract class Adb(host: String, port: Int) {
  import java.io.IOException
  import java.net.{InetAddress,InetSocketAddress}
  import java.nio.{ByteBuffer,ByteOrder}
  import java.nio.channels.SocketChannel

  lazy val chan = {
     val c = SocketChannel.open(
       new InetSocketAddress(InetAddress.getByName(host), port)
     )
     c.configureBlocking(false)
     c
   }

  send("host:transport-usb") { ok =>
    if(!ok) error("no usb connection detected")
  }

  def send[T](cmd: String)(f: Boolean => T) = {
    println("sending: %s" format cmd)
    val req = "%04X%s" format(cmd.size, cmd)
    write(ByteBuffer.wrap(req.getBytes))
    f(ok)
  }

  def ok = {
    val b = ByteBuffer.wrap(Array.ofDim[Byte](4))
    read(b)
    b.array() match {
      case Array('O', 'K', 'A', 'Y') => true
      case e => false
    }
  }

  protected def write(b: ByteBuffer) =
    while (b.position() != b.limit()) {
      chan.write(b) match {
        case 0 => Thread.sleep(4)
        case n if(n < 0) => error("eof")
        case ok => ()
      }
    }

  protected def read(b: ByteBuffer) =
    while (b.position() != b.limit()) {
      chan.read(b) match {
        case 0 => Thread.sleep(4)
        case n if(n < 0) => error("eof")
        case ok => ()
      }
    }
}
