package tyrian

/** A command describes some side-effect to perform.
  *
  * The difference with a `Task` is that a command never produces error values.
  */
// FIXME Unify Cmd and Task (Cmd is just a Task[Nothing, ?])
sealed trait Cmd[+Msg]:
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[OtherMsg]

object Cmd:
  given CanEqual[Cmd[_], Cmd[_]] = CanEqual.derived

  final case class RunTask[Err, Success, Msg](task: Task[Err, Success], f: Either[Err, Success] => Msg) extends Cmd[Msg]:
    def map[OtherMsg](g: Msg => OtherMsg): Cmd[OtherMsg] = RunTask(task, f andThen g)
  
  case object Empty extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Cmd[OtherMsg] = this
