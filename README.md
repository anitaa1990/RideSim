# RideSim
RideSim is a ride-hailing simulation app where the user can select a pickup and drop location, see the route on a map, estimate the fare, and simulate a driver arriving and completing a ride. It’s focused on showcasing Jetpack Compose + Maps + trip flow logic.

## ✅ User Stories

* As a user, I want to open the app and see my current location on a map, so I know where I am.
* I want to be able to select pickup (if location is not enabled) and drop-off locations by typing my address (or from suggestions).
* I want to see the drawn route, all other details (fare, distance, car types etc), so I know what to expect before requesting a ride.
* I want to press a “Request Ride” button, so the app can simulate a driver accepting and approaching me.
* I want to visually see a car icon move along the route, so it feels like a real-time trip is in progress.
* I want to see a final summary (fare, distance, time) when the trip ends, so I can review the trip.

## 🔧 Tech Stack

* Jetpack Compose – For building our UI.
* Google Maps Compose – To display interactive maps, user location, and route polyline using Compose
* MVVM Architecture – Clean separation of concerns: UI ↔ ViewModel ↔ Business Logic
* Kotlin Coroutines – For async operations like location fetch & trip simulation
* Kotlin Flow – Reactive state updates for trip progress, fare updates and UI state.
* Firebase/Places API – To enable real-time search suggestions and address-to-lat/lng conversion
* Google Directions API – For drawing route between pickup & drop and estimating distance & duration
* Hilt (optional) – For dependency injection and easier testability of ViewModels & utils
* Location Services (Fused) – To get accurate current location from the device
* Maps Utils (PolyUtil) – To interpolate & animate car marker along the polyline
* JUnit – Unit testing for business logic (fare calculation, state transitions)
* Mockk / Mockito – Mock dependencies like location providers or ViewModel observers.

## 📋 Implementation Plan
#### M1: Business Logic Implementation
> Set up the core business logic, models, utility classes, and ViewModel with full unit test coverage.

- [ ] Define `LocationUtils` class: A utility class that provides geographical calculations between two points using latitude and longitude. Add unit tests.
- [ ] Define `FareCalculator` class: Calculates total trip fare based on distance, duration, and selected ride type. Add unit tests.
- [ ] Define `RouteUtils` class: Responsible for retrieving route information between pickup and drop locations via Google Directions API (or mocked during development). Add unit tests.
- [ ] Define `PlaceSearchUtil` class: Wraps calls to the Google Places API to provide location suggestions as user types, and resolve place IDs to coordinates. Add unit tests.
- [ ] Define `RideViewModel` class + add unit tests:
  - [ ] Handle pickup/drop input fields.
  - [ ] Fetch suggestions using `PlaceSearchUtil`.
  - [ ] Converts selected suggestion → `LocationData`.
  - [ ] Fetch route once both locations are selected.
  - [ ] Calculate fare for all ride types.
  - [ ] Manage selected ride type (default: Auto).
  - [ ] Tracks trip states.
  - [ ] Reset state on ride completion.

#### M2: Pre-book: UI + Map Integration
> Build a complete pre-booking flow that covers input fields, map display, suggestions, and fare estimate UI.

- [ ] UI: Create a HomeScreen composable.
- [ ] UI: Add TopAppBar with RideSim logo + title.
- [ ] UI: Integrate GoogleMap composable (Maps Compose).
- [ ] UI: Add pickup/drop location TextField section.
- [ ] Request runtime permission: request location permission from user when the app is first opened.
- [ ] If location is available, use `FusedLocationProvider` to fetch last known location and update UI.
- [ ] UI: Show user’s current location with blue dot / marker (if available).
- [ ] UI: Add pickup & drop section.
- [ ] UI: Add an autocomplete dropdown UI, if user starts typing pickup/drop address.
- [ ] Update map with pickup & drop location, once it is available.
- [ ] UI: Display bottom sheet with trip details, including distance between two locations, time taken, type of vechiles available, along with it's price.


#### M3: Ride Simulation
> This milestone brings the ride to life. Once the user clicks “Book Ride,” the app simulates a full trip flow — from a driver approaching the pickup location to the trip ending and displaying a summary.

- [ ] Update ViewModel to simulate driver arriving, trip getting started and trip completion state.
- [ ] Show the moving driver marker on the map based on carPosition.
- [ ] UI: Animate the car icon along the polyline (driver → pickup and then pickup → drop).
- [ ] Rotate car icon to match the direction of movement (bearing between points).
- [ ] Center or follow the moving car on the map using camera position updates.

#### M4: Post-Ride – Trip Summary & Reset Flow
> After the ride ends, show a trip summary screen with fare, distance, time, and ride type. Provide an option to start a new ride by resetting the app’s state.

- [ ] Create a new TripSummaryScreen composable.
- [ ] Display:
  - [ ] Total fare (with ₹ or $ prefix).
  - [ ] Total distance and time.
  - [ ] Selected ride type (icon + name).
  - [ ] Pickup & drop address (optional).

