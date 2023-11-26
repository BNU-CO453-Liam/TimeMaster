package com.timemaster.model

import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
//import java.util.logging.Handler

class Task(val name: String) : Parcelable {
    var id: Int = 0
    var isRunning: Boolean = false
    var startTime: Long = 0
    var endTime: Long = 0
    var duration: Long = 0
    var handler: Handler? = null
    var timerRunnable: Runnable? = null

    constructor(parcel: Parcel) : this(parcel.readString()!!) {
        id = parcel.readInt()
        isRunning = parcel.readByte() != 0.toByte()
        duration = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(id)
        parcel.writeByte(if (isRunning) 1 else 0)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}
