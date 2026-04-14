package com.kingree.scanimal.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kingree.scanimal.Model.AnimalRecord
import kotlinx.coroutines.tasks.await

object AnimalRepository {
	private const val COLLECTION_ANIMALS = "animals"

	private val firestore by lazy { FirebaseFirestore.getInstance() }
	private val storage by lazy { FirebaseStorage.getInstance() }

	suspend fun saveAnimal(
		record: AnimalRecord,
		frontUri: Uri,
		onProgress: (String) -> Unit = {}
	): Result<AnimalRecord> = runCatching {
		onProgress("Uploading front photo...")
		// Do not fail registration if image upload is denied or temporarily unavailable.
		val frontUrl = runCatching {
			uploadAnimalImage(record.animalId, "front", frontUri)
		}.getOrElse {
			onProgress("Could not upload photo now. Saving record...")
			""
		}

		onProgress("Saving animal record...")
		val savedRecord = record.copy(
			frontImageUrl = frontUrl,
			sideImageUrl = "",
			noseImageUrl = ""
		)

		firestore.collection(COLLECTION_ANIMALS)
			.document(savedRecord.animalId)
			.set(savedRecord)
			.await()

		savedRecord
	}

	suspend fun getAnimalsForUser(uid: String): Result<List<AnimalRecord>> = runCatching {
		val snapshot = firestore.collection(COLLECTION_ANIMALS)
			.whereEqualTo("ownerUid", uid)
			.get()
			.await()

		snapshot.documents.mapNotNull { doc ->
			val data = doc.data ?: return@mapNotNull null
			AnimalRecord(
				animalId = data["animalId"] as? String ?: doc.id,
				ownerUid = data["ownerUid"] as? String ?: uid,
				ownerName = data["ownerName"] as? String ?: "",
				name = data["name"] as? String ?: "",
				species = data["species"] as? String ?: "",
				age = data["age"] as? String ?: "",
				color = data["color"] as? String ?: "",
				frontImageUrl = data["frontImageUrl"] as? String ?: "",
				sideImageUrl = data["sideImageUrl"] as? String ?: "",
				noseImageUrl = data["noseImageUrl"] as? String ?: "",
				registeredAt = (data["registeredAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
				status = data["status"] as? String ?: "Pending"
			)
		}.sortedByDescending { it.registeredAt }
	}

	suspend fun updateAnimalDetails(
		animalId: String,
		name: String,
		species: String,
		age: String,
		color: String
	): Result<Unit> = runCatching {
		firestore.collection(COLLECTION_ANIMALS)
			.document(animalId)
			.update(
				mapOf(
					"name" to name,
					"species" to species,
					"age" to age,
					"color" to color
				)
			)
			.await()
	}

	suspend fun deleteAnimal(animalId: String): Result<Unit> = runCatching {
		firestore.collection(COLLECTION_ANIMALS)
			.document(animalId)
			.delete()
			.await()

		// Best effort cleanup for the stored front image.
		runCatching {
			storage.reference
				.child("animals")
				.child(animalId)
				.child("front.jpg")
				.delete()
				.await()
		}
	}

	private suspend fun uploadAnimalImage(
		animalId: String,
		imageName: String,
		uri: Uri
	): String {
		val ref = storage.reference
			.child("animals")
			.child(animalId)
			.child("$imageName.jpg")

		ref.putFile(uri).await()
		return ref.downloadUrl.await().toString()
	}
}

