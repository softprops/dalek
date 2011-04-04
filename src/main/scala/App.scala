object App {
  import unfiltered.netty._
  import websockets._
  import unfiltered.request._
  import unfiltered.response._
  import org.jboss.netty.handler.stream.{ChunkedStream, ChunkedWriteHandler}

  import java.util.concurrent.{Executors, TimeUnit}
  import java.util.Collections
  import java.io.ByteArrayInputStream

  def main(args: Array[String]) {
    val deviceFrames = Frames("127.0.0.1", 5037)
    val frames = Collections.synchronizedMap(
      new java.util.HashMap[String, Array[Byte]]()
    )
    val sched = Executors.newScheduledThreadPool(1)
    unfiltered.netty.Http(8080)
       .handler(websockets.Planify({
          case _ => {
            case Open(s) =>
              deviceFrames.takeWhile(s.channel.isConnected) { frame =>
                val path = java.util.UUID.randomUUID.toString
                frames.put(path, frame.data)
                s.send(path)
                sched.schedule(new Runnable {
                  def run = {
                    //println("removing path %s" format path)
                    //frames.remove(path)
                  }
                }, 6, TimeUnit.SECONDS)
              }
          }
         }).onPass(_.sendUpstream(_))
       )
       .handler(new ChunkedWriteHandler())
       .handler(unfiltered.netty.channel.Planify({
          case r @ Path(path) => frames.get(path.drop(1)) match {
            case null =>
              println("no entry for path %s" format path.drop(1))
              NotFound
            case f: Array[Byte] =>
              println("attempting to write data for path %s : data size %s" format(path.drop(1), f.size))
              // todo: make sure content type is correct and set
              r.underlying.
                context.getChannel.write(
                  new ChunkedStream(new ByteArrayInputStream(f))
                )
          }
       })).run
  }
}
