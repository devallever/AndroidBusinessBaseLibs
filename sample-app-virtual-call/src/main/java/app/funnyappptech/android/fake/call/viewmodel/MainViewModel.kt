package app.funnyappptech.android.fake.call.viewmodel

import android.content.Intent
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.funnyappptech.android.fake.call.base.BaseViewModel

class MainViewModel : BaseViewModel() {
    val nameLiveData by lazy { MutableLiveData<String>() }
    val phoneLiveData by lazy { MutableLiveData<String>() }

    fun getContacts(data: Intent) {
        val contactUri = data.data ?: return
        var phoneNumber = ""
        var name = ""
        val cursor = App.context.contentResolver.query(contactUri, null, null, null, null);
        if (cursor?.moveToFirst() == true) {
            try {
                name = cursor
                    .getString(
                        cursor
                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    )
                nameLiveData.postValue(name)
                var hasPhone = cursor
                    .getString(
                        cursor
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                    )
                val id = cursor.getString(
                    cursor
                        .getColumnIndex(ContactsContract.Contacts._ID)
                )
                hasPhone = if (hasPhone == "1") {
                    "true"
                } else {
                    "false"
                }
                if (hasPhone.toBoolean()) {
                    val phones = App.context.contentResolver
                        .query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = " + id, null, null
                        )
                    if (phones?.moveToNext() == true) {
                        phoneNumber = phones
                            .getString(
                                phones
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                        log("$name : $phoneNumber")
                        phoneLiveData.postValue(phoneNumber)
                    }
                    phones?.close()
                }
                cursor.close()
            } catch (e: Exception) {
                e.printStackTrace()
                toast("请选择合法的联系人")
            } finally {
                cursor.close()
            }
        }
    }

}