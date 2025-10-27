package tyrian.ui.datatypes

import tyrian.next.Batch
import tyrian.ui.datatypes.DataSet.Column

final case class DataSet[DataType](
    columns: Batch[DataSet.Column[DataType, ?]],
    data: Batch[DataType]
):
  def addColumn[Field](
      name: String
  )(using column: DataSet.ColumnOps[DataType, Field], field: DataSet.FieldOps[Field]): DataSet[DataType] =
    this.copy(columns = columns :+ DataSet.Column[DataType, Field](name, column, field))
  def addColumn[Field](
      name: String,
      read: DataType => Field
  )(using field: DataSet.FieldOps[Field]): DataSet[DataType] =
    addColumn[Field](name)(using DataSet.ColumnOps(read), field)
  def addColumn[Field](name: String, read: DataType => Field, show: Field => String): DataSet[DataType] =
    addColumn[Field](name)(using DataSet.ColumnOps(read), DataSet.FieldOps(show))

  def withData(rows: Batch[DataType]): DataSet[DataType] =
    this.copy(data = rows)

  def headers: Batch[String] =
    columns.map(_.name)

  def rows: Batch[Batch[String]] =
    data.map { value =>
      columns.map { case Column(name, column, field) =>
        (column.read andThen field.show)(value)
      }
    }

object DataSet:

  def empty[DataType]: DataSet[DataType] =
    DataSet(Batch.empty, Batch.empty)

  final case class Column[DataType, Field](
      name: String,
      column: ColumnOps[DataType, Field],
      field: FieldOps[Field]
  )

  trait ColumnOps[DataType, Field]:
    def read(data: DataType): Field

  object ColumnOps:

    def apply[DataType, Field](
        _read: DataType => Field
    ): ColumnOps[DataType, Field] =
      new ColumnOps[DataType, Field]:
        def read(data: DataType): Field = _read(data)

  trait FieldOps[Field]:
    def show(value: Field): String

  object FieldOps:

    def apply[Field](
        _show: Field => String
    ): FieldOps[Field] =
      new FieldOps[Field]:
        def show(value: Field): String = _show(value)

    given FieldOps[String] with
      def show(s: String): String = s

    given FieldOps[Int] with
      def show(i: Int): String = i.toString

    given FieldOps[Long] with
      def show(l: Long): String = l.toString

    given FieldOps[Double] with
      def show(d: Double): String = d.toString

    given FieldOps[Float] with
      def show(f: Float): String = f.toString

    given FieldOps[Boolean] with
      def show(b: Boolean): String = b.toString

    given [Field](using r: FieldOps[Field]): FieldOps[Option[Field]] with
      def show(ob: Option[Field]): String = ob.fold("")(r.show)
