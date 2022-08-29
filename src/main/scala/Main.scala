import lms.collection.immutable.{ListOps, TupleOps}
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}
import lms.thirdparty.{CCodeGenLibFunction, LibFunction, ScannerOps}

trait FileOps extends ScannerOps with LibFunction {
  def read(fd: Rep[Int], buf: Rep[Array[Char]], size: Rep[Long]): Rep[Int] =
    libFunction[Int]("read", Unwrap(fd), Unwrap(buf), Unwrap(size))(Seq(0, 1, 2), Seq(1), Set())
}

object Main {
  def main(args: Array[String]): Unit = {
    val BlockPaths = List("/Foo.txt", "/Bar.txt")
    //val snippet = new DslDriverC[Int, Unit] with FileOps with CCodeGenLibFunction {
    val snippet = new DslDriverC[Int, Unit] with FileOps {
      q => // FIXME: Why is this required?
      override val codegen = new DslGenC with CCodeGenLibFunction {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        var count = 0L
        for (i <- 0 until BlockPaths.length: Range) {
          val block_num: Rep[Int] = open(BlockPaths(i))
          val size: Rep[Long] = filelen(block_num)
          val buf: Rep[Array[Char]] = NewArray[Char](1000000000)
          val toread = read(block_num, buf, size)
          for (j <- 0 until toread) {
            if (buf(j) != ' ') count = count + 1
          }
        }
        println(count)
      }
    }
    println(snippet.code)
  }
}
