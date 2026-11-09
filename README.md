# 🏃‍♀️💪 SmartBMI – Your Personal BMI Tracker

SmartBMI is an Android application designed to help users calculate their Body Mass Index (BMI), understand their health status, and monitor their BMI progress over time.

The application combines a modern user interface with secure authentication, local and cloud data storage, interactive BMI visualisation, and accessibility features.

Built with Kotlin and modern Android development technologies, SmartBMI demonstrates practical mobile application development, Firebase integration, database management, and user-focused design.

---

## ✨ Features

### 🖼️ Splash Screen
- Custom SmartBMI logo and application branding.
- Branded introduction when launching the application.

### 👋 Welcome & Authentication
- Firebase Authentication.
- Register and log in using email and password.
- Google Sign-In.
- Facebook Sign-In.
- Forgot Password functionality.
- Password visibility toggle.
- Email and password validation.
- User-friendly authentication feedback.
- Android Credential Manager integration.

### 📝 User Registration
- Dedicated account registration screen.
- Email address input.
- Password and password confirmation fields.
- Password visibility controls.
- Terms of Agreement acceptance.
- Account creation using Firebase Authentication.
- Input validation and error handling.

### ⚖️ BMI Calculator
- Calculate BMI using the user's weight and height.
- Capture personal information, including gender and age.
- Calculate BMI using the standard formula:

  **BMI = Weight (kg) / Height (m)²**

- Categorise BMI results into four health categories:

  - 🟦 Underweight
  - 🟩 Normal Weight
  - 🟧 Overweight
  - 🟥 Obese

### 🌈 Semi-Circular BMI Gauge
- Modern semi-circular BMI gauge replacing the traditional linear colour ruler.
- Colour-coded BMI ranges representing different health categories.
- Visual indicator showing the user's BMI position.
- Provides an intuitive visual representation of BMI results.

### 📊 BMI Result Display
- Displays the calculated BMI value.
- Shows the corresponding BMI health category.
- Provides visual feedback through the semi-circular gauge.
- Displays health messages based on the latest BMI calculation.

### 💾 BMI Data Storage
- Store BMI records locally using Room Database.
- Save BMI records to Firebase Cloud Firestore.
- Maintain previously calculated BMI results.
- Support access to saved BMI records.

### 📅 BMI History
- View previously saved BMI records.
- Display BMI values and calculation dates.
- Track BMI progress over time.
- Visualise historical BMI data using charts.
- Present saved records using CardView components.
- Custom back-arrow navigation.

### 🗣️ Text-to-Speech
- Provides audio feedback for BMI results and health categories.
- Improves accessibility and user experience.

### 🔐 Logout
- Firebase Authentication logout functionality.
- Allows users to securely sign out of their accounts.

---

## 📷 Screenshots

### 01. Splash Screen
![SmartBMI Splash Screen](app/screenshots/01_splash_screen.jpeg)

### 02. Welcome & Login Screen
![SmartBMI Welcome and Login Screen](app/screenshots/02_welcome_screen.jpeg)

### 03. Sign-Up Screen
![SmartBMI Sign-Up Screen](app/screenshots/03_sign_up_screen.jpeg)

### 04. Main BMI Calculator Screen
![SmartBMI Main Screen](app/screenshots/04_main_screen.jpeg)

### 05. BMI History Screen
![SmartBMI History Screen](app/screenshots/05_history_screen.jpeg)

---

## 🛠️ Technologies & Tools

| Category | Technology |
|---|---|
| Programming Language | Kotlin |
| IDE | Android Studio |
| Authentication | Firebase Authentication |
| Cloud Database | Firebase Cloud Firestore |
| Local Database | Room Database |
| Charts & Visualisation | MPAndroidChart |
| UI Development | XML Layouts, Activities, ConstraintLayout, CardView |
| Authentication Support | Android Credential Manager |
| Accessibility | Android Text-to-Speech |
| Version Control | Git & GitHub |

---

## 🚀 Installation

### Prerequisites
- Android Studio.
- Android SDK.
- JDK 17.
- Firebase project configuration.

### Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/RapolaiKate/SmartBMI.git
   ```

2. Open the project in Android Studio.

3. Configure your Firebase project and add your own `google-services.json` file inside the `app` directory.

4. Enable the required Firebase Authentication providers.

5. Configure Cloud Firestore for cloud data storage.

6. Sync the project with Gradle files.

7. Build and run the application on an Android emulator or physical Android device.

**Note:** Firebase services require valid project configuration. Google and Facebook authentication may also require their respective provider credentials and configuration.

---

## 📝 How to Use

1. Launch the SmartBMI application.
2. Register a new account or log in using an existing account.
3. Alternatively, use Google or Facebook sign-in.
4. Enter the required personal information.
5. Provide your weight and height.
6. Tap the **Calculate BMI** button.
7. View your calculated BMI value and health category.
8. Explore the semi-circular BMI gauge to understand your result visually.
9. Listen to your BMI result using Text-to-Speech.
10. Navigate to BMI History to review previous calculations and monitor your progress.
11. Log out when finished.

---

## 💡 Why This Project?

SmartBMI demonstrates my ability to design and develop a functional Android application using Kotlin and modern mobile development technologies.

Through this project, I gained practical experience in:

- Android application development.
- Firebase Authentication and social sign-in integration.
- Local and cloud database management.
- BMI calculation and data processing.
- Interactive data visualisation.
- Android user interface design.
- Input validation and authentication handling.
- Accessibility through Text-to-Speech.
- Git and GitHub version control.

The project reflects my interest in developing practical, user-friendly mobile applications that combine functionality, accessibility, and modern technology.

---

## 📜 License

This project is licensed under the MIT License.

Feel free to use and modify it for your own purposes.

---

## 👩‍💻 Developer

**Rapolai Kate**

Junior Android Developer

GitHub: [@RapolaiKate](https://github.com/RapolaiKate)