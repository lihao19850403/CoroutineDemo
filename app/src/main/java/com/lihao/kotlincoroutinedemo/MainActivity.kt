package com.lihao.kotlincoroutinedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private lateinit var mBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBtn = findViewById(R.id.test_btn)
        mBtn.setOnClickListener {
            start()
        }
    }

    private fun start() {
//        val runBlockingJob = runBlocking {
//            Log.d("runBlocking", "启动一个协程")
//            41
//        }
//        Log.d("runBlockingJob", "$runBlockingJob")
//        val launchJob = GlobalScope.launch {
//            Log.d("launch", "启动一个协程")
//        }
//        Log.d("launchJob", "$launchJob")
//        val asyncJob = GlobalScope.async {
//            Log.d("async", "启动一个协程")
//            "我是返回值"
//        }
//        Log.d("asyncJob", "$asyncJob")

//        GlobalScope.launch {
//            val launchJob = launch {
//                Log.d("launch", "启动一个协程")
//            }
//            Log.d("launchJob", "$launchJob")
//            val asyncJob = async {
//                Log.d("async", "启动一个协程")
//                "我是async的返回值"
//            }
//            Log.d("asyncJob.wait", "${asyncJob.await()}")
//            Log.d("asyncJob", "$asyncJob")
//        }

//        GlobalScope.launch(Dispatchers.Main) {
//            Log.d("stats", "主线程")
//            val result = withContext(Dispatchers.IO) {
//                Log.d("stats", "IO线程执行任务")
//                "请求结果"
//            }
//            Log.d("stats", "执行完毕：$result")
//        }

//        val defaultJob = GlobalScope.launch {
//            Log.d("defaultJob", "CoroutineStart.DEFAULT")
//        }
//        defaultJob.cancel()
//        val lazyJob = GlobalScope.launch(start = CoroutineStart.LAZY) {
//            Log.d("lazyJob", "CoroutineStart.LAZY")
//        }
//        val atomicJob = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
//            Log.d("atomicJob", "CoroutineStart.ATOMIC挂起前")
//            delay(100)
//            Log.d("atomicJob", "CoroutineStart.ATOMIC挂起后")
//        }
//        atomicJob.cancel()
//        val undispatchedJob = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
//            Log.d("undispatchedJob", "CoroutineStart.UNDISPATCHED挂起前")
//            delay(100)
//            Log.d("undispatchedJob", "CoroutineStart.UNDISPATCHED挂起后")
//        }
//        undispatchedJob.cancel()

//        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//            Log.d("exceptionHandler", "${coroutineContext[CoroutineName]} $throwable")
//        }
//        GlobalScope.launch(Dispatchers.Main + CoroutineName("scope1") + exceptionHandler) {
//            Log.d("scope", "---------- 1")
//            launch(CoroutineName("scope2") + exceptionHandler) {
//                Log.d("scope", "---------- 2")
//                throw NullPointerException("空指针")
//                Log.d("scope", "---------- 3")
//            }
//            val scope3 = launch(CoroutineName("scope3") + exceptionHandler) {
//                Log.d("scope", "---------- 4")
//                delay(2000)
//                Log.d("scope", "---------- 5")
//            }
//            scope3.join()
//            Log.d("scope", "---------- 6")
//        }

//        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//            Log.d("exceptionHandler", "${coroutineContext[CoroutineName]}: $throwable")
//        }
//        var a: MutableList<Int> = mutableListOf(1, 2, 3)
//        GlobalScope.launch(CoroutineName("异常处理") + exceptionHandler) {
//            launch {
//                Log.d("${Thread.currentThread().name}", "我要开始抛异常了")
//                try {
//                    launch {
//                        Log.d("${Thread.currentThread().name}", "${a[1]}")
//                    }
//                    a.clear()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            Log.d("${Thread.currentThread().name}", "end")
//        }

//        val supervisorScope = CoroutineScope(SupervisorJob() + exceptionHandler)
//        with(supervisorScope) {
//            launch(CoroutineName("异常子协程")) {
//                Log.d("${Thread.currentThread().name}", "我要开始抛异常了")
//                throw NullPointerException("空指针异常")
//            }
//            for (index in 0 .. 10) {
//                launch(CoroutineName("子协程$index")) {
//                    Log.d("${Thread.currentThread().name}正常执行", "$index")
//                    if (index % 3 == 0) {
//                        throw NullPointerException("子协程${index}空指针异常")
//                    }
//                }
//            }
//        }

        MainScope().launch {
            
        }
    }
}









