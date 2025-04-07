# RideSim
RideSim is an MVP (Minimum Viable Product) that aims to simulate the core experience of booking a ride through an app like Uber or Ola. Users can enter pickup and drop locations, select a vehicle type, and watch the simulation play out in real-time on a map. This simulation includes route calculation, animated driver movement, estimated fare, and dynamic UI updates. 

The aim of this repository is to showcase how to work with Maps in Jetpack Compose. The designs are inspired by the [Namma Yatri app](https://play.google.com/store/apps/details?id=in.juspay.nammayatri&hl=en_IN).

## App Screenshots

<img src="https://github.com/user-attachments/assets/fb21bf8e-f55d-4794-9651-fea18b4249f0" width="300"/>  <img src="https://github.com/user-attachments/assets/1c9c729f-c3d7-40e9-96b1-603d5016c156" width="300"/> <img src="https://github.com/user-attachments/assets/520536dd-f0ff-4427-9738-859f2296d19d" width="300"/>  <img src="https://github.com/user-attachments/assets/518631f4-4e54-4329-af8d-e00b1ca09698" width="300"/>  <img src="https://github.com/user-attachments/assets/4518956b-7dd4-48b9-a090-a7a4273da6d0" width="300"/> <img src="https://github.com/user-attachments/assets/71a12906-2b2c-4054-84b4-87346b188a0e" width="300"/> <img src="https://github.com/user-attachments/assets/62272ee8-87fb-48c5-911c-bbce7c9ed087" width="300"/> <img src="https://github.com/user-attachments/assets/678ecb41-4d2d-4eff-b847-1f3b3d8406e2" width="300"/> <img src="https://github.com/user-attachments/assets/16d2cd8b-a0c9-4167-b5f8-c31aef2b119f" width="300"/> 

## ✅ User Stories
I split the project into multiple user stores to help plan the architecture and implementation details. As a user:
* I want to open the app and see my current location on a map, so I know where I am.
* I want to be able to select pickup (if location is not enabled) and drop-off locations by typing my address (or from suggestions).
* I want to see the drawn route, all other details (fare, distance, car types etc), so I know what to expect before requesting a ride.
* I want to press a “Request Ride” button, so the app can simulate a driver accepting and approaching me.
* I want to visually see a car icon move along the route, so it feels like a real-time trip is in progress.
* I want to see a final summary (fare, distance, time) when the trip ends, so I can review the trip.

<img src="https://github.com/user-attachments/assets/45f3313a-e277-4415-b14e-65e2a7930b1b" width="400"/>


## 🔧 Tech Stack

* Jetpack Compose – For building our UI.
* Google Maps Compose – To display interactive maps, user location, and route polyline using Compose
* MVVM Architecture – Clean separation of concerns: UI ↔ ViewModel ↔ Business Logic
* Kotlin Coroutines – For async operations like location fetch & trip simulation
* Kotlin Flow – Reactive state updates for trip progress, fare updates and UI state.
* Firebase/Places API – To enable real-time search suggestions and address-to-lat/lng conversion
* Google Directions API – For drawing route between pickup & drop and estimating distance & duration
* Hilt – For dependency injection and easier testability of ViewModels & utils
* Location Services (Fused) – To get accurate current location from the device
* JUnit – Unit testing for business logic (fare calculation, state transitions)
* Mockk / Mockito – Mock dependencies like location providers or ViewModel observers.


## 📋 Implementation Plan
* [M1: Business Logic Implementation](https://github.com/anitaa1990/RideSim/issues/2): Set up the core business logic, models, utility classes, and ViewModel with full unit test coverage.
* [M2: Pre-book: UI + Map Integration](https://github.com/anitaa1990/RideSim/issues/3): Build a complete pre-booking flow that covers input fields, map display, suggestions, and fare estimate UI.
* [M3: Ride Simulation](https://github.com/anitaa1990/RideSim/issues/4): This milestone brings the ride to life. Once the user clicks “Book Ride,” the app simulates a full trip flow — from a driver approaching the pickup location to the trip ending and displaying a summary.
* [M4: Post-Ride – Trip Summary & Reset Flow](https://github.com/anitaa1990/RideSim/issues/1): After the ride ends, show a trip summary screen with fare, distance, time, and ride type. Provide an option to start a new ride by resetting the app’s state.

## Videos
<img src="https://github.com/user-attachments/assets/52488a99-e17f-4181-b490-648b15d232d1" width="300"/> <img src="https://github.com/user-attachments/assets/db6f3244-b78d-41be-a718-abb869d21f1a" width="300"/> <img src="https://github.com/user-attachments/assets/5d5881ae-b5a6-47c9-8d38-96cca2f8ff31" width="300"/>  <img src="https://github.com/user-attachments/assets/29a42528-a7cd-4d07-8328-c15cc3d00863" width="300"/> 

