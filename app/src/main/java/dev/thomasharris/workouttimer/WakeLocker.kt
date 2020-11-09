package dev.thomasharris.workouttimer

import android.content.Context
import android.os.PowerManager

interface WakeLocker {
    fun lock() {}

    fun unlock() {}
}

// that said, my oneplus 6 still kills it near the end of 3 30s sets
class AndroidWakeLocker(
    context: Context,
) : WakeLocker {

    private var wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WorkoutTimer::WakeLockTag")

    override fun lock() {
        if (!wakeLock.isHeld)
            wakeLock.acquire(20 * 60 * 1000L /*20 minutes*/)
    }

    override fun unlock() {
        if (wakeLock.isHeld)
            wakeLock.release()
    }
}