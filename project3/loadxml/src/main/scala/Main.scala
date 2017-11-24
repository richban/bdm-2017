object Main extends App {
  
  type ParsedEdge = (String, String, String, String)
  
  def getEdgeData(t: String, m: String, e: String, l: String): ParsedEdge = {
    val edge_type = Option(t).map(_ => "intersection").getOrElse("road")
    val movement = Option(m).map(_ => "backward").getOrElse("forward")
    (edge_type, movement, e, l)
  }

  def parseEdge(edge: String): ParsedEdge = {
    val Pattern = raw"^(:)?(-)?(\d+)_(\d).*$$".r
    edge match {
      case Pattern(t, m, e, l) => getEdgeData(t, m, e, l)
      case _ => null
    }
  }
}
