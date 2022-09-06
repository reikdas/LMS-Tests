import flare.{Config, FlareBackend, FlareOps}
import lms.macros.SourceContext
import lms.core.{Backend, virtualize}
import flare.debug.LOGLevel

object myConfig {
  val config = Config(
    folder = "/tmp/",
    boundcheck = true,
    comLogLvl = LOGLevel.ERROR,
    runLogLvl = LOGLevel.ALL)
}

class MyBackend extends FlareBackend(myConfig.config) with FlareOps {

  def readInt: Rep[Int] = unchecked[Int]("read_int(0)")
  def auxReadString(len: Rep[Int]): Rep[String] = unchecked[String]("unsafe_read_str(",len,", 0)")
  def readString = {
    val len = readInt
    (auxReadString(len), len)
  }

  def readRecord(schema: Schema): Record = {
    Record.column(schema, schema map {
      case _: IntField => IntValue(readInt, false)
      case _: StringField =>
        val (str, len) = readString
        StringValue(str, len, false)
    })
  }

  @virtualize
  def cmain2(argc: Rep[Int], args: Rep[Array[String]]) = {
    // We want - arr[(2, "5"), (4, "7"), (5, "9")]
    val input = Seq(2, "5", 4, "7", 5, "9")
    val len = 3L // len(arr)
    val reclen = 2 // reclen-tuple
    val schema = Seq(IntField("Bar", nullable = false), StringField("Foo", nullable = false))
    val nativeBuf = Buffer.flat(schema, 1, len)

    nativeBuf.fill { _ => readRecord(schema) }

    var st: Int = 0
    for (i <- 0 until len: Range) {
      val rec = nativeBuf(i.toLong)
      val values = input.slice(st, st + reclen)
      st += reclen
      for ((x, n) <- values zip schema) {
        if ((rec(n.name) equalsTo Value(x)).toBool) println(1) else println(0)
      }
    }
  }

  @virtualize
  override def cmain(
                      argc: Rep[Int],
                      args: Rep[Array[String]]
                    ): Rep[Unit] = {
    // Expecting arr[("thestring", 4)]
    val schema: Schema = Seq(StringField("Foo", nullable = false), IntField("Bar", nullable = false))
    val nativeBuf = Buffer.flat(schema, 1, 10L)
    //nativeBuf.fill(_ => Record.column(schema, Seq(StringValue("thestring", 9, true), IntValue(argc, true))))
    nativeBuf(0L) = Record.column(schema, Seq(StringValue("thestring", 9, true), IntValue(argc, true)))
    println(nativeBuf(0L)(schema(1).name) equalsTo Value(1))
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val backend = new MyBackend()
    backend.stage
  }
}
