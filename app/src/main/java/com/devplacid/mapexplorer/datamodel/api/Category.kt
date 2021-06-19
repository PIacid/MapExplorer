package com.devplacid.mapexplorer.datamodel.api

import java.io.Serializable

enum class Category(val apiName: String, val displayedName: String): Serializable {
    BOOKMARKS("bookmarks", "Bookmarks"),
    HOTEL("hotel", "Hotel"),
    HOSTEL("hostel", "Hostel"),
    SUPERMARKET("supermarket", "Supermarket"),
    GAS_STATION("gas_station", "Gas station"),
    RESTAURANT("restaurant", "Restaurant"),
    BAR("bar", "Bar"),
    MUSEUM("museum", "Museum"),
    DENTIST("dentist", "Dentist"),
    HOSPITAL("hospital", "Hospital"),
    PHARMACY("pharmacy", "Pharmacy"),
    PARKING("parking", "Parking"),
    BANK("bank", "Bank"),
    POLICE("police", "Police"),
    AIRPORT("airport", "Airport")
}