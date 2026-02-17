package com.dasurv.data.repository

import com.dasurv.data.local.dao.ClientPigmentPreferenceDao
import com.dasurv.data.local.entity.ClientPigmentPreference
import com.dasurv.data.model.Pigment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientPigmentPreferenceRepository @Inject constructor(
    private val dao: ClientPigmentPreferenceDao
) {
    fun getPreferencesForClient(clientId: Long): Flow<List<ClientPigmentPreference>> =
        dao.getPreferencesForClient(clientId)

    /**
     * Toggles a pigment as favorite for a client.
     * @return true if the pigment is now a favorite, false if it was removed.
     */
    suspend fun togglePreference(clientId: Long, pigment: Pigment): Boolean {
        val existing = dao.getPreference(clientId, pigment.name, pigment.brand.displayName)
        return if (existing != null) {
            dao.deletePreference(existing)
            false
        } else {
            dao.insertPreference(
                ClientPigmentPreference(
                    clientId = clientId,
                    pigmentName = pigment.name,
                    pigmentBrand = pigment.brand.displayName,
                    pigmentColorHex = pigment.colorHex
                )
            )
            true
        }
    }
}
