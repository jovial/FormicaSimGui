/**
 * Created by fluxoid on 17/02/17.
 */

import java.util.{Timer, TimerTask}
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import org.cowboycoders.ant.interfaces.AntTransceiver
import org.cowboycoders.ant.profiles.simulators._

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.binding.{Bindings, ObjectBinding}
import scalafx.beans.property.{BooleanProperty, DoubleProperty, ObjectProperty, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Group, Parent, Scene}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color._
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

case class Cell(label: String, value: StringProperty)
case class GridCellGroup(enabled: Option[BooleanProperty], cells: Array[Cell])

object HelloStageDemo extends JFXApp {

  val turbo = new DummyFecTurbo();
  val antInterface = new AntTransceiver(0);
  val antNode = new org.cowboycoders.ant.Node(antInterface);
  val turboModel = turbo.getState

  val turboThread = new Thread() {
    override def run() = {
      println("Node starting")
      antNode.start()
      antNode.reset()
      turbo.start(antNode)
    }
  }
  turboThread.start()

  val timer = new Timer()

  val power = new StringProperty() {
    value = "0"
  }

  val speed = new StringProperty() {
    value = "10"
  }

  val hr = new StringProperty() {
    value = "75"
  }

  val cadence = new StringProperty() {
    value = "75"
  }

  val distance = new StringProperty() {
    value = "0.00"
  }

  val gearRatio = new StringProperty() {
    value = "4.00"
  }

  val gradient = new StringProperty
  val coeffRolling = new StringProperty

  val bikeWeight = new StringProperty
  val userWeight = new StringProperty
  val userAge = new StringProperty
  val userHeight = new StringProperty
  val wheelDiameter = new StringProperty
  val windSpeed = new StringProperty
  val windCoeff = new StringProperty
  val draftingFactor = new StringProperty

  val powerLabel = "Power/w"
  val speedLabel = "Speed/kmh"
  val heartRateLabel = "Heart rate/bpm"
  val cadenceLabel = "Cadence/rpm"
  val distanceLabel = "Distance/km"
  val gearRatioLabel = "Gear ratio"
  val bikeWeightLabel = "Bike weight/kg"
  val userWeightLabel = "User weight/kg"
  val userAgeLabel = "User age/years"
  val userHeightLabel = "User height/cm"
  val wheelDiaLabel = "Wheel diameter/m"
  val coeffRollingLabel = "Coeff. rolling r."
  val gradientLabel = "Gradient/%"

  val windSpeedLabel = "Wind speed/m/s"
  val windCoeffLabel = "Wind coeff."
  val draftingFactorLabel = "Drafting factor"

  timer.scheduleAtFixedRate(new TimerTask() {
    override def run = {
      Platform.runLater {
        () =>
          {
            power.value = turboModel.getPower.toString()
            speed.value = "%2.2f".format(turboModel.getSpeed.doubleValue())
            hr.value = Option(turboModel.getHeartRate).getOrElse(0).toString()
            cadence.value = turboModel.getCadence.toString
            distance.value = "%2.2f".format((turboModel.getDistance / 1000.0))
            gearRatio.value = "%2.2f".format(turboModel.getGearRatio)
            bikeWeight.value = "%2.2f".format(turboModel.getBikeWeight)
            val athlete = turboModel.getAthlete
            userWeight.value = "%2.2f".format(athlete.getWeight)
            userHeight.value = "%2.0f".format(athlete.getHeight)
            userAge.value = "%d".format(athlete.getAge)
            wheelDiameter.value = "%2.2f".format(turboModel.getWheelDiameter)
            gradient.value = "%2.2f".format(turboModel.getTrackResistance.getGradient)
            coeffRolling.value = "%2.2f".format(turboModel.getTrackResistance.getCoefficientRollingResistance)
            val windResistance = turboModel.getWindResistance
            windSpeed.value = "%d".format(windResistance.getWindSpeed)
            draftingFactor.value = "%2.2f".format(windResistance.getDraftingFactor)
            windCoeff.value = "%2.2f".format(windResistance.getWindResistanceCoefficent)

          }

      }
    }
  }, 2000, 500)

  val simulationCellsEnabled = new BooleanProperty {
    value = true
  }

  val telemetryProps = Array(
    GridCellGroup(None, Array(Cell(powerLabel, power),
      Cell(speedLabel, speed), Cell(heartRateLabel, hr),
      Cell(cadenceLabel, cadence), Cell(distanceLabel, distance), Cell(gearRatioLabel, gearRatio))),
    GridCellGroup(Some(simulationCellsEnabled), Array(
      Cell(bikeWeightLabel, bikeWeight), Cell(userWeightLabel, userWeight), Cell(userAgeLabel, userAge),
      Cell(userHeightLabel, userHeight), Cell(wheelDiaLabel, wheelDiameter), Cell(coeffRollingLabel, coeffRolling),
      Cell(gradientLabel, gradient), Cell(windSpeedLabel, windSpeed), Cell(windCoeffLabel, windCoeff),
      Cell(draftingFactorLabel, draftingFactor))))

  def genSplit: Parent = {

    val telemetryGrid = new TilePane {
      tileAlignment = Pos.CenterLeft
      padding = Insets(20)
      hgap = 10
      children = genCellGroups()
    }

    telemetryProps.map(_.enabled).filter(_.nonEmpty).map(_.get).foreach {
      (e) =>
        e.onChange {
          (_, _, _) => telemetryGrid.children = genCellGroups()
        }
    }

    val scroll = new ScrollPane() {
      content = telemetryGrid
      fitToWidth = true
      fitToHeight = true
    }

    scroll.width.onChange {
      (_, old, newV) =>
        {
          val size = newV.doubleValue();
          var cols = 1
          if (size >= 600) {
            cols = 5
          } else if (size >= 380) {
            cols = 3
          } else if (size >= 200) {
            cols = 2
          }

          val actual = (size - 40 - 10 * cols) / cols

          theWidth.value = actual

        }
    }

    val powerInput = mkInput(
      StringProperty("Power"),
      StringProperty("Set power (w)"),
      validateNumber,
      getInvalidNumTxt,
      (v) => turbo.setPower(Integer.parseUnsignedInt(v)))

    val hrInput = mkInput(
      StringProperty("Heart rate"),
      StringProperty("Set heart rate (bpm)"),
      validateNumber,
      getInvalidNumTxt,
      (v) => turbo.setHeartrate(Integer.parseUnsignedInt(v)))

    val button = new Button {
      text = "press me"
      onAction = { (e: ActionEvent) =>
        {
          simulationCellsEnabled.value = !simulationCellsEnabled.value
        }
      }
    }

    val button2 = new Button {
      text = "press me"
      onAction = { (e: ActionEvent) => windSpeed.value = "" }
    }

    val rightSide = new TilePane {
      margin = Insets(10)
      prefColumns = 1
      children = Seq(powerInput, hrInput, button, button2)
    }

    val border = new BorderPane

    val statusLight = new Rectangle {
      height = 30
      fill = Color.Green
      width <== 30
    }

    val statusText = new Text {
      text = "hello"
    }

    val statusPane = new BorderPane {
      center = statusText
      hgrow = Priority.Always
      style = "-fx-background-color: white"
    }

    val status = new HBox {
      children = Seq(statusLight, statusPane)
      style = "-fx-border-width: 1; -fx-border-style: solid;"
    }

    border.center = scroll
    border.right = rightSide
    border.bottom = status

    border
  }

  def mkInput(labelTxt: StringProperty, promptTxt: StringProperty, validate: (scalafx.scene.control.TextField) => Boolean,
              getErrorTxt: (scalafx.scene.control.TextField) => String, onAccept: (String) => Unit) = {
    val inputField = new TextField() {
      promptText <== promptTxt
    }

    // validate an integer
    inputField.focused.onChange {
      ((
        s, o, n) => if (!n && !validate(inputField))
        // unfocused
        inputField.text = "")
    }

    inputField.onAction = (ae: ActionEvent) => {
      if (validate(inputField)) {
        onAccept(inputField.text.value)
        inputField.text = ""
      } else {

        new Alert(AlertType.Error) {
          title = "Error"
          contentText = getErrorTxt(inputField)
        }.showAndWait()
      }

    }

    val inputLabel = new Label {
      text <== labelTxt
      labelFor = inputField
    }

    val inputPair = new VBox {
      //margin = Insets(10)
      padding = Insets(5)
      children = Seq(inputLabel, inputField)
    }
    inputPair
  }

  def getInvalidNumTxt(field: scalafx.scene.control.TextField) = {
    field.text.value + " is invalid. You must enter a valid number"
  }

  def validateNumber(field: scalafx.scene.control.TextField) = field.text.value.matches("[0-9]+")

  var largest: ObservableValue[javafx.geometry.Bounds, javafx.geometry.Bounds] = null;

  val theWidth = new DoubleProperty {
    value = 1
  }

  def genCellGroups(): List[Group] = {
    var cells = List[Group]()
    for (cg <- telemetryProps) {
      if (cg.enabled.getOrElse(new BooleanProperty { value = true }).value) {
        cells ++= genCells(cg.cells).reverse
      }
    }
    cells
  }

  def genCells(rawCells: Array[Cell]): List[Group] = {
    var labelValuePairs = List[VBox]()
    var cells = List[Group]()

    val scaleFactor = new DoubleProperty {
      value = 1
    }

    for (a <- 0 until rawCells.length) {
      val label = new Label {
        text = rawCells(a).label
      }

      val value = new Text {
        text <== rawCells(a).value
        style = "-fx-font-size: 200%"
      }

      val labelDataPair = new VBox() {
        children = Seq(label, value)
        padding = Insets(20)
      }

      labelDataPair.boundsInLocal.onChange {
        (_, o, n) =>
          {
            val a = n.getWidth
            labelDataPair.scaleX <== scaleFactor
            labelDataPair.scaleY <== scaleFactor
          }
      }

      // second group to take into account scaled bounds
      val gridCell = new Group {
        children = labelDataPair
      }

      labelValuePairs = labelDataPair :: labelValuePairs
      cells = gridCell :: cells

    }

    def helper(a: ObservableValue[javafx.geometry.Bounds, javafx.geometry.Bounds], b: ObservableValue[javafx.geometry.Bounds, javafx.geometry.Bounds]): ObjectBinding[javafx.geometry.Bounds] = {
      Bindings.createObjectBinding(() => {
        (Option(a.value), Option(b.value)) match {
          case (Some(a), Some(b)) => if (a.getWidth > b.getWidth) a else b
          case (_, Some(b))       => b
          case (Some(a), _)       => a
          case _                  => throw new RuntimeException()
        }

      },
        a, b)
    }

    val ty = new ObjectProperty[javafx.geometry.Bounds]() {
    }
    largest = labelValuePairs.map(_.boundsInLocal).foldLeft(ty.asInstanceOf[ObservableValue[javafx.geometry.Bounds, javafx.geometry.Bounds]])((a, b) => helper(a, b))

    largest.onChange {
      (_, a, b) =>
        {
          //scaleFactor.unbind() // unsure if this is necessary
          scaleFactor <== theWidth / (b.getWidth / 0.98)
        }
    }

    cells
  }

  val split = genSplit

  val theScene = new Scene {
    fill = LightGreen
    root = split
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Formica Sim"
    width = 600
    height = 450
    scene = theScene
  }

  // kill the background thread
  stage.onCloseRequest = new EventHandler[WindowEvent] {

    override def handle(e: WindowEvent): Unit = {
      Platform.exit();
      antNode.stop()
      System.exit(0);
    }

  }
}
