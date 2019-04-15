/**
 * Copyright Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

	private lateinit var username: String
	private var firebaseDatabase = FirebaseDatabase.getInstance()
	private var messagesDatabaseReference = firebaseDatabase.reference.child("messages")

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		username = ANONYMOUS

		// Initialize message ListView and its adapter
		val friendlyMessages = ArrayList<FriendlyMessage>()
		val mMessageAdapter = MessageAdapter(this, R.layout.item_message, friendlyMessages)
		messageListView.adapter = mMessageAdapter

		// Initialize progress bar
		progressBar.visibility = ProgressBar.INVISIBLE

		// ImagePickerButton shows an image picker to upload a image for a message
		photoPickerButton.setOnClickListener {
			// TODO: Fire an intent to show an image picker
		}

		// Enable Send button when there's text to send
		messageEditText.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

			override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
				sendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
			}

			override fun afterTextChanged(editable: Editable) {}
		})
		messageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))

		// Send button sends a message and clears the EditText
		sendButton.setOnClickListener {
			val friendlyMessage = FriendlyMessage(messageEditText.text.toString(), username, null)
			val exception = messagesDatabaseReference.push().setValue(friendlyMessage).exception
			if (exception != null) {
				Log.e(TAG, "sendButton", exception)
			}

			// Clear input box
			messageEditText.setText("")
		}
		val childEventListener = object : ChildEventListener {
			override fun onChildAdded(dataSnapshot: DataSnapshot, str: String?) {
				val friendlyMessage = dataSnapshot.getValue(FriendlyMessage::class.java)
				mMessageAdapter.add(friendlyMessage)
			}

			override fun onChildMoved(ataSnapshot: DataSnapshot, str: String?) {
			}

			override fun onChildChanged(dataSnapshot: DataSnapshot, str: String?) {
			}

			override fun onChildRemoved(dataSnapshot: DataSnapshot) {
			}

			override fun onCancelled(databaseError: DatabaseError) {
			}
		}
		messagesDatabaseReference.addChildEventListener(childEventListener)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.main_menu, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return super.onOptionsItemSelected(item)
	}

	companion object {
		private val TAG = "MainActivity"

		val ANONYMOUS = "anonymous"
		val DEFAULT_MSG_LENGTH_LIMIT = 1000
	}
}
