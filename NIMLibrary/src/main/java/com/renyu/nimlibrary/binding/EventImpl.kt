package com.renyu.nimlibrary.binding

import android.view.View

interface EventImpl {
    fun click(view: View) {}

    fun deleteRecentContact(view: View, contactId: String) {}
}