# Quit Smoking App 🚭

A comprehensive Android application built with Jetpack Compose to help users quit smoking through tracking, community support, and personalized assistance.

## 📱 Features

### 🏠 **Home Dashboard**
- **Progress Tracking**: View smoke-free days, current streak, and money saved
- **Quick Actions**: Access morning/night check-ins and weekly goal setting
- **Motivational Display**: Real-time statistics and achievements

### 📊 **Progress & Statistics**
- **Calendar View**: Visual representation of smoke-free days
- **Milestone Goals**: Set and track weekly/monthly smoke-free targets
- **Financial Tracking**: Monitor money saved by not purchasing cigarettes
- **Purchase Logging**: Track cigarette purchases and calculate savings
- **Data Visualization**: Charts and graphs showing progress over time

### 🧘 **Craving Management**
- **Urge Relief Tools**: Immediate help when cravings strike
- **Craving Tips**: Interactive cards with practical advice
- **Mindfulness Videos**: Guided meditation and relaxation content
- **Withdrawal Relief**: Comprehensive tips for managing withdrawal symptoms
- **Emergency Contacts**: Quick access to support when needed

### 🤖 **AI-Powered Support**
- **GPT Chat**: Personalized AI assistant for quitting advice
- **Community Chat**: Connect with other users on their quitting journey
- **Leaderboard**: Friendly competition and motivation through rankings

### ⚙️ **Personalization**
- **Morning Check-ins**: Start your day with intention and goal setting
- **Night Check-ins**: Reflect on daily progress and challenges
- **Weekly Goal Builder**: Create and modify personalized quitting goals
- **Settings**: Customize your experience and preferences

## 🛠️ **Technology Stack**

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM with Repository pattern
- **Backend**: Firebase (Authentication, Firestore, Storage, Analytics)
- **Navigation**: Navigation Compose
- **Media**: ExoPlayer for video content
- **Networking**: Retrofit for API calls
- **Data Storage**: DataStore Preferences + Firebase Firestore

## 📋 **Requirements**

- **Android SDK**: 23+ (Android 6.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 2.0.0
- **Gradle**: 8.12.2
- **Java**: 11

## 🚀 **Getting Started**

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 23+
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/quit-smoking-app.git
   cd quit-smoking-app
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Enable Authentication, Firestore, Storage, and Analytics
   - Download `google-services.json` and place it in `app/` directory
   - Configure authentication methods (Email/Password recommended)

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and run the app.

## 📁 **Project Structure**

```
app/src/main/java/com/example/quitesmoking/
├── auth/                    # Authentication screens
│   ├── LoginScreen.kt
│   └── RegisterScreen.kt
├── chat/                    # Community and AI chat features
│   ├── ChatScreen.kt
│   ├── CommunityScreen.kt
│   ├── GPTChatScreen.kt
│   └── LeaderboardScreen.kt
├── model/                   # Data models
│   ├── DailyLog.kt
│   ├── DailySmokingStatus.kt
│   └── MilestoneGoal.kt
├── navigation/              # Navigation configuration
│   └── Navigation.kt
├── repo/                    # Repository classes
│   └── DailyLogRepository.kt
├── ui/                      # UI screens and components
│   ├── HomeScreen.kt
│   ├── MorningCheckInScreen.kt
│   ├── NightCheckInScreen.kt
│   ├── SettingsScreen.kt
│   ├── WeeklyGoalBuilderScreen.kt
│   └── LogPurchaseScreen.kt
├── urge/                    # Craving management features
│   ├── CravingTipsScreen.kt
│   ├── MindfulnessVideoPlayerScreen.kt
│   ├── UrgeTabScreen.kt
│   └── WithdrawalReliefTipsScreen.kt
├── MainActivity.kt          # Main activity and navigation
├── StatsScreen.kt          # Progress and statistics
└── BottomNav.kt            # Bottom navigation component
```

## 🔧 **Configuration**

### Firebase Configuration
The app requires Firebase services to be properly configured:

1. **Authentication**: Enable Email/Password authentication
2. **Firestore**: Set up security rules for user data
3. **Storage**: Configure for media file uploads
4. **Analytics**: Enable for usage tracking

### Environment Variables
No additional environment variables are required for basic functionality.

## 📱 **Screenshots**

*Screenshots would be added here showing the main features of the app*

## 🤝 **Contributing**

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 **Acknowledgments**

- Material 3 Design System
- Firebase for backend services
- Jetpack Compose team
- Android development community

## 📞 **Support**

For support, email support@quitsmokingapp.com or create an issue in this repository.

---

**Note**: This app is designed to support smoking cessation efforts but should not replace professional medical advice. Users experiencing severe withdrawal symptoms should consult healthcare professionals.
