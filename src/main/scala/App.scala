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
                sched.schedule(new Runnable {
                  def run = {
                    val path = java.util.UUID.randomUUID.toString
                    frames.put(path, frame.data)
                    s.send(path)
                    sched.schedule(new Runnable {
                      def run = frames.remove(path)
                    }, 3, TimeUnit.SECONDS)
                  }
                }, 0, TimeUnit.MILLISECONDS)
              }
          }
         }).onPass(_.sendUpstream(_))
       )
       .handler(new ChunkedWriteHandler())
       .handler(unfiltered.netty.channel.Planify({
          case r @ Path(path) => frames.get(path) match {
            case null => NotFound
            case f: Array[Byte] =>
              // todo: make sure content type is correct and set
              r.underlying.
                context.getChannel.write(
                  new ChunkedStream(new ByteArrayInputStream(f))
                )
          }
       })).run
  }
}
