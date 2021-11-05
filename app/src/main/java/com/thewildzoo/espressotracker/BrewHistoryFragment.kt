package com.thewildzoo.espressotracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.thewildzoo.espressotracker.adapter.BrewHistoryAdapter
import com.thewildzoo.espressotracker.helper.BrewHistoryRecyclerViewAdapterInterface
import com.thewildzoo.espressotracker.model.BrewRecipe
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [BrewHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BrewHistoryFragment : Fragment(), BrewHistoryRecyclerViewAdapterInterface {

    lateinit var recyclerView: RecyclerView
    private val viewmodel: BrewHistoryViewModel by viewModels()
    private lateinit var brewHistoryAdapter: BrewHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        brewHistoryAdapter = BrewHistoryAdapter(mutableListOf(), requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brew_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.brew_history_list)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = brewHistoryAdapter
        }

        viewmodel.recipes.observe(
            viewLifecycleOwner,
            {
                val items = mutableListOf<BrewRecipe>()
                items.addAll(it)
                brewHistoryAdapter.setData(items)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewmodel.getRecipes()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BrewHistory.
         */
        @JvmStatic
        fun newInstance() =
            BrewHistoryFragment()
    }

    override fun deleteItem(position: Int) {
        val adapter = recyclerView.adapter as BrewHistoryAdapter
        val removedRecipe = adapter.removeAt(position)

        lifecycleScope.launch {
            viewmodel.deleteRecipe(removedRecipe)
        }

        Snackbar.make(requireView(), R.string.label_item_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.label_item_deleted_undo) { _ ->
                adapter.add(position, removedRecipe)
            }
            .show()
    }

    override fun addItem(recipe: BrewRecipe) {
        lifecycleScope.launch {
            viewmodel.insertRecipe(recipe)
        }
    }
}
