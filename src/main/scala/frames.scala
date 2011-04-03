case class Frames(host: String, port: Int) extends Adb(host, port) {
  import java.nio.{ByteBuffer, ByteOrder}

  def takeWhile[T](cond: => Boolean)(f: RawImage => T) =
    while(cond) {
      send("framebuffer:") { ok =>
        if(ok) {
          // header
          val pb = ByteBuffer.wrap(Array.ofDim[Byte](4))
          read(pb)
          pb.rewind()
          pb.order(ByteOrder.LITTLE_ENDIAN)
          val version = pb.getInt
          val headerSize = RawImage.getHeaderSize(version)
          val hb = ByteBuffer.wrap(Array.ofDim[Byte](headerSize * 4))
          read(hb)
          hb.rewind()
          hb.order(ByteOrder.LITTLE_ENDIAN)

          val img = new RawImage()
          img.readHeader(version, hb)

          // data
          write(ByteBuffer.wrap(Array[Byte](0)))
          val data = Array.ofDim[Byte](img.size)
          val bb = ByteBuffer.wrap(data)
          read(bb)
          img.data = data
          img
          f(img)
        } else error("error sending framebuffer request")
      }
    }
}
