import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import scala.io.Source
import java.sql.{Connection, DriverManager, Statement, Timestamp}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
object RuleEngineProject extends App {
  // Read lines from the CSV file, skipping the header
  val lines = Source.fromFile("src/main/TRX1000.csv").getLines().toList.tail
  val logFile = new File("Rules_Engine.log")
  val writer = new FileWriter(logFile, true)

  val url = "jdbc:mysql://localhost:3306/scala" // JDBC URL
  val user = "root"
  val password = "root"
  // Connect to the database
  logMessage("INFO", "Connected to the database!")
  val connection: Connection = DriverManager.getConnection(url, user, password)

  // Function to write data to the database
  def write_Data(Line: String) = {
    // Split the line into individual parameters
    val OrderArr = Line.split(",")

    // Parse and convert each parameter to the appropriate data type
    val orderTime = java.sql.Date.valueOf(OrderArr(0).substring(0, 10))
    val productName = OrderArr(1)
    val expiryDate = java.sql.Date.valueOf(OrderArr(2))
    val quantity = OrderArr(3).toInt
    val unitPrice = OrderArr(4).toDouble
    val channel = OrderArr(5)
    val paymentMethod = OrderArr(6)
    val discount = OrderArr(7).toDouble
    val totalPrice = OrderArr(8).toDouble

    // Prepare SQL insert statement
    val insertStatement = connection.prepareStatement(
      "INSERT INTO Orders (OrderDate, ProductName, ExpiryDate, Quantity, UnitPrice, Channel, PaymentMethod, Discount, TotalPrice) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    insertStatement.setDate(1, orderTime)
    insertStatement.setString(2, productName)
    insertStatement.setDate(3, expiryDate)
    insertStatement.setInt(4, quantity)
    insertStatement.setDouble(5, unitPrice)
    insertStatement.setString(6, channel)
    insertStatement.setString(7, paymentMethod)
    insertStatement.setDouble(8, discount)
    insertStatement.setDouble(9, totalPrice)
    insertStatement.executeUpdate()
  }

  // Function to log messages with a timestamp
  def logMessage(logLevel: String, Message: String) = {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val logMessage = s"$timestamp $logLevel $Message"
    writer.write(logMessage + "\n")
    writer.flush()

  }


  // Function to check if the expiry date is within 30 days
  def qualifyingExpireDate(line: String): Boolean = {
    val date = new SimpleDateFormat("yyyy-MM-dd").parse(line.split(",")(0).substring(0, 10))
    val expiry_date = new SimpleDateFormat("yyyy-MM-dd").parse(line.split(",")(2))
    val result = TimeUnit.MILLISECONDS.toDays(expiry_date.getTime - date.getTime) < 30
    result
  }

  // Function to calculate discount based on remaining days to expiry
  def discountBasedOnRemainingDays(line: String): Double = {
    val date = new SimpleDateFormat("yyyy-MM-dd").parse(line.split(",")(0).substring(0, 10))
    val expiry_date = new SimpleDateFormat("yyyy-MM-dd").parse(line.split(",")(2))
    val day_diff = TimeUnit.MILLISECONDS.toDays(expiry_date.getTime - date.getTime)
    val result = 30 - day_diff
    result.toInt / 100.0
  }

  // Function to check if the product name qualifies for discount
  def qualifyingProductName(line: String): Boolean = {
    val productType = line.split(",")(1).takeWhile(_ != '-').toLowerCase().trim
    productType == "cheese" || productType == "wine"
  }

  // Function to determine discount based on product name
  def discountProductName(line: String): Double = {
    val product_type = line.split(",")(1).takeWhile(_ != '-')
    val result =
      if (product_type.toLowerCase().trim == "cheese")
        0.1
      else if (product_type.toLowerCase().trim == "wine")
        0.05
      else
        0.0
    result
  }

  // Function to check if the order date is March 23rd
  def qualifyMarch23rd(line: String): Boolean = {
    val date = new SimpleDateFormat("yyyy-MM-dd").parse(line.split(",")(0).substring(0, 10))
    val targetDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-03-23")
    date == targetDate
  }


  def discountForMarch23rd(line: String): Double = {
    0.5
  }

  // Function to check if the purchase quantity is more than five
  def qualifyMoreThanFivePurchases(line: String): Boolean = {
    val quantity = line.split(",")(3).toInt
    quantity > 5
  }

  // Function to calculate discount based on quantity
  def discountForMultiplePurchases(line: String): Double = {
    val quantity = line.split(",")(3).toInt
    val result =
      if (quantity >= 6 && quantity <= 9) {
        0.05
      } else if (quantity >= 10 && quantity <= 14) {
        0.07
      } else {
        0.1
      }
    result
  }

  // Function to check if the sales channel is 'app
  def qualifyChannel(line: String): Boolean = {
    val channel = line.split(",")(5).toLowerCase()
    channel == "app"
  }

  // Function to calculate discount based on the sales channel
  def discountChannel(line: String): Double = {
    val quantity = line.split(",")(3).toInt
    //Equation Can Round up the quantity to the nearest multiple of 5
    //General Equation=(num+(n-1)/n)*n ->
    // num: The original number that needs to be rounded.
    //n: The value to which num is being rounded, i.e., the nearest multiple.
    val roundedQuantity = ((quantity + 4) / 5) * 5
    roundedQuantity / 100.0
  }

  // Function to check if the payment method is 'visa'
  def qualifyPaymentMethod(line: String): Boolean = {
    val paymentMethod = line.split(",")(6).toLowerCase()
    paymentMethod == "visa"
  }

  // Function to apply a fixed discount for 'visa' payment method
  def discountPaymentMethod(line: String): Double = {
    0.05
  }

  // Function to get all discount rules
  def getDiscountRules: List[(String => Boolean, String => Double)] = {
    List(
      (qualifyingExpireDate, discountBasedOnRemainingDays),
      (qualifyingProductName, discountProductName),
      (qualifyMarch23rd, discountForMarch23rd),
      (qualifyMoreThanFivePurchases, discountForMultiplePurchases),
      (qualifyChannel, discountChannel),
      (qualifyPaymentMethod, discountPaymentMethod)
    )
  }

  // Function to calculate discounts for a line of data
  def calculateDiscounts(line: String, getDiscount: List[(String => Boolean, String => Double)]): String = {
    val discounts = getDiscount.filter { case (qualifyingCondition, _) => qualifyingCondition(line) }.map { case (_, getDiscount) => getDiscount(line) }
    val sortedDiscount = discounts.sortBy(identity).reverse

    val avgDiscounts =
      if (sortedDiscount.size >= 2) {
        ((sortedDiscount.head + sortedDiscount(1)) / 2)
      } else if (sortedDiscount.size == 1) {
        (sortedDiscount.head)
      } else {
        0.0
      }
    val quantity = line.split(',')(3).toInt
    val unitPrice = line.split(',')(4).toDouble
    val finalPrice = quantity * unitPrice - (quantity * unitPrice * avgDiscounts)
    line + "," + (math.round(avgDiscounts * 100) / 100.0).toDouble + "," + (math.round(finalPrice * 100) / 100.0).toDouble
  }

  try {
    // Process each line of the CSV file
    lines.foreach { line =>
      val result = calculateDiscounts(line, getDiscountRules).split(",")
      logMessage("INFO", s"Order Processing : ${line}" +
        s" With Discount ${result(7)} and Total Price ${result(8)} ")
      write_Data(calculateDiscounts(line, getDiscountRules))
    }
    connection.close()
  } catch {
    case e: Exception => // Handle exceptions here

      logMessage("ERROR", s"Error occurred: ${e.getMessage}")
  } finally {
    // Close the writer in the finally block to ensure it's always closed, even if an exception occurs
    writer.close()
  }

}

