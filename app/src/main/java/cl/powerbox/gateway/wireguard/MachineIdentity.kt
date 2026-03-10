package cl.powerbox.gateway.wireguard

import android.content.Context
import android.provider.Settings
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.MachineConfig
import cl.powerbox.gateway.util.Logger

/**
 * Identidad de la máquina vending.
 * Lee machine_number y machine_nickname desde la tabla machine_config.
 * En primera instalación, genera valores por defecto usando ANDROID_ID.
 */
object MachineIdentity {

    private const val KEY_MACHINE_NUMBER   = "machine_number"
    private const val KEY_MACHINE_NICKNAME = "machine_nickname"
    private const val KEY_ANDROID_ID       = "android_id"

    data class Identity(
        val machineNumber: String,
        val nickname: String,
        val androidId: String
    )

    suspend fun get(ctx: Context): Identity {
        val db    = AppDatabase.get(ctx)
        val dao   = db.machineConfigDao()
        val now   = System.currentTimeMillis()

        val androidId = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        var number   = dao.getValue(KEY_MACHINE_NUMBER)
        var nickname = dao.getValue(KEY_MACHINE_NICKNAME)

        // Primera instalación: asignar valores por defecto
        if (number == null) {
            number = "VM-${androidId.takeLast(4).uppercase()}"
            dao.upsertAll(listOf(MachineConfig(KEY_MACHINE_NUMBER, number, now)))
            Logger.i("[WG] Primera instalación → machine_number=$number (editar desde UI)")
        }

        if (nickname == null) {
            nickname = "Máquina $number"
            dao.upsertAll(listOf(MachineConfig(KEY_MACHINE_NICKNAME, nickname, now)))
        }

        // Guardar android_id por referencia
        if (dao.getValue(KEY_ANDROID_ID) == null) {
            dao.upsertAll(listOf(MachineConfig(KEY_ANDROID_ID, androidId, now)))
        }

        return Identity(
            machineNumber = number,
            nickname      = nickname,
            androidId     = androidId
        )
    }

    /** Permite actualizar el número y nickname desde la UI o via API */
    suspend fun update(ctx: Context, machineNumber: String, nickname: String) {
        val dao = AppDatabase.get(ctx).machineConfigDao()
        val now = System.currentTimeMillis()
        dao.upsertAll(listOf(
            MachineConfig(KEY_MACHINE_NUMBER,   machineNumber, now),
            MachineConfig(KEY_MACHINE_NICKNAME, nickname,      now)
        ))
        Logger.i("[WG] Identidad actualizada → #$machineNumber | $nickname")
    }
}
