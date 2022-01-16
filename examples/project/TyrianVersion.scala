object TyrianVersion {
  def getVersion: String = {
    def rec(path: String, levels: Int, version: Option[String]): String = {
      val msg = "ERROR: Couldn't find Tyrian version."
      version match {
        case Some(v) => 
          println(s"""Tyrian version set to '$v'""")
          v

        case None if levels < 3 =>
          try {
            val v = scala.io.Source.fromFile(path).getLines.toList.head
            rec(path, levels, Some(v))
          } catch {
            case _: Throwable =>
              rec("../" + path, levels + 1, None)
          }

        case None =>
          println(msg)
          throw new Exception(msg)
      }
    }
    
    rec(".tyrian-version", 0, None)
  }
}
