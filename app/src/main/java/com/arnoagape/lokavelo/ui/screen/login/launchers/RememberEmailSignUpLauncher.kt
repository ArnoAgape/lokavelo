package com.arnoagape.lokavelo.ui.screen.login.launchers

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.common.Event
import com.arnoagape.lokavelo.ui.screen.login.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse

/**
 * Creates and remembers an email sign-up launcher using FirebaseUI Auth.
 * On success, the user is synced with Firestore and a success message is sent.
 * On failure, an error event is emitted.
 *
 * @param loginViewModel ViewModel used to sync the user and send UI events.
 * @return A lambda that launches the email sign-up flow.
 */
@Composable
fun rememberEmailSignUpLauncher(
    loginViewModel: LoginViewModel
): () -> Unit {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = IdpResponse.fromResultIntent(result.data)

        if (result.resultCode == Activity.RESULT_OK) {
            loginViewModel.syncUserWithFirestore()
            loginViewModel.sendEvent(Event.ShowMessage(R.string.success_sign_up))

        } else {
            if (response == null) {
                loginViewModel.sendEvent(Event.ShowMessage(R.string.error_sign_up))
            }
        }
    }

    val signUpIntent = remember {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            //.setLogo(R.drawable.ic_lokavelo_logo)
            .setTheme(R.style.Theme_Lokavelo)
            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
            .build()
    }

    return {
        launcher.launch(signUpIntent)
    }
}