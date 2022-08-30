import lms.collection.immutable.{CppCodeGen_List, CppCodeGen_Tuple, ListOps, TupleOps}
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}

object Main {
  def main(args: Array[String]): Unit = { // Remember to pass string arg
    val arg = args(0)
    val snippet = new DslDriverCPP[Int, Unit] with ListOps with TupleOps {
      q =>
      override val codegen = new DslGenCPP with CppCodeGen_List with CppCodeGen_Tuple {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        val foo: Rep[Tuple2[Char, Char]] = Tuple2[Char, Char](arg(0), arg(1))
        val bar: Rep[Tuple2[Char, Char]] = Tuple2[Char, Char](arg(2), arg(3))
        var l: Rep[List[Tuple2[Char, Char]]] = List()
        l = foo::l
        l = bar::l
        for (i <- 0 until l.size) {
          println(l(i)._1)
          println(l(i)._2)
        }
      }
    }
    println(snippet.code)
  }
}
