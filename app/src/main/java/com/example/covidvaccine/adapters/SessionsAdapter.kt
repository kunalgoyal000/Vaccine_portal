package com.example.covidvaccine.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.covidvaccine.R
import com.example.covidvaccine.WebViewActivity
import com.example.covidvaccine.models.Session
import com.example.covidvaccine.utils.inflate
import kotlinx.android.synthetic.main.list_item_session.view.*

class SessionsAdapter(private var activity: FragmentActivity?, private var sessions: List<Session>): RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val v=parent.inflate(R.layout.list_item_session);
        return SessionViewHolder(v)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {

        holder.bindView(sessions[position],activity)
    }

    class SessionViewHolder(v:View):View.OnClickListener,RecyclerView.ViewHolder(v){

        private lateinit var session: Session
        private lateinit var activity:FragmentActivity
        val context: Context =itemView.context
        init {
            itemView.setOnClickListener(this)
        }
        fun bindView(session: Session, activity: FragmentActivity?) {
            this.session = session
            this.activity = activity!!
            itemView.centerName.text = session.name
            itemView.locationName.text = session.block_name + ", " + session.pincode
            itemView.doseOne.text = "Dose One: " + session.available_capacity_dose1.toString()
            itemView.doseTwo.text = "Dose Two: " + session.available_capacity_dose2.toString()

            if(session.available_capacity==1) {
                itemView.availableSlots.text = session.available_capacity.toString() + " slot available"
            }else{
                itemView.availableSlots.text = session.available_capacity.toString() + " slots available"
            }

            itemView.ageLimit.text = session.min_age_limit.toString() + "+"
            itemView.date.text=session.date
            itemView.vaccineName.text = session.vaccine

            itemView.locationName.setOnClickListener {
                //Setting click Listener on image
                val strUri = ("http://maps.google.com/maps?q=" +session.name + ","+ session.address + "," +session.district_name + "," + session.pincode)

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(strUri))
                    intent.setClassName(
                        "com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity")
                    activity.startActivity(intent)

            }
            if (session.fee_type.equals("Free")) {
                itemView.feeType.setTextColor(ContextCompat.getColor(context,R.color.black))
                itemView.feeType.text =" (" + session.fee_type + ")"
            } else if (session.fee_type.equals("Paid")) {
                itemView.feeType.setTextColor(ContextCompat.getColor(context,android.R.color.holo_red_light))
                itemView.feeType.text =" (₹" + session.fee + ")"
            }
            if (session.available_capacity > 0) {
                itemView.availableSlots.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_arrow_forward_24_white,0)
                itemView.availableSlots.setTextColor(ContextCompat.getColor(context,R.color.white))
                itemView.availableSlots.background = ContextCompat.getDrawable(context,R.drawable.available_slots_background)
                itemView.ageLimit.setTextColor(ContextCompat.getColor(context,R.color.white))
                itemView.ageLimit.background = ContextCompat.getDrawable(context,R.drawable.green_background)
                itemView.date.setTextColor(ContextCompat.getColor(context,R.color.white))
                itemView.date.background = ContextCompat.getDrawable(context,R.drawable.green_background)
            }else{
                itemView.availableSlots.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_arrow_forward_24_black,0)
                itemView.availableSlots.setTextColor(ContextCompat.getColor(context,R.color.black))
                itemView.availableSlots.background =  ContextCompat.getDrawable(context,R.drawable.not_available_slots_background)
                itemView.ageLimit.setTextColor(ContextCompat.getColor(context,R.color.black))
                itemView.ageLimit.background = ContextCompat.getDrawable(context,R.drawable.grey_background)
                itemView.date.setTextColor(ContextCompat.getColor(context,R.color.black))
                itemView.date.background = ContextCompat.getDrawable(context,R.drawable.grey_background)
            }
        }

        override fun onClick(v: View?) {
            activity.startActivity(Intent(context,WebViewActivity::class.java))
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
        }
    }

    override fun getItemCount() = getSessions().size

    fun setSessions(sessionsList: List<Session>) {
        this.sessions=sessionsList
    }

     private fun getSessions(): List<Session> {
        return sessions
    }

}