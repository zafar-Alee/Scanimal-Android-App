package com.kingree.scanimal.Model

data class AnimalRecord(
    val animalId: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val name: String = "",
    val species: String = "",
    val age: String = "",
    val color: String = "",
    val frontImageUrl: String = "",   // Firebase Storage download URL
    val sideImageUrl: String = "",
    val noseImageUrl: String = "",
    val registeredAt: Long = System.currentTimeMillis(),
    val status: String = "Pending"
)
