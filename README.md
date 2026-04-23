# Restaurant Reservation System

Desktop GUI application for managing restaurant reservations, built with Java Swing.

## Project Overview

This application helps restaurant staff manage reservations quickly and efficiently through a graphical user interface.

Users can add, edit, delete, search, and filter reservations while the system automatically checks seating capacity.

## Main Features

* Add new reservations
* Edit selected reservations
* Delete reservations
* Search by customer name
* Filter by reservation type
* Filter by date
* Save reservations to file
* Load saved reservations automatically
* Real-time occupancy tracking

## Reservation Areas

### Indoor Reservations

* Table number
* Seating type:

  * Standard Table
  * Booth
  * Long Table

### Outdoor Reservations

* Garden
* Terrace
* Smoking section
* Non-smoking section

## Capacity Control

* Total restaurant capacity: **200 seats**
* Indoor hall: **120 seats**
* Outdoor area: **80 seats**

The system prevents overbooking automatically.

## Technologies Used

* Java
* Java Swing
* Object-Oriented Programming
* File Handling
* IntelliJ IDEA

## How to Run

Compile:

```bash
javac src/ReservationDeskGUI.java
```

Run:

```bash
java -cp src ReservationDeskGUI
```

## Project Structure

```text
restaurant-reservation-system/
│── src/
│   └── ReservationDeskGUI.java
│── README.md
│── .gitignore
│── screenshots/
```

## Screenshots

Add screenshots of the running application inside the `screenshots` folder.

## Author

Created Aleksandra Kolarova, student from Technical University Sofia, Plovdiv branch
