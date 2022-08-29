import lms.collection.immutable.{CppCodeGen_Tuple, TupleOps}
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}

object Main {
  def main(args: Array[String]): Unit = { // Remember to pass string arg
    val arg = args(0)
    val snippet = new DslDriverCPP[Int, Unit]  with TupleOps {
      q =>
      override val codegen = new DslGenCPP with CppCodeGen_Tuple {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        val foo: Rep[Tuple2[Char, Char]] = Tuple2[Char, Char](arg(0), arg(1))
        println(foo._1)
        println(foo._2)
      }
    }
    println(snippet.code)
  }
}
