package tyrian.htmx

enum Event(val name: String):
  case DOMContent         extends Event("DOMContentLoaded")
  case Afterprint         extends Event("afterprint")
  case Beforeprint        extends Event("beforeprint")
  case Beforematch        extends Event("beforematch")
  case Beforetoggle       extends Event("beforetoggle")
  case Beforeunload       extends Event("beforeunload")
  case Blur               extends Event("blur")
  case Cancel             extends Event("cancel")
  case Change             extends Event("change")
  case Click              extends Event("click")
  case Close              extends Event("close")
  case Connect            extends Event("connect")
  case Contextlost        extends Event("contextlost")
  case Contextrestored    extends Event("contextrestored")
  case Currententrychange extends Event("currententrychange")
  case Dispose            extends Event("dispose")
  case Error              extends Event("error")
  case Focus              extends Event("focus")
  case Formdata           extends Event("formdata")
  case Hashchange         extends Event("hashchange")
  case Input              extends Event("input")
  case Invalid            extends Event("invalid")
  case KeyDown            extends Event("keydown")
  case KeyPress           extends Event("keypress")
  case KeyUp              extends Event("keyup")
  case Languagechange     extends Event("languagechange")
  case Load               extends Event("load")
  case Message            extends Event("message")
  case Messageerror       extends Event("messageerror")
  case Navigate           extends Event("navigate")
  case Navigateerror      extends Event("navigateerror")
  case Navigatesuccess    extends Event("navigatesuccess")
  case Offline            extends Event("offline")
  case Online             extends Event("online")
  case Open               extends Event("open")
  case Pagehide           extends Event("pagehide")
  case Pagereveal         extends Event("pagereveal")
  case Pageshow           extends Event("pageshow")
  case Pointercancel      extends Event("pointercancel")
  case Popstate           extends Event("popstate")
  case Readystatechange   extends Event("readystatechange")
  case Rejectionhandled   extends Event("rejectionhandled")
  case Reset              extends Event("reset")
  case Select             extends Event("select")
  case Storage            extends Event("storage")
  case Submit             extends Event("submit")
  case Toggle             extends Event("toggle")
  case Unhandledrejection extends Event("unhandledrejection")
  case Unload             extends Event("unload")
  case Visibilitychange   extends Event("visibilitychange")
