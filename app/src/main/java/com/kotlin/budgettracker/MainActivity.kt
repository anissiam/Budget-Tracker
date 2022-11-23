package com.kotlin.budgettracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var transactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase
    private lateinit var deletedTransaction: Transaction
    private lateinit var oldTransactions: List<Transaction>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        transactions = arrayListOf()
        /*transactions = arrayListOf(
            Transaction("Weekends budget", 400.00),
            Transaction("Bananas", -4.00),
            Transaction("Gasoline", -40.90),
            Transaction("Breakfast", -9.99),
            Transaction("Water", -4.00),
            Transaction("Car Parks", -15.00),
        )*/
        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build()


        //var recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.apply { // using Kotlin extensions
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }
        addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        //swipe to remove
        val itemTouchHelper = object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
               return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recyclerview)

    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        oldTransactions = transactions
        GlobalScope.launch {
            db.transactionDao().delete(transaction)
            transactions = transactions.filter { it.id!=transaction.id }
            runOnUiThread {
                updateDashbord()
                showSnackBar()

            }

        }
    }

    private fun showSnackBar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view , "Transaction deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setTextColor(ContextCompat.getColor(this , R.color.white))
            .show()
    }

    private fun undoDelete() {
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)
            transactions = oldTransactions
            runOnUiThread {
                transactionAdapter.setData(transactions)
                updateDashbord()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
    private fun fetchAll() {
        GlobalScope.launch {
            //db.transactionDao().insertAll(Transaction(0,"IceCream",-3.00 , "T") )
            transactions = db.transactionDao().getAll()
            Log.d("sdsaadd", "${transactions.size}")
            runOnUiThread {
                updateDashbord()
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun updateDashbord() {
        val total = transactions.map { it.amount }.sum() // sum of all amount from transactions list
        val budgetList = transactions.filter { it.amount > 0 }
            .map { it.amount } // list of all the amount of transactions that are amount grater then 0
        val budgetAmount = budgetList.sum()
        val expenseAmount = total - budgetAmount

        balance.text = "$ %.2f".format(total)
        budget.text = "$ %.2f".format(budgetAmount)
        expense.text = "$ %.2f".format(expenseAmount)

    }
}