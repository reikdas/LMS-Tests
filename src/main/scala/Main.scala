import lms.collection.immutable.TupleOps
import lms.core.stub._
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}
import lms.thirdparty.CCodeGenLibFunction

object Main {
  def main(args: Array[String]): Unit = { // Remember to pass string arg
    val arg = args(0)
    val snippet = new DslDriverC[Int, Unit] with TupleOps {
      q =>
      override val codegen = new DslGenC with CCodeGenLibFunction {
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
