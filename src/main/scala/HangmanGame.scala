import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control.{Alert, Button, Label, TextField, ComboBox}
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.geometry.Pos
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.text.{Font, FontWeight}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color

object HangmanGame extends JFXApp {

  val categories = Map(
    "Animals" -> List("CAT", "DOG", "ELEPHANT", "GIRAFFE", "KANGAROO", "RACCOON", "PENGUIN", "DOLPHIN", "OTTER", "WOLF", "KOALA", "BEAR", "PANDA", "TIGER", "HIPPOPOTAMUS"),
    "Foods" -> List("CHOCOLATE", "AVOCADO", "ORANGE", "PIZZA", "BANANA", "RAMEN", "SUSHI", "BURGER", "PASTA", "TIRAMISU", "SANDWICH", "DIMSUM"),
    "Nature" -> List("OCEAN", "MOUNTAIN", "RIVER", "FOREST", "DESERT", "WATERFALL", "VOLCANO", "BEACH", "ISLAND", "RAINFOREST", "SUNSET", "SUNRISE"),
    "Cars" -> List("MAZDA", "TOYOTA", "BMW", "MERCEDES", "PORSCHE", "LEXUS", "VOLVO", "AUDI", "VOLKSWAGEN", "BENTLEY", "LAMBORGHINI", "FERRARI"),
    "Countries" -> List("CANADA", "BRAZIL", "GERMANY", "JAPAN", "AUSTRALIA", "FRANCE", "SPAIN", "CHINA", "ITALY", "MALAYSIA", "SINGAPORE", "HONG KONG", "ICELAND"),
    "Sports" -> List("BASKETBALL", "TENNIS", "FOOTBALL", "SWIMMING", "BASEBALL", "HOCKEY", "GOLF", "VOLLEYBALL", "SURFING", "GYMNASTICS", "BOXING"),
  )

  var currentCategory = "Animals"
  var words = categories(currentCategory)
  var word = words(scala.util.Random.nextInt(words.length)).toUpperCase
  var guessedWord = Array.fill(word.length)('*')
  var attemptsLeft = 7
  var guessedLetters = Set[Char]()

  val wordLabel = new Label(guessedWord.mkString(" ")) {
    font = Font.font("Arial", FontWeight.Bold, 30)
    textFill = Color.Black
  }

  val messageLabel = new Label("Enter a letter to guess:") {
    font = Font.font("Arial", 18)
  }

  val hangmanImageView = new ImageView {
    fitWidth = 200
    fitHeight = 250
    image = new Image(getClass.getResourceAsStream("/img1.png"))
  }

  val hangmanImages = (1 to 8).map(i => new Image(getClass.getResourceAsStream(s"/img$i.png"))).toArray

  def updateHangmanImage(): Unit = {
    if (attemptsLeft < hangmanImages.length) {
      hangmanImageView.image = hangmanImages(7 - attemptsLeft)
    }
  }

  val keyboardButtons: Seq[Button] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { letter =>
    new Button(letter.toString) {
      onAction = _ => handleGuess(letter)
      style = "-fx-background-color: #ADD8E6; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;"
    }
  }

  val guessInput = new TextField {
    maxWidth = 50
    font = Font.font("Arial", 18)
  }

  val guessButton = new Button("Guess") {
    onAction = _ => {
      val guess = guessInput.text.value.toUpperCase
      if (guess.length == 1 && guess.forall(_.isLetter)) {
        handleGuess(guess.head)
        guessInput.text = ""
      } else {
        messageLabel.text = "Please enter a single letter."
      }
    }
    style = "-fx-background-color: #080808; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;"
  }

  def createKeyboard(): GridPane = {
    val keyboard = new GridPane {
      hgap = 5
      vgap = 5
      alignment = Pos.Center
    }

    for ((button, index) <- keyboardButtons.zipWithIndex) {
      keyboard.add(button, index % 10, index / 10)
    }

    keyboard
  }

  def handleGuess(letter: Char): Unit = {
    val guess = letter.toUpper
    if (guessedLetters.contains(guess)) {
      messageLabel.text = s"You already guessed the letter $guess."
    } else {
      guessedLetters += guess
      if (word.contains(guess)) {
        for (i <- word.indices) {
          if (word(i) == guess) guessedWord(i) = guess
        }
      } else {
        attemptsLeft -= 1
        updateHangmanImage()
        messageLabel.text = s"The letter $guess is not in the word."
      }

      wordLabel.text = guessedWord.mkString(" ")
    }

    if (guessedWord.mkString == word) {
      showAlert(AlertType.Information, "Congratulations!", s"You got it right!\nWord: $word")
      disableKeyboard()
    } else if (attemptsLeft == 0) {
      showAlert(AlertType.Error, "Game Over", s"The word was: $word")
      disableKeyboard()
    }
  }

  def showAlert(alertType: AlertType, title: String, content: String): Unit = {
    val alert = new Alert(alertType) {
      initOwner(stage)
      headerText = None
      contentText = content
    }
    alert.title = title
    alert.showAndWait()
  }

  def disableKeyboard(): Unit = {
    keyboardButtons.foreach(_.disable = true)
  }

  def enableKeyboard(): Unit = {
    keyboardButtons.foreach(_.disable = false)
  }

  def resetGame(): Unit = {
    word = words(scala.util.Random.nextInt(words.length)).toUpperCase
    guessedWord = Array.fill(word.length)('*')
    guessedLetters = Set[Char]()
    attemptsLeft = 7
    wordLabel.text = guessedWord.mkString(" ")
    messageLabel.text = "Enter a letter to guess:"
    hangmanImageView.image = new Image(getClass.getResourceAsStream("/img1.png"))
    enableKeyboard()
  }

  val keyboard = createKeyboard()

  val categorySelector = new ComboBox[String](categories.keys.toSeq) {
    value = currentCategory
    onAction = _ => {
      currentCategory = value.value
      words = categories(currentCategory)
      resetGame()
    }
  }

  stage = new JFXApp.PrimaryStage {
    title = "Hangman Game"
    scene = new Scene(600, 600) {
      content = new VBox(10) {
        alignment = Pos.Center
        children = Seq(
          categorySelector,
          hangmanImageView,
          wordLabel,
          messageLabel,
          new HBox(10) {
            alignment = Pos.Center
            children = Seq(guessInput, guessButton)
          },
          keyboard,
          new Button("Reset") {
            onAction = _ => resetGame()
            style = "-fx-background-color: #080808; -fx-text-fill: white;"
          }
        )
      }
    }
  }
}
