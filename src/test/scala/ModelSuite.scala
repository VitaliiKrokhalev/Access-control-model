import org.scalatest.flatspec.AsyncFlatSpec
import ru.sibsutis.application.model.AccessControlModel

class ModelSuite extends AsyncFlatSpec {
  behavior of "createModel"
  it should "eventually get a values of experiments" in {
    AccessControlModel.createModel map { values => assert(values.nonEmpty) }
  }
}