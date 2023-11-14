package com.lihao.kotlincoroutinedemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.NullPointerException
import kotlin.system.measureTimeMillis

class MainActivity2 : AppCompatActivity() {

    private val mMainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button?>(R.id.test_btn).also {
            it.setOnClickListener {
//                start()
//                start2()
//                start3()
//                start4()
//                start5()
//                start6()
//                start7()
                start8()
            }
        }
    }

    private fun start() {
        // 在主线程执行任务。
        GlobalScope.launch(Dispatchers.Main) {
            // Main线程在这里挂起5秒，但不是卡死，而是不会执行下面的inIO方法了。
            // 可以理解为把后面的inIO等代码打了个包，作为delay的callback使用了。
            delay(5000)
//            Thread.sleep(5000) // 如果使用Thread.sleep，将会阻塞Main线程。
            // 切换至IO线程。
            inIO()
            // 执行完IO任务后回到了主线程。
            Log.e("stats", "继续执行主线程：" + Thread.currentThread().id)
        }
    }

    private suspend fun inIO() {
        withContext(Dispatchers.IO) {
            Log.e("stats", "任务执行于：" + Thread.currentThread().id)
        }
    }

    // --------------------

    private fun start2() {
        GlobalScope.launch {
            mMainScope.launch {
                delay(10000)
                Log.e("stats", "MainScope协程体执行完毕。")
            }

            // launch与async的区别：async的返回值是一个Deferred，可以用Deferred.await来延期获取协程最后的返回结果。
            val job1 = mMainScope.launch(start = CoroutineStart.DEFAULT) {
                delay(2000)
                Log.e("stats", "通过launch启动的Job。")
            }
            job1.join() // 如果使用join，则和线程类似，下面的代码都要等待job1执行完毕后才会继续运行。
            val job2 = mMainScope.async {
                delay(1000)
                Log.e("stats", "通过async启动的Deferred，也是一个Job。")
                500
            }
            // job1就没有await，这里只能job2调用await。
            // job2的await也只能在协程中被调用。
            Log.e("stats", "job2结果是：" + job2.await()) // await也有join的作用。

            val timeCount = measureTimeMillis {
                // 这种写法，doOne之后的代码还是会变成doOne的回调，也就是说doOne和doTwo是顺序执行的。
//                val oneRes = doOne()
//                val twoRes = doTwo()
//                Log.e("stats", "运行结果是：${oneRes + twoRes}")
                // 但是套了async后，doOne和doTwo会在两个协程中同时运行，
                // 后面的await也会在当前协程中分别等待doOne和doTwo两个协程的执行结果，但是doOne和doTwo是同时进行的，
                // 因此等待时长是doOne和doTwo里面执行时间最长的那个。
                val oneRes = async { doOne() }
                val twoRes = async { doTwo() }
                Log.e("stats", "运行结果是：${oneRes.await() + twoRes.await()}")
            }
            Log.e("stats", "总用时：${timeCount}")

            // 如果此处使用undispatched，则lazyCo将会在当前线程/协程中立即执行，不受async的分配。
            val lazyCo = async(start = CoroutineStart.LAZY) {
                Log.e("stats", "LAZY模式启动。")
                100
            }
            // 如果不使用await，则lazyCo将不会主动启动，因为它的启动模式是LAZY。
            Log.e("stats", "LAZY运行结果是：${lazyCo.await()}")
        }
    }

    private suspend fun doOne() : Int {
        delay(1000)
        Log.e("stats", "doOne执行完毕")
        return 10
    }

    private suspend fun doTwo() : Int {
        delay(2000)
        Log.e("stats", "doTwo执行完毕")
        return 20
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainScope.cancel()
    }

    // --------------------

    private fun start3() {
        GlobalScope.launch {
            // 如果不是安卓环境，使用supervisorScope可以在一个子协程崩溃的时候，保证其他协程都执行完毕。
            // 但安卓重写了全局异常捕获，一个子协程崩溃，如果不加处理，则整个进程都会被杀死。
            supervisorScope {
                launch {
                    delay(4000)
                    Log.e("stats", "协程1 finished。")
                }
                launch {
                    delay(2000)
                    Log.e("stats", "协程2 finished。")
                    throw NullPointerException()
                }
            }
        }
    }

    // --------------------

    private fun start4() {
        GlobalScope.launch {
            val startTime = System.currentTimeMillis()
            val job = launch(Dispatchers.Default) {
                var nextTime = startTime
                var i = 0
//                while (i < 5) {
                while (i < 5 && isActive) { // 如果使用isActive，则会在CPU密集运行过程中判断协程的状态，若外部使用了cancel，则可以判断出isActive是false。
                    if (System.currentTimeMillis() >= nextTime) {
                        Log.e("stats", "内部任务休息：${i}")
                        i++
                        nextTime += 500
                    }
                }
            }
            delay(1300)
            Log.e("stats", "外部协程想停止内部CPU密集型协程")
            job.cancelAndJoin() // 顺路等待内部协程运行完毕。
            Log.e("stats", "外部协程运行完毕。")
        }
    }

    // --------------------

    private fun start5() {
        GlobalScope.launch {
            // 处理协程的异常。
            val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
                Log.e("stats", "协程发生了异常：${exception}")
            }
            // 在构建协程作用域的时候，可以指定特定的协程异常处理器。
            val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("test") + coroutineExceptionHandler)
            // 异常捕获器只能安装在最外层的scope上，如果安装在内部的async，则异常捕获器是不起作用的。
            // 这点类似于安卓的全局异常捕获。
            val job = scope.launch {
                Log.e("stats", "外层携程任务信息：${coroutineContext[Job]}，所在线程名称：${Thread.currentThread().name}")
                // 又开了一个子协程。
                val result = async {
                    Log.e("stats", "内部子携程任务信息：${coroutineContext[Job]}，所在线程名称：${Thread.currentThread().name}")
                    "结果"
                }
                result.await()
            }
            job.join()
        }
    }

    // --------------------

    private fun start6() {
        GlobalScope.launch {
            Log.e("stats", "执行")
            flowDemo().onEach { Log.e("stats", "收集到了${it}") }
//                .buffer(50) // 如果背压过大，则缓存50个数据。
                .conflate() // 如果背压过大，则忽略处理不了的中间数据。
                .launchIn(CoroutineScope(Dispatchers.Default)) // 改变接收线程。
                .join()
            Log.e("stats", "又执行一次")
            flowDemo().collect {
                Log.e("stats", "又一次收集到了${it}")
            }

            // 流的连续性。
            (1..5).asFlow().filter {
                it % 2 == 0
            }.map {
                "过滤流${it}"
//            }.collect {
            }.collectLatest { // 如果不用collect而是用这个，则当背压严重的时候，接收器会忽略前面所有的值，只接受最新的值。
                Log.e("stats", "流的连续性：${it}")
            }

            // 流的异常捕获。
            flow<Int> {
                emit(1)
                emit(2)
                throw NullPointerException("模拟抛出异常")
                emit(3)
            }.onCompletion { // 注：onCompletion只能发现异常，但不会处理异常。处理异常还是要用catch。
                Log.e("stats", "完成")
                if (it != null) {
                    Log.e("stats", "发现了异常：${it}")
                }
            }.catch {
                Log.e("stats", "处理了异常：${it}")
            }.collect {
                Log.e("stats", "抓取${it}")
            }
        }
    }

    fun flowDemo() = flow<Int> {
        Log.e("stats", "流启动...")
        for (i in 1..3) {
            delay(1000)
            Log.e("stats", "发射：${i}")
            emit(i)
        }
    }.flowOn(Dispatchers.IO)

    // --------------------

    private fun start7() {
        GlobalScope.launch {
            // channel的容量默认是0，如果channel里面有一个元素，但没有人接收，则再往channel里面发送元素的语句会挂起。
            val channel = Channel<Int>(5) // 容量默认是0，此处指定5，相当于缓冲区容量。
            val producer = launch {
                var element = 0
                while (element < 10) {
                    delay(1000)
                    channel.send(++element)
                    Log.e("stats", "生产了一个元素：${element}")
                }
            }
            val consumer = launch {
                while (true) {
                    delay(2000) // 如果不指定channel容量，则此处消费者明显慢于生产者，会导致生产者速度变低。
                    val item = channel.receive()
                    Log.e("stats", "收到了${item}")
                    if (item >= 10) {
                        break
                    }
                }
                Log.e("stats", "完毕")
            }
            joinAll(producer, consumer)

            // 使用生产者与消费者同样可以达到channel的效果。
            val producerA : ReceiveChannel<Int> = produce {
                for (i in 1..5) {
                    delay(1000)
                    send(i)
                }
            }
            launch {
                for (i in producerA) {
                    Log.e("stats", "收到了生产者的信息：${i}")
                }
            }.join()

            val consumerA : SendChannel<Int> = actor {
                while (true) {
                    val item = receive()
                    Log.e("stats", "消费者接收到了信息：${item}")
                    if (item >= 5) {
                        break;
                    }
                }
            }
            launch {
                for (i in 1..5) {
                    delay(1000)
                    consumerA.send(i)
                }
            }.join()

            // 还可以用广播通道来进行多个接受者之间的信息发送。
            val broadcastChannel = BroadcastChannel<Int>(Channel.BUFFERED)
            launch {
                for (i in 1..3) {
                    delay(1000)
                    broadcastChannel.send(i)
                }
                broadcastChannel.close()
            }
            // 开三个协程来接收广播消息。
            for (index in 1..3) {
                launch {
                    val receiveChannel = broadcastChannel.openSubscription()
                    for (i in receiveChannel) {
                        Log.e("stats", "第${index}个接受者收到了${i}")
                    }
                }
            }
        }
    }

    // --------------------

    private fun start8() {
        GlobalScope.launch {
            // 协程复用安全问题。
            var count = 0
            List(1000) {
                launch { count++ }
            }.joinAll()
            Log.e("stats", "初始累加值：${count}") // 这里每次都不一样，因为1000个协程同时操作一个count值，有同步问题。

            var count2 = AtomicInteger(0)
            List(1000) {
                launch { count2.incrementAndGet() }
            }.joinAll() // 这里需要增加joinAll，否则下面的日志打印有可能比1000次累加还要快。
            Log.e("stats", "AtomicInteger累加值：${count2.get()}") // AtomicInteger是原子操作。

            var count3 = 0
            val mutex = Mutex()
            List(1000) {
                launch {
                    mutex.withLock {
                        count3++
                    }
                }
            }.joinAll()
            Log.e("stats", "使用mutex累加值：${count3}")

            var count4 = 0
            val semaphore = Semaphore(1) // 信号量容量为1的时候相当于mutex。
            List(1000) {
                launch {
                    semaphore.withPermit {
                        count4++
                    }
                }
            }.joinAll()
            Log.e("stats", "使用信号量累加值：${count4}")

            var count5 = 0
            val result = count5 + List(1000) {
                async { 1 }
            }.map { it.await() }.sum() // 实际上这里的await会等待1000个协程全部执行完毕。
            Log.e("stats", "使用await等待所有值累加结束：${result}")
        }
    }
}