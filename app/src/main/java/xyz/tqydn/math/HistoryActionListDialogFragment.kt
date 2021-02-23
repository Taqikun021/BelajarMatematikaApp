package xyz.tqydn.math

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.tqydn.math.databinding.FragmentHistoryActionListDialogBinding
import xyz.tqydn.math.databinding.FragmentHistoryActionListDialogItemBinding
import kotlin.collections.ArrayList

class HistoryActionListDialogFragment : BottomSheetDialogFragment() {

    private var mListener: Listener? = null
    private lateinit var mData: ArrayList<String>
    private var _binding: FragmentHistoryActionListDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryActionListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mData = ArrayList(listOf(getString(R.string.no_history)))

        val data = arguments?.getStringArrayList(ARG_HISTORY_ACTION)
        if (data!!.isNotEmpty()) {
            mData.clear()
            mData.addAll(data)
        }

        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.adapter = ItemAdapter(mData)

        binding.buttonClearHistory.setOnClickListener {
            data.clear()
            mData.clear()
            mData.add(getString(R.string.no_history))
            Toast.makeText(activity, getString(R.string.history_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        mListener = if (parent != null) {
            parent as Listener
        } else {
            context as Listener?
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onHistoryItemClicked(resultText: String)
    }

    private inner class ViewHolder(private val binding: FragmentHistoryActionListDialogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String){
            val reg = Regex("(?<=[=])")
            val historyActionList = item.split(reg)
            binding.action.text = if (historyActionList.size == 1) "" else historyActionList.first()
            binding.result.text = historyActionList.last().trim()
            binding.row.setOnClickListener {
                if (mListener != null) {
                    mListener!!.onHistoryItemClicked(binding.result.text.toString())
                    dismiss()
                }
            }
        }
    }

    private inner class ItemAdapter(private val mHistoryActionList: ArrayList<String>) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = FragmentHistoryActionListDialogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val items = mHistoryActionList[position]
            holder.bind(items)
        }

        override fun getItemCount(): Int {
            return mHistoryActionList.count()
        }

    }

    companion object {

        private const val ARG_HISTORY_ACTION = "history_action"

        fun newInstance(historyActionList: ArrayList<String>): HistoryActionListDialogFragment {
            val fragment = HistoryActionListDialogFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_HISTORY_ACTION, historyActionList)
            fragment.arguments = args
            return fragment
        }
    }

}