package tyrian.ui

trait UIElement[+Msg]:

  def toHtml: Theme ?=> tyrian.Html[Msg]
