package example

import example.models.Page
import example.models.WSStatus
import org.scalajs.dom
import tyrian.cmds.ImageLoader
import tyrian.http.Response

import scala.scalajs.js

enum Msg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case FromSocket(message: String)
  case ToSocket(message: String)
  case FocusOnInputField
  case Log(msg: String)
  case WebSocketStatus(status: WSStatus)
  case Clear
  case StageSaveData(content: String)
  case Save(key: String, value: String)
  case Load(key: String)
  case ClearStorage(key: String)
  case DataLoaded(data: String)
  case NavigateTo(page: Page)
  case NavigateToUrl(href: String)
  case JumpToHomePage
  case OverwriteModel(model: Model)
  case TakeSnapshot
  case NewTime(time: js.Date)
  case MakeHttpRequest
  case GotHttpResult(response: Response)
  case GotHttpError(message: String)
  case UpdateHttpDetails(newUrl: String)
  case UpdateHttpBody(body: String)
  case UpdateHttpMethod(method: String)
  case UpdateHttpCredentials(credentials: String)
  case UpdateHttpTimeout(timeout: String)
  case UpdateHeaderKey(headerIndex: Int, key: String)
  case UpdateHeaderValue(headerIndex: Int, value: String)
  case UpdateHeaderAdd
  case UpdateHeaderRemove(headerIndex: Int)
  case UpdateHttpCache(cache: String)
  case FrameTick(runningTime: Double)
  case MouseMove(to: (Int, Int))
  case NewFlavour(name: String)
  case AddFruit
  case UpdateFruitInput(input: String)
  case ToggleFruitAvailability(name: String)
  case SelectImageFile
  case ReadImageFile(file: dom.File)
  case FileImageRead(fileData: String)
  case SelectTextFile
  case ReadTextFile(file: dom.File)
  case FileTextRead(fileData: String)
  case SelectBytesFile
  case ReadBytesFile(file: dom.File)
  case FileBytesRead(fileData: IArray[Byte])
  case LoadImage
  case UpdateImageUrl(url: String)
  case ImageLoaded(result: ImageLoader.Result)
  case NoOp
