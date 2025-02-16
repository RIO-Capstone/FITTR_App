package com.example.fittr_app.types

import java.io.Serializable


enum class Exercise(val exerciseName:String) : Serializable {
    SQUATS("SQUATS"),
    RIGHT_BICEP_CURLS("RIGHT_BICEP_CURLS"),
    LEFT_BICEP_CURLS("LEFT_BICEP_CURLS"),
    CABLE_TRICEP_EXTENSION("CABLE_TRICEP_EXTENSION"),
    UNKNOWN("UNKNOWN") // A default or unknown exercise
}