import lms.core.stub._
import lms.macros.SourceContext
import lms.core.virtualize

object Main {
  def main(args: Array[String]): Unit = { // Remember to pass string arg
    val snippet = new DslDriverCPP[Int, Unit] {
      q =>
      override val codegen = new DslGenCPP {
        val IR: q.type = q
      }

      @virtualize
      def snippet(dummy: Rep[Int]) = {
        val str1 = args(0)
        val str2 = args(1)
        val res: Rep[Boolean] = str1 < str2
        println(res)
      }
    }
    println(snippet.code)
  }
}
