import lms.collection.immutable.{CppCodeGen_List, CppCodeGen_Tuple, ListOps, TupleOps}
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.virtualize
import lms.thirdparty.{CCodeGenLibFunction, LibFunction, ScannerOps}
import lms.collection.mutable.ArrayOps

trait FileOps extends ScannerOps with LibFunction {
  def read(fd: Rep[Int], buf: Rep[Array[Char]], size: Rep[Long]): Rep[Int] =
    libFunction[Int]("read", Unwrap(fd), Unwrap(buf), Unwrap(size))(Seq(0, 2), Seq(1), Set())
}

@virtualize
trait MapReduceOps extends FileOps with ListOps with TupleOps {

  abstract class MapReduceComputation[KeyType: Manifest, ValueType: Manifest] {
    def Mapper(buf: Rep[Array[Char]]): Rep[List[Tuple2[KeyType, ValueType]]]
  }

  def HDFSExec[KeyType: Manifest: Ordering, ValueType: Manifest, ReducerResult: Manifest](
               paths: List[String],
               mapReduce: MapReduceComputation[KeyType, ValueType]) = {
    val buf = NewArray[Char](100000)
    val map_result = NewArray[List[Tuple2[KeyType, ValueType]]](paths.length)
    for (i <- 0 until paths.length: Range) {
      val block_num = open(paths(i))
      val size = filelen(block_num)
      read(block_num, buf, size)
      /* val out = read(block_num, buf, size)
         println(out)
         generates the read call
       */
      map_result(i) = mapReduce.Mapper(buf)
    }
    map_result(0)(3)
  }
}

trait MyFoo extends MapReduceOps with ListOps with TupleOps with ArrayOps {
  @virtualize
  case class MyComputation() extends MapReduceComputation[String, Int] {

    override def Mapper(buf: Rep[Array[Char]]): Rep[List[Tuple2[String, Int]]] = {
      var wordlist: Rep[List[Tuple2[String, Int]]] = List()
      var start = 0
      while (start < (buf.length - 1)) {
        while (buf(start) == ' ' || buf(start) == '\n' && start < (buf.length - 1)) start = start + 1
        var end = start + 1
        while ((buf(end) != ' ' || buf(end) != '\n') && (end < buf.length)) end = end + 1
        wordlist = Tuple2[String, Int](buf.slice(start, end).ArrayOfCharToString(), 1)::wordlist
        start = end
      }
      wordlist
    }
  }
}

object Main {

  def main(args: Array[String]): Unit = {
    val paths = List("/foo.txt")
    val snippet = new DslDriverCPP[Int, Unit] with MyFoo {
      q =>
      override val codegen = new DslGenCPP with CppCodeGen_List with CppCodeGen_Tuple with CCodeGenLibFunction {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        val res = HDFSExec(paths, MyComputation())
        println(res)
      }
    }
    println(snippet.code)
  }
}
