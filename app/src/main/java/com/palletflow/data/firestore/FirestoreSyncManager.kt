package com.palletflow.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.palletflow.data.local.entity.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val ITEMS = "items"
        const val WAREHOUSES = "warehouses"
        const val SUPPLIERS = "suppliers"
        const val LOTS = "lots"
        const val LOT_ITEMS = "lot_items"
        const val LOT_EXPENSES = "lot_expenses"
        const val TRANSACTIONS = "transactions"
        const val STOCKS = "stocks"
        const val SALE_ITEMS = "sale_items"
        const val SALE_EXPENSES = "sale_expenses"
    }

    private val TAG = "FirestoreSyncManager"
    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    // Sync a single object to its collection
    suspend fun <T : Any> syncToCloud(collectionName: String, id: String, data: T): Result<Unit> {
        return try {
            firestore.collection(collectionName)
                .document(id)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to cloud: $collectionName/$id", e)
            Result.failure(e)
        }
    }

    // Delete a single object from its collection
    suspend fun deleteFromCloud(collectionName: String, id: String): Result<Unit> {
        return try {
            firestore.collection(collectionName)
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting from cloud: $collectionName/$id", e)
            Result.failure(e)
        }
    }

    // Fetch all data for a user to restore
    suspend fun fetchAllUserData(userId: String): Result<FirestoreBackupData> {
        return try {
            val items = fetchCollection<Item>("items", userId)
            val warehouses = fetchCollection<Warehouse>("warehouses", userId)
            val suppliers = fetchCollection<Supplier>("suppliers", userId)
            val lots = fetchCollection<Lot>("lots", userId)
            val lotItems = fetchCollection<LotItem>("lot_items", userId)
            val lotExpenses = fetchCollection<LotExpense>("lot_expenses", userId)
            val transactions = fetchCollection<Transaction>("transactions", userId)
            val stocks = fetchCollection<Stock>("stocks", userId)
            val saleItems = fetchCollection<SaleItem>("sale_items", userId)
            val saleExpenses = fetchCollection<SaleExpense>("sale_expenses", userId)

            Result.success(
                FirestoreBackupData(
                    items = items,
                    warehouses = warehouses,
                    suppliers = suppliers,
                    lots = lots,
                    lotItems = lotItems,
                    lotExpenses = lotExpenses,
                    transactions = transactions,
                    stocks = stocks,
                    saleItems = saleItems,
                    saleExpenses = saleExpenses
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all user data", e)
            Result.failure(e)
        }
    }

    private suspend inline fun <reified T : Any> fetchCollection(collectionName: String, userId: String): List<T> {
        return firestore.collection(collectionName)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(T::class.java)
    }

    fun <T : Any> observeCollection(collectionName: String, userId: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
        val registration = firestore.collection(collectionName)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing $collectionName", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(clazz))
                }
            }
        
        awaitClose { registration.remove() }
    }
}

data class FirestoreBackupData(
    val items: List<Item>,
    val warehouses: List<Warehouse>,
    val suppliers: List<Supplier>,
    val lots: List<Lot>,
    val lotItems: List<LotItem>,
    val lotExpenses: List<LotExpense>,
    val transactions: List<Transaction>,
    val stocks: List<Stock>,
    val saleItems: List<SaleItem>,
    val saleExpenses: List<SaleExpense>
)
