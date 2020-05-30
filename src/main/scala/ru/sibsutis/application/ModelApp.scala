package ru.sibsutis.application

import java.io.{File, IOException}

import javafx.stage.Stage
import javax.imageio.ImageIO
import ru.sibsutis.application.model.AccessControlModel
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.property.BooleanProperty
import scalafx.embed.swing.SwingFXUtils
import scalafx.event.EventIncludes.handle
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button}
import scalafx.scene.image.{Image, WritableImage}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.paint.Color.White
import scalafx.scene.transform.Scale
import scalafx.scene.{Scene, SnapshotParameters}
import scalafx.stage.FileChooser.ExtensionFilter
import scalafx.stage.{FileChooser, Modality}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ModelApp extends JFXApp {

  implicit class ErrorMessageFuture[A](val future: Future[A]) extends AnyVal {
    def errorMessage(error: String = ""): Future[A] = future.recoverWith {
      case e: IOException => Future.failed(new Exception(error, e))
      case e: NumberFormatException => Future.failed(new Exception(error, e))
      case t: Throwable => Future.failed(t)
    }
  }

  System.setProperty("prism.lcdtext", "false")

  private val disableButtons = BooleanProperty(false)
  private val params = new SnapshotParameters {
    transform = new Scale(3.0, 3.0)
  }
  private val stylesheet = getClass.getResource("/css/stylesheet.css").toExternalForm
  private val windowIcon = new Image(getClass.getResource("/icons/window_icon.png").toString)

  private val fileChooser: FileChooser = new FileChooser() {
    private val folder = System.getProperty("user.home")
    extensionFilters.add(new ExtensionFilter("PNG (*.png)", "*.png"))
    initialDirectory = new File(folder)
  }

  private val alertDialog = new Alert(AlertType.Error) {
    initModality(Modality.ApplicationModal)
    initOwner(stage)
    dialogPane().getStylesheets.add(stylesheet)
    title = "Error"
    headerText = "An error has occurred"
    dialogPane().getScene.getWindow.asInstanceOf[Stage].getIcons.add(windowIcon)
  }

  private val xAxis = new NumberAxis("Priority", 0.0d, 100.0d, 10.0d)

  private val yAxis = new NumberAxis("Number of characters read", 0.0d, 26.0d, 2.0d)

  private val series1 = new XYChart.Series[Number, Number]() {
    name = "Reader 1"
  }

  private val series2 = new XYChart.Series[Number, Number]() {
    name = "Reader 2"
  }

  private val chart = new LineChart(xAxis, yAxis) {
    data.value.addAll(series1, series2)
    padding = Insets(30.0, 0.0, 30.0, 0.0)
    title = "Dependency plot"
  }

  private val startButton = new Button {
    alignmentInParent = Pos.Center
    defaultButton = true
    disable <== disableButtons
    styleClass add "start-btn"
    text = "START"

    onAction = handle {
      disableButtons.set(true)
      AccessControlModel.createModel.onComplete {
        case Success(values) => updatePlot(values)
          disableButtons.set(false)
        case Failure(throwable) => Platform runLater handleError(throwable.getLocalizedMessage)
      }
    }
  }

  private val saveButton = new Button {
    alignmentInParent = Pos.Center
    disable <== disableButtons
    styleClass add "browse-btn"
    text = "SAVE"

    onAction = handle {
      disableButtons.set(true)
      val image = chart.snapshot(params, null)
      Option apply fileChooser.showSaveDialog(stage) match {
        case Some(file) => saveImage(image, file)
        case None => disableButtons.set(false)
      }
    }
  }

  private val customScene: Scene = new Scene {
    fill = White
    stylesheets.add(stylesheet)

    root = new BorderPane {
      padding = Insets(30.0, 30.0, 30.0, 30.0)
      center = chart
      bottom = new HBox {
        alignment = Pos.BottomCenter
        children = Seq(startButton, saveButton)
        margin = Insets(20.0, 0.0, 20.0, 0.0)
        spacing = 100.0
      }
    }
  }

  stage = new PrimaryStage {
    icons.add(windowIcon)
    title.value = "Access control model"
    width = 800.0
    height = 600.0
    resizable = false
    scene = customScene
    onCloseRequest = handle {
      close
      Platform.exit()
      System.exit(0)
    }
  }

  private def handleError(message: String): Unit = {
    alertDialog.contentText = "Caused by:\n" + message
    alertDialog.show()
    disableButtons.set(false)
  }

  private def saveImage(image: WritableImage, file: File): Unit = Future {
    val bufferedImage = SwingFXUtils.fromFXImage(image, null)
    ImageIO.write(bufferedImage, "png", file)
  } onComplete {
    case Success(true) => disableButtons.set(false)
    case Success(false) => Platform runLater handleError("Failed to save image.")
    case Failure(throwable) => Platform runLater handleError(throwable.getLocalizedMessage)
  }

  private def updatePlot(values: List[List[Double]]): Unit = Platform runLater {
    series1.getData.clear()
    series2.getData.clear()
    for (i <- values.indices) {
      val List(reader1, reader2) = values(i)
      series1.getData.add(XYChart.Data(i + 1, reader1))
      series2.getData.add(XYChart.Data(i + 1, reader2))
    }
  }
}
