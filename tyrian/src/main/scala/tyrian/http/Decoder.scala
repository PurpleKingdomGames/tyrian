package tyrian.http

/** Tries to transform an response body or http error into to a value of type Msg.
  * @tparam Msg
  *   Message type of the successfully decoded response
  */
opaque type Decoder[Msg] = Response | HttpError => Msg
object Decoder:

  /** Construct a decoder from two functions that handle a `Response` or an `HttpError`. Alias for `Decoder.fold`
    */
  def apply[Msg](onResponse: Response => Msg, onError: HttpError => Msg): Decoder[Msg] =
    fold(onResponse, onError)

  /** Unintelligently returns the body or the error message as a string in a message.
    */
  def asString[Msg](toMsg: String => Msg): Decoder[Msg] =
    Decoder.fold(
      r => toMsg(r.body),
      e => toMsg(e.toString)
    )

  /** Construct a decoder from two functions that handle a `Response` or an `HttpError`.
    */
  def fold[Msg](onResponse: Response => Msg, onError: HttpError => Msg): Decoder[Msg] =
    (p: Response | HttpError) =>
      p match
        case r: Response  => onResponse(r)
        case e: HttpError => onError(e)

  extension [Msg](d: Decoder[Msg])
    /** Convert a `Response` or an `HttpError` into a `Msg`.
      */
    def apply(result: Response | HttpError): Msg =
      d(result)

    /** Convert a `Response` into a `Msg`.
      */
    def withResponse(result: Response): Msg =
      d(result)

    /** Convert an `HttpError` into a `Msg`.
      */
    def withError(result: HttpError): Msg =
      d(result)
