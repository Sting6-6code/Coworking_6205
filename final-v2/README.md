# Coworking Space Management System

## Project Overview

A comprehensive desktop application for managing coworking spaces, built with JavaFX. The system provides separate interfaces for administrators and regular users to manage spaces, bookings, transactions, and analytics.

## Version Information

- **Java Version**: Java SE 21
- **JavaFX Version**: 23.0.1
- **Project Version**: 2.0

## Features

### User Features
- User registration and authentication
- Space browsing with interactive map view
- Space filtering by type, capacity, floor, and availability
- Time slot selection and booking
- Transaction history and billing overview
- Question submission and FAQ access

### Admin Features
- User management
- Space management (add, modify, view spaces)
- Booking management and monitoring
- Transaction and billing management
- Analytics dashboard with revenue and booking statistics
- Question management and responses

## Project Structure

```
final-v2/
├── src/
│   ├── adt/              # Abstract Data Type interfaces
│   ├── application/      # Main application controllers and FXML files
│   ├── controller/       # Specialized controllers
│   ├── datastructure/    # Data structure implementations (BST, HashTable, QuickSort)
│   ├── model/            # Domain models (User, Space, Booking, Transaction, etc.)
│   ├── service/          # Business logic services
│   └── util/             # Utility classes for data management
├── resources/            # Resources (icons, maps, CSS)
├── data/                # CSV data files
└── bin/                 # Compiled classes
```

## Data Structures

- **TransactionBST**: Binary Search Tree for efficient transaction date-based queries
- **ChainingHashTable**: Hash table with chaining collision resolution for fast lookups
- **QuickSort**: Efficient sorting algorithm for space and booking lists
- **ResizableArrayBag**: Dynamic bag implementation
- **SpaceIndexDS**: Indexed data structure for space filtering and retrieval

## Prerequisites

- Java Development Kit (JDK) 21 or higher
- JavaFX SDK 23.0.1 or compatible version
- Eclipse IDE (recommended) or any Java IDE

## Setup Instructions

1. **Clone or extract the project** to your local machine

2. **Configure JavaFX Library**:
   - In Eclipse: Right-click project → Properties → Java Build Path → Libraries
   - Add JavaFX library to the classpath
   - Ensure JavaFX modules are properly configured

3. **Verify Data Files**:
   - Ensure `data/` directory contains required CSV files:
     - `data.csv` (user data)
     - `spaces.csv` (space information)
     - `bookings.csv` (booking records)
     - `transactions.csv` (transaction records)
     - `question.csv` (FAQ/questions)

4. **Run the Application**:
   - Main entry point: `application.Main` or `application.Launcher`
   - Run configuration should include JavaFX module path

## Running the Application

### Using Eclipse:
1. Right-click on `src/application/Launcher.java`
2. Select "Run As" → "Java Application"

### Using Command Line:
```bash
# Compile (ensure JavaFX is in classpath)
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -d bin src/**/*.java

# Run
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp bin application.Launcher
```

## Default Login Credentials

The system uses CSV-based authentication. Check `data/data.csv` for existing user credentials. Format:
```
userId,username,password,email,type,membership
```

## Usage Guide

### For Users:
1. **Login/Register**: Start with login screen, or register a new account
2. **Browse Spaces**: Navigate to Spaces section, use map view or building overview
3. **Filter Spaces**: Apply filters for type, capacity, floor, and availability
4. **Make Booking**: Select a space, choose time slots, and confirm booking
5. **View Transactions**: Check billing section for transaction history

### For Administrators:
1. **Login**: Use admin credentials
2. **Manage Users**: View and manage user accounts in Users section
3. **Manage Spaces**: Add, modify, or view spaces in Spaces section
4. **Monitor Bookings**: View all bookings in Bookings section
5. **Analytics**: Access revenue and booking statistics in Analytics section
6. **Handle Questions**: Respond to user questions in Questions section

## Data Storage

All data is stored in CSV format in the `data/` directory:
- User data: `data/data.csv`
- Space data: `data/spaces.csv`
- Booking data: `data/bookings.csv`
- Transaction data: `data/transactions.csv`
- Question data: `data/question.csv`

## Technical Notes

- The application uses a fixed window size (1250x800 pixels)
- Map view requires internet connection for OpenStreetMap tiles
- All data operations are file-based (CSV), no database required
- Transaction history uses BST for efficient date range queries
- Space filtering uses indexed data structures for performance

## Troubleshooting

- **JavaFX not found**: Ensure JavaFX SDK is properly configured in your IDE
- **FXML loading errors**: Verify that FXML files are in the correct package structure
- **Data not loading**: Check that CSV files exist in `data/` directory with correct format
- **Map not displaying**: Ensure internet connection for OpenStreetMap tiles

## License

This project is part of a course assignment (6205).

