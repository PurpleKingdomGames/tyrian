package example

final case class Time(running: Double, delta: Double):
  def next(t: Double): Time =
    this.copy(running = t, delta = t - running)
