package com.kunalapps.vaccineFinder.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.kunalapps.vaccineFinder.R
import com.kunalapps.vaccineFinder.models.Session
import com.kunalapps.vaccineFinder.utils.inflate
import kotlinx.android.synthetic.main.list_item_session.view.*


class SessionsAdapter(
    private var activity: FragmentActivity?,
    private var sessions: List<Session>,
    private var doseCheckedItem: Int
) : RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val v = parent.inflate(R.layout.list_item_session);
        return SessionViewHolder(v)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {

        holder.bindView(sessions[position], activity, doseCheckedItem)
    }

    class SessionViewHolder(v: View) : View.OnClickListener, RecyclerView.ViewHolder(v) {

        private lateinit var session: Session
        private lateinit var activity: FragmentActivity
        val context: Context = itemView.context

        init {
            itemView.setOnClickListener(this)
        }

        fun bindView(session: Session, activity: FragmentActivity?, doseCheckedItem: Int) {
            this.session = session
            this.activity = activity!!
            itemView.centerName.text = session.name
            itemView.locationName.text = session.block_name + ", " + session.state_name + ", " + session.pincode
            itemView.doseOne.text =
                context.getString(R.string.dose_one) + ": " + session.available_capacity_dose1.toString()
            itemView.doseTwo.text =
                context.getString(R.string.dose_two) + ": " + session.available_capacity_dose2.toString()

            var availableDoses = getAvailableDoseAccordingToSelection(session, doseCheckedItem);

            if (availableDoses == 1) {
                itemView.availableSlots.text =
                    availableDoses.toString() + " " + context.getString(R.string.slot_available)
            } else {
                itemView.availableSlots.text =
                    availableDoses.toString() + " " + context.getString(R.string.slots_available)
            }

            itemView.ageLimit.text = session.min_age_limit.toString() + "+"
            itemView.date.text = session.date

            val vaccine: String = when (session.vaccine) {
                "COVISHIELD" -> {
                    context.getString(R.string.covishield)
                }
                "COVAXIN" -> {
                    context.getString(R.string.covaxin)
                }
                "SPUTNIK V" -> {
                    context.getString(R.string.sputnik_v)
                }
                else -> {
                    session.vaccine
                }
            }

            itemView.vaccineName.text = vaccine

            itemView.locationName.setOnClickListener {
                //Setting click Listener on image
                val strUri =
                    ("http://maps.google.com/maps?q=" + session.name + "," + session.address + "," + session.district_name + "," + session.pincode)

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
                intent.setClassName(
                    "com.google.android.apps.maps",
                    "com.google.android.maps.MapsActivity"
                )
                activity.startActivity(intent)

            }
            if (session.fee_type == "Free") {
                itemView.feeType.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemView.feeType.text = " (" + context.getString(R.string.free) + ")"
            } else if (session.fee_type == "Paid") {
                itemView.feeType.setTextColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_red_light
                    )
                )
                itemView.feeType.text = " (₹" + session.fee + ")"
            }
            if (session.available_capacity > 0) {
                itemView.availableSlots.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_arrow_forward_24_white,
                    0
                )
                itemView.availableSlots.setTextColor(ContextCompat.getColor(context, R.color.white))
                itemView.availableSlots.background =
                    ContextCompat.getDrawable(context, R.drawable.available_slots_background)
                itemView.ageLimit.setTextColor(ContextCompat.getColor(context, R.color.white))
                itemView.ageLimit.background =
                    ContextCompat.getDrawable(context, R.drawable.green_background)
                itemView.date.setTextColor(ContextCompat.getColor(context, R.color.white))
                itemView.date.background =
                    ContextCompat.getDrawable(context, R.drawable.green_background)
            } else {
                itemView.availableSlots.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_baseline_arrow_forward_24_black,
                    0
                )
                itemView.availableSlots.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemView.availableSlots.background =
                    ContextCompat.getDrawable(context, R.drawable.not_available_slots_background)
                itemView.ageLimit.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemView.ageLimit.background =
                    ContextCompat.getDrawable(context, R.drawable.grey_background)
                itemView.date.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemView.date.background =
                    ContextCompat.getDrawable(context, R.drawable.grey_background)
            }
        }

        private fun getAvailableDoseAccordingToSelection(
            session: Session,
            doseCheckedItem: Int
        ): Int {

            if (doseCheckedItem != -1) {
                if (doseCheckedItem == 0) {
                    return session.available_capacity_dose1;
                } else if (doseCheckedItem == 1) {
                    return session.available_capacity_dose2;
                }
            } else {
                return session.available_capacity;
            }
            return session.available_capacity;
        }

        override fun onClick(v: View?) {
            val URL_TO_BE_LOADED="https://selfregistration.cowin.gov.in/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(URL_TO_BE_LOADED)
            activity.startActivity(i)
//            activity.startActivity(Intent(context, WebViewActivity::class.java))
//            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
        }
    }

    override fun getItemCount() = getSessions().size

    fun setSessions(sessionsList: List<Session>, doseCheckedItem: Int) {
        this.sessions = sessionsList
        this.doseCheckedItem = doseCheckedItem
    }

    private fun getSessions(): List<Session> {
        return sessions
    }

}