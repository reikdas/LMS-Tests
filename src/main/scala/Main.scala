import lms.collection.immutable.{CppCodeGen_List, ListOps}
import lms.collection.mutable.ArrayOps
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}

object Main {
  def main(args: Array[String]): Unit = { // Remember to pass string arg
    val arg = args(0)
    val snippet = new DslDriverCPP[Int, Unit] with ListOps with ArrayOps {
      q =>
      override val codegen = new DslGenCPP with CppCodeGen_List {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        val arr1 = NewArray[Char](3)
        val arr2 = NewArray[Char](3)
        arr1(0) = arg(0)
        arr2(0) = arg(3)
        arr1(1) = arg(1)
        arr2(1) = arg(4)
        arr1(2) = arg(2)
        arr2(2) = arg(5)
        var l: Rep[List[Array[Char]]] = List()
        l = arr1::l
        l = arr2::l
        for (i <- 0 until l.size) {
          for (j <- 0 until l(i).length)
            println(l(i)(j))
        }
      }
    }
    println(snippet.code)
  }
}
