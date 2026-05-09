

```markdown
# 📦 Storeroom Checklist Pro

A complete inventory management system for storerooms, warehouses, and donation centers with real-time tracking, history logging, and advanced analytics.

![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-purple)
![Firebase](https://img.shields.io/badge/firebase-latest-orange)
![API](https://img.shields.io/badge/API-21%2B-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## 📱 Screenshots

| Main Screen | Add Item Dialog | Statistics Dashboard | History View |
|-------------|----------------|---------------------|--------------|
| [Screenshot 1] | [Screenshot 2] | [Screenshot 3] | [Screenshot 4] |

## ✨ Features

### Core Functionality
- ✅ **Complete CRUD Operations** - Create, Read, Update, Delete inventory items
- ✅ **7 Data Fields** - Date, Crate Number, Category, Item Name, Quantity, Location, Notes
- ✅ **Real-time Sync** - All changes sync instantly to Firebase cloud
- ✅ **Card View Layout** - Modern, clean presentation of inventory items

### Search & Filter
- 🔍 **Smart Search** - Search across item names, crate numbers, and notes
- 🏷️ **Category Filter** - Filter by 24+ pre-defined categories
- 📍 **Location Filter** - Filter by storage location (Row A-F)
- 🎯 **Combined Filters** - Use multiple filters simultaneously
- 🔄 **Clear Filters** - One-click reset of all filters

### Statistics & Analytics
- 📊 **Total Crates Tracked** - Count of all inventory entries
- 📈 **Total Items in Stock** - Sum of all item quantities
- 🔢 **Unique Items Count** - Distinct product tracking
- 📦 **Unique Crates Count** - Distinct crate identification
- 📍 **Location Analytics** - Track items across storage areas
- 🏆 **Top Category Insights** - Identify most stocked categories
- ⚠️ **Low Stock Alerts** - Auto-detection of items below 10 units

### History Tracking
- 📜 **Complete Audit Log** - Records ALL changes (ADD/UPDATE/DELETE)
- 🔄 **Before/After Comparison** - See exactly what changed
- 🎯 **Action Filtering** - Filter history by action type
- 🗑️ **Clear History** - Option to clear old records
- ⏱️ **Timeline View** - Chronological change tracking

### Data Management
- ☁️ **Cloud Backup** - Automatic Firebase sync
- 🔁 **Real-time Updates** - Live data synchronization
- 💾 **Persistent Storage** - Dropdown lists saved to cloud
- 🔐 **Secure Access** - Firebase security rules
- 📱 **Multi-user Ready** - Changes sync across devices

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or later
- JDK 11 or later
- Android SDK API 21+
- Firebase account (free tier available)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/storeroom-checklist-pro.git
cd storeroom-checklist-pro
```

2. **Open project in Android Studio**
   - File → Open → Select project folder
   - Wait for Gradle sync to complete

3. **Setup Firebase**
   - Create a project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name: `com.dbrightsites.stockapp`
   - Download `google-services.json`
   - Place the file in the `app/` directory

4. **Enable Firebase services**
   - In Firebase Console, enable **Realtime Database**
   - Set database rules (see below)

5. **Build and run**
   - Connect an Android device or start an emulator
   - Click Run ▶️ in Android Studio

### Firebase Security Rules

Copy these rules in Firebase Console → Realtime Database → Rules:

```json
{
  "rules": {
    "storeroom_items": {
      ".read": true,
      ".write": true,
      ".indexOn": ["timestamp"]
    },
    "item_history": {
      ".read": true,
      ".write": true,
      ".indexOn": ["timestamp", "action"]
    },
    "dropdown_lists": {
      ".read": true,
      ".write": true
    }
  }
}
```

## 📁 Project Structure

```
app/
├── src/main/java/com/dbrightsites/stockapp/
│   ├── MainActivity.kt           # Main inventory screen
│   ├── HistoryActivity.kt        # Change history viewer
│   ├── StatisticsActivity.kt     # Statistics dashboard
│   ├── StoreroomAdapter.kt       # RecyclerView adapter
│   ├── HistoryAdapter.kt         # History list adapter
│   ├── models/
│   │   ├── StoreroomItem.kt      # Inventory data model
│   │   └── ItemHistory.kt        # History data model
│   └── utils/
│       └── DateFormatter.kt      # Date utilities
├── src/main/res/
│   ├── layout/                   # XML layout files
│   ├── drawable/                 # Icons and graphics
│   ├── values/                   # Colors, strings, themes
│   └── menu/                     # Menu configurations
└── build.gradle                  # Dependencies
```

## 🎯 Usage Guide

### Adding an Item
1. Tap the **+ FAB** (bottom right)
2. Fill in the item details:
   - Crate Number (select from dropdown or add new)
   - Crate Category (select from 24+ categories)
   - Item Name
   - Quantity
   - Location (Row A-F or custom)
   - Notes (optional)
3. Tap **Add Item**

### Editing an Item
1. Find the item in the main list
2. Tap the **Edit** button on the item card
3. Modify any fields
4. Tap **Update**

### Searching & Filtering
1. Use the **Search Bar** at the top to search by name/crate/notes
2. Tap **Select** next to Category Filter
3. Choose a category from the list
4. Tap **Select** next to Location Filter
5. Choose a location
6. Tap **Clear All Filters** to reset

### Viewing Statistics
1. Tap the **📊 FAB** (bottom center)
2. View comprehensive statistics:
   - Total crates and items
   - Unique items count
   - Top category
   - Low stock alerts

### Checking Change History
1. Tap the **📜 FAB** (bottom left)
2. Browse all recorded changes
3. Use filters to view specific action types
4. Click **Clear History** to delete old records

## 🛠️ Built With

- **[Kotlin](https://kotlinlang.org/)** - Primary programming language
- **[Firebase Realtime Database](https://firebase.google.com/products/realtime-database)** - Cloud database
- **[Material Design Components](https://material.io/components)** - UI components
- **[AndroidX](https://developer.android.com/jetpack/androidx)** - Support libraries
- **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** - Async operations

## 📊 Database Schema

### Storeroom Items Collection
```json
{
  "id": "auto-generated",
  "dateLastChecked": "MMM dd, yyyy HH:mm",
  "crateNumber": "CR01",
  "crateName": "Electronics",
  "itemName": "Resistors",
  "itemCount": 100,
  "location": "Row A",
  "notes": "Box 3",
  "timestamp": 1234567890
}
```

### History Collection
```json
{
  "id": "auto-generated",
  "itemId": "reference-to-item",
  "action": "UPDATED",
  "timestamp": 1234567890,
  "dateFormatted": "MMM dd, yyyy HH:mm:ss",
  "previousItemCount": 50,
  "newItemCount": 100,
  "changeSummary": "Updated: Resistors - quantity changed from 50 to 100"
}
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Version History

- **v1.0.0** (Current)
  - Initial release
  - Complete CRUD functionality
  - Search and filter system
  - Statistics dashboard
  - History tracking
  - Firebase integration
  - Material Design UI

## 🐛 Known Issues

- None currently. Report issues on GitHub Issues page.

## 🔮 Future Roadmap

- [ ] Barcode scanning for quick item lookup
- [ ] Export data to CSV/Excel
- [ ] Dark mode support
- [ ] Multi-language support
- [ ] PDF report generation
- [ ] User authentication and roles
- [ ] Push notifications for low stock
- [ ] Data backup to Google Drive
- [ ] Offline mode with sync
- [ ] Photo attachment for items

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


---

**Made with ❤️ for efficient inventory management**

