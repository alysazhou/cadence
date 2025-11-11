import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

enum class EmailResult {
    Valid,
    Empty,
    Invalid,
}

fun checkEmail(email: String) : EmailResult {
    if (email.isEmpty()){
        return EmailResult.Empty
    }

    val pattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    return if (pattern.matches(email)) {
        EmailResult.Valid
    } else {
        EmailResult.Invalid
    }
}

enum class PasswordResult {
    Valid,
    Empty,
    Short,
    Invalid
}

fun checkPassword(password: String) : PasswordResult {
    if (password.isEmpty()) {
        return PasswordResult.Empty
    }
    if (password.length < 5) {
        return PasswordResult.Short
    }
    if (Regex("\\d+").containsMatchIn(password) &&
        Regex("[a-z]+").containsMatchIn(password) &&
        Regex("[A-Z]+").containsMatchIn(password)
    ) {
        return PasswordResult.Valid
    }
    return PasswordResult.Invalid
}

/**
 * sign in with existing email/password
 */
fun signIn(
    email: String,
    password: String,
    onSuccess: (FirebaseUser) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    onSuccess(user)
                } else {
                    onFailure(IllegalStateException("Firebase user is null after successful sign-in."))
                }
            } else {
                task.exception?.let(onFailure)
            }
        }
}

/**
 * register a new account
 */
fun createAccount(
    email: String,
    password: String,
    onSuccess: (FirebaseUser) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val user = result.user
            if (user != null) {
                onSuccess(user)
            } else {
                onFailure(IllegalStateException("User is null after account creation."))
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

/**
 * update display name
 */
fun updateName(name: String) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            displayName = name
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("User profile updated successfully. New name: $name")
                } else {
                    println("Failed to update user profile: ${task.exception?.message}")
                }
            }
    } else {
        println("No authenticated Firebase user found.")
    }
}

fun reauthenticateUser(
    password: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val user = Firebase.auth.currentUser
    if (user?.email == null) {
        onFailure(IllegalStateException("User is not logged in or email is missing."))
        return
    }

    val credential = EmailAuthProvider.getCredential(user.email!!, password)

    user.reauthenticate(credential)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}