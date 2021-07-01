package com.th.pv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import com.th.pv.data.PVData
import java.security.MessageDigest

class LoginFragment : Fragment() {
    lateinit var pvData : PVData

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        pvData = (activity as MainActivity).pvData
        return inflater.inflate(R.layout.login_fragment, container, false)
    }

    private fun sha512(pass : String) : String{
        val digest = MessageDigest.getInstance("SHA-512")
        val hash = digest.digest(pass.toByteArray())

        return hash.map { Integer.toHexString(0xFF and it.toInt()) }
                .map { if (it.length < 2) "0$it" else it }
                .fold("", { acc, d -> acc + d })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Login"
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val ipEditText = view.findViewById<EditText>(R.id.editTextAddress)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)

        ipEditText.setText(ip)

        view.findViewById<Button>(R.id.buttonConnect).setOnClickListener {
            ip = ipEditText.text.toString()
            val pass = sha512(passwordEditText.text.toString())

            if (pass == password || password.isEmpty()) {
                password = pass
                val bundle = bundleOf("showAlbums" to false)

                (activity as MainActivity).queryStats()

                view.findViewById<TextView>(R.id.errorText).visibility = TextView.GONE
                findNavController().navigate(R.id.action_loginFragment_to_actorsListFragment, bundle)
            }
            else {
                view.findViewById<TextView>(R.id.errorText).visibility = TextView.VISIBLE
                view.findViewById<TextView>(R.id.errorText).text = "Wrong password. If the password was changed, delete \"" + pvData.savePath + "\" and restart app"
            }
        }
    }
}