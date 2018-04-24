package rutgers.edu.bonfire.extensions

import android.app.Fragment
import android.content.Context
import android.widget.Toast

/**
 * Created by hemanth on 3/7/18.
 */

fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Fragment.toast(message: CharSequence)=
        context?.toast(message)