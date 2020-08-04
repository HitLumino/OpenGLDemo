# Java并发

- `sleep` 是线程的方法，` wait / notify / notifyAll `是 Object 类的方法；
- `sleep` 不会释放当前线程持有的锁，到时间后程序会继续执行，`wait` 会释放线程持有的锁并挂起，直到通过 `notify` 或者 `notifyAll` 重新获得锁。

## 常见的同步场景：

> 一个线程执行过程中，需要开启另外一个子线程去做某个耗时的操作（通过休眠3秒模拟）， 并且**等待**子线程返回结果，主线程再根据返回的结果继续往下执行。

* 接口回调：

  * 主线程执行`download`, `download`在子线程里跑

  * 定义接口`IDownload`,下载的结果有两种：`onSucess` or `onFailed`

    ```java
    public interface IDownload {
        /**
         * 下载成功
         */
        void onSucess();
    
        /**
         * downlaod failed
         */
        void onFailed();
    }
    ```

  * 主线程里实现download 接口 `IDownload callback`私有变量,

    并调用这个异步接口。

    ```java
    private IDownload mCallback = new IDownload() {
            @Override
            public void onSucess() {
                Log.d(TAG, "onSucess: ");
            }
    
            @Override
            public void onFailed() {
                Log.d(TAG, "onFailed: ");
    
            }
        };
    
    // .....
      download(mCallback);
    // .....
    ```

    或者直接调用，传参将callback掺入

    ```java
    download(new IDownload() {
                @Override
                public void onSucess() {
                    
                }
    
                @Override
                public void onFailed() {
    
                }
            });
    ```

  * 下载函数里根据下载情况调用

    ```java
    boolean download(IDownload callback) {
            if (成功) {
                callback.onSucess();
            } else {
                callback.onFailed();
            }
            return true;
        }
    ```

* 设置标志位flag，注意标志位`volatile`

  ```java
  volatile boolean flag = false;
  
      public void test(){
          //...
  
          Thread t1 = new Thread(() -> {
              try {
                  Thread.sleep(3000);
                  System.out.println("--- 休眠 3 秒");
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  flag = true;
              }
          });
          t1.start();
  
          while(!flag){
  
          }
          System.out.println("--- work thread run");
      }
  ```

  强调一点，声明标志位的时候，一定注意 `volatile` 关键字不能忘，如果不加该关键字修饰，程序可能进入死循环。这是同步中的可见性问题，在 《java 并发——内置锁》 中有记录。

  显然，这个实现方案并不好，本来主线程什么也不用做，**却一直在竞争资源，做空循环，性能上不好**，所以并不推荐。

* 线程join方法

  ```java
  public void test(){
          //...
  
          Thread t1 = new Thread(() -> {
              try {
                  Thread.sleep(3000);
                  System.out.println("--- 休眠 3 秒");
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          });
  
          t1.start();
  
          try {
              t1.join();
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          System.out.println("--- work thread continue");
      }
  ```

  上面的代码，执行结果同上。利用 Thread 类的` join `方法实现了同步，达到了效果，但是 `join` 方法不能一定保证效果，在不同的 cpu 上，可能呈现出意想不到的结果，所以尽量不要用上述方法。

* 使用闭锁`CountDownLatch`

  ```java
  public void test(){
          //...
  
          final CountDownLatch countDownLatch = new CountDownLatch(1);
  
          Thread t1 = new Thread(() -> {
              try {
                  Thread.sleep(3000);
                  System.out.println("--- 休眠 3 秒");
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  countDownLatch.countDown();
              }
          });
  
          t1.start();
  
          try {
              countDownLatch.await();
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          System.out.println("--- work thread run");
      }
  ```

  上面的代码，执行结果同上。同样可以实现上述效果，执行结果和上面一样。该方法推荐使用。

* 利用`wait/notify`优化标志位方法

  为了方便对比，首先给 2.1 中的循环方法增加一些打印。修改后的代码如下：

  ```java
  volatile boolean flag = false;
  
      public void test() {
          //...
          Thread t1 = new Thread(() -> {
              try {
                  Thread.sleep(3000);
                  System.out.println("--- 休眠 3 秒");
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } finally {
                  flag = true;
              }
          });
          t1.start();
  
          while (!flag) {
              try {
                  System.out.println("---while-loop---");
                  Thread.sleep(500);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          System.out.println("--- work thread run");
      }
  ```

  事实证明，while 循环确实一直在执行。

  为了使该线程再不需要执行的时候不抢占资源，我们可以利用 `wait` 方法将其挂起，在需要它执行的时候，再利用 `notify` 方法将其唤醒。这样达到优化的目的，优化后的代码如下：

  ```java
  volatile boolean flag = false;
  
      public void test() {
          //...
          final Object obj = new Object();
          Thread t1 = new Thread(() -> {
              synchronized (obj) {
                  try {
                      Thread.sleep(3000);
                      System.out.println("--- 休眠 3 秒");
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  } finally {
                      flag = true;
                  }
                  obj.notify();
              }
          });
          t1.start();
  
          synchronized (obj) {
              while (!flag) {
                  try {
                      System.out.println("---while-loop---");
                      Thread.sleep(500);
                      obj.wait();
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
          System.out.println("--- work thread run");
  
      }
  ```

  结果证明，优化后的程序，循环只执行了一次。

## 理解 wait / notify / notifyAll

在Java中，每个对象都有两个池，锁(monitor)池和等待池

### 锁池

锁池: 假设线程A已经拥有了某个对象的锁，而其它的线程想要调用这个对象的某个synchronized方法(或者synchronized块)，由于这些线程在进入对象的synchronized方法之前必须先获得该对象的锁的拥有权，但是该对象的锁目前正被线程A拥有，所以这些线程就进入了该对象的锁池中。

### 等待池

等待池: 假设一个线程A调用了某个对象的wait()方法，线程A就会释放该对象的锁(因为wait()方法必须出现在synchronized中，这样自然在执行wait()方法之前线程A就已经拥有了该对象的锁)，同时线程A就进入到了该对象的等待池中。如果另外的一个线程调用了相同对象的notifyAll()方法，那么处于该对象的等待池中的线程就会全部进入该对象的锁池中，准备争夺锁的拥有权。如果另外的一个线程调用了相同对象的notify()方法，那么仅仅有一个处于该对象的等待池中的线程(随机)会进入该对象的锁池.

### wait 和 notify/notifyAll 的区别

#### wait()

`public final void wait() throws InterruptedException,IllegalMonitorStateException`
该方法用来将当前线程置入休眠状态，直到接到通知或被中断为止。在调用 wait()之前，线程必须要获得该对象的对象级别锁，即只能在同步方法或同步块中调用 wait()方法。进入 wait()方法后，当前线程释放锁。在从 wait()返回前，线程与其他线程竞争重新获得锁。如果调用 wait()时，没有持有适当的锁，则抛出 IllegalMonitorStateException，它是 RuntimeException 的一个子类，因此，不需要 try-catch 

#### notify()

`public final native void notify() throws IllegalMonitorStateException`
该方法也要在同步方法或同步块中调用，即在调用前，线程也必须要获得该对象的对象级别锁，的如果调用 notify()时没有持有适当的锁，也会抛出 IllegalMonitorStateException。
该方法用来通知那些可能等待该对象的对象锁的其他线程。如果有多个线程等待，则线程规划器任意挑选出其中一个 wait()状态的线程来发出通知，并使它等待获取该对象的对象锁（notify 后，当前线程不会马上释放该对象锁，wait 所在的线程并不能马上获取该对象锁，要等到程序退出 synchronized 代码块后，当前线程才会释放锁，wait所在的线程也才可以获取该对象锁），但不惊动其他同样在等待被该对象notify的线程们。当第一个获得了该对象锁的 wait 线程运行完毕以后，它会释放掉该对象锁，此时如果该对象没有再次使用 notify 语句，则即便该对象已经空闲，其他 wait 状态等待的线程由于没有得到该对象的通知，会继续阻塞在 wait 状态，直到这个对象发出一个 notify 或 notifyAll。这里需要注意：它们等待的是被 notify 或 notifyAll，而不是锁。这与下面的 notifyAll()方法执行后的情况不同。

#### notifyAll()

`public final native void notifyAll() throws IllegalMonitorStateException`
该方法与 notify ()方法的工作方式相同，重要的一点差异是：
notifyAll 使所有原来在该对象上 wait 的线程统统退出 wait 的状态（即全部被唤醒，不再等待 notify 或 notifyAll，但由于此时还没有获取到该对象锁，因此还不能继续往下执行），变成等待获取该对象上的锁，一旦该对象锁被释放（notifyAll 线程退出调用了 notifyAll 的 synchronized 代码块的时候），他们就会去竞争。如果其中一个线程获得了该对象锁，它就会继续往下执行，在它退出 synchronized 代码块，释放锁后，其他的已经被唤醒的线程将会继续竞争获取该锁，一直进行下去，直到所有被唤醒的线程都执行完毕。

## 生产者与消费者模式

生产者与消费者问题是并发编程里面的经典问题。接下来说说利用wait()和notify()来实现生产者和消费者并发问题：
显然要保证生产者和消费者并发运行不出乱，主要要解决：当生产者线程的缓存区为满的时候，就应该调用wait()来停止生产者继续生产，而当生产者满的缓冲区被消费者消费掉一块时，则应该调用notify()唤醒生产者，通知他可以继续生产；同样，对于消费者，当消费者线程的缓存区为空的时候，就应该调用wait()停掉消费者线程继续消费，而当生产者又生产了一个时就应该调用notify()来唤醒消费者线程通知他可以继续消费了。

```java
public class Test {
    public static void main(String[] args) {
        Reposity reposity = new Reposity(600);
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for(int i = 0; i < 10; i++){
            threadPool.submit(new Producer(reposity));
        }

        for(int i = 0; i < 10; i++){
            threadPool.submit(new Consumer(reposity));
        }
        threadPool.shutdown();
    }
}


class Reposity {
    private static final int MAX_NUM = 2000;
    private int currentNum;

    private final Object obj = new Object();

    public Reposity(int currentNum) {
        this.currentNum = currentNum;
    }

    public void in(int inNum) {
        synchronized (obj) {
            while (currentNum + inNum > MAX_NUM) {
                try {
                    System.out.println("入货量 " + inNum + " 线程 " + Thread.currentThread().getId() + "被挂起...");
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentNum += inNum;
            System.out.println("线程： " + Thread.currentThread().getId() + ",入货：inNum = [" + inNum + "], currentNum = [" + currentNum + "]");
            obj.notifyAll();
        }
    }

    public void out(int outNum) {
        synchronized (obj) {
            while (currentNum < outNum) {
                try {
                    System.out.println("出货量 " + outNum + " 线程 " + Thread.currentThread().getId() + "被挂起...");
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentNum -= outNum;
            System.out.println("线程： " + Thread.currentThread().getId() + ",出货：outNum = [" + outNum + "], currentNum = [" + currentNum + "]");
            obj.notifyAll();
        }
    }
}

class Producer implements Runnable {
    private Reposity reposity;

    public Producer(Reposity reposity) {
        this.reposity = reposity;
    }

    @Override
    public void run() {
        reposity.in(200);
    }
}

class Consumer implements Runnable {
    private Reposity reposity;

    public Consumer(Reposity reposity) {
        this.reposity = reposity;
    }

    @Override
    public void run() {
        reposity.out(200);
    }
}
```



## 总结

最后做几点总结：

1. 调用wait方法和notify、notifyAll方法前必须获得对象锁，也就是必须写在`synchronized(锁对象){......}代码块`中。
2. 当线程调用了wait()方法后就释放了对象锁，否则其他线程无法获得对象锁。
3. 当调用 wait() 方法后，线程必须再次获得对象锁后才能继续执行。
4. 如果另外两个线程都在 wait，则正在执行的线程调用notify方法只能唤醒一个正在wait的线程（公平竞争，由JVM决定）。
5. 当使用`notifyAll`方法后，所有wait状态的线程都会被唤醒，但是只有一个线程能获得锁对象，必须执行完`while(condition){this.wait();}`后才释放对象锁。其余的需要等待该获得对象锁的线程执行完释放对象锁后才能继续执行。
6. 当某个线程调用`notifyAll`方法后，虽然其他线程被唤醒了，但是该线程依然持有着对象锁，必须等该同步代码块执行完（右大括号结束）后才算正式释放了锁对象，另外两个线程才有机会执行。
7. 第5点中说明， `wait 方法的调用前的条件判断需放在循环中`，否则可能出现逻辑错误。另外，根据程序逻辑合理使用 wait 即 notify 方法，避免如先执行 notify ，后执行 wait 方法，线程一直挂起之类的错误。

## Java并发---内置锁

举例单例模式：

```java
public class Singleton {
    private static Singleton mSingleton;
    public static Singleton getInstance() {
        if (mSingleton == null) {
            mSingleton = new Singleton();
        }
        return mSingleton;
    }
    private Singleton() {

    }
}
```

该方法在单线程中运行是没有问题的，但是在多线程中，某些情况下，多个线程同时都判断到 `sSingleton == null`，然后又都执行 `sSingleton = new Singleton()`，这样就不能保证单例了，我们说它不是线程安全的。

改进：

```java
public class Singleton {
    private static Singleton mSingleton;
    public synchronized static Singleton getInstance() {
        if (mSingleton == null) {
            mSingleton = new Singleton();
        }
        return mSingleton;
    }
    private Singleton() {

    }
}
```

上面这种实现，实际上效率非常低，是完全不推荐使用的。主要是因为加了 `sychronized` 关键字，意为同步的，也就是内置锁。使用 `synchronized` 关键字修饰方法， 是对该方法加锁，这样在同一时刻，只有一个线程能进入该方法，这样保证了线程安全，但是也正因为如此，效率变得很低，**因为当对象创建之后，再次调用该方法的时候，直接使用对象就可以了，无需再同步了。**于是有了下面改进的实现方式—— DCL（双重检查锁）：

```java
public class Singleton {
    private static volatile Singleton mSingleton;
    public static Singleton getInstance() {
        if (mSingleton == null) {
            synchronized (Singleton.class) {
                if (mSingleton == null) {
                    mSingleton = new Singleton();
                }
            }
        }
        return mSingleton;
    }
    private Singleton() {

    }
}
```

`sSingleton = new Singleton() ` 不是一个原子操作。故须加 `volatile` 关键字修饰，该关键字在 jdk1.5 之后版本才有。下面就来说说 `synchronized` 和 `volatile` 这两个关键字。

### 同步与`synchronized`

#### synchronized 锁代码块

**每个 java 对象**都可以作为实现同步的锁，**java 的内置锁也称为互斥锁**。同一时刻只能有一个线程获得该锁，获得该锁的线程才能进入由锁保护的代码块，其它线程只能等待该线程执行完代码块之后，释放该锁后，再去获得该锁。例子：

```java
synchronized (lock) {
// 由锁保护的代码块
}
```

```java
public class SynchronizedDemo1 {
private Object lock = new Object();
```

##### 以关键字 `synchronized` 修饰的方法就是一种横跨整个方法的同步代码块，其中该同步代码块的**锁就是调用该方法的对象**。

```java
class A {
  public synchronized void a(){
  	System.out.println("hello");
	}
}
// 等价于
class A {
	public void a(){
		synchronized(this) {
			System.out.println("hello");
		}
	}
}
```

#####  静态方法用 `类名.方法名` 来调用，以关键字 `synchronized` 修饰的静态方法则以 `Class 对象`作为锁。

```java
class A {
	public static synchronized void a(){
		System.out.println("hello");
	}
}
// 等价于
class A {
	public static void a(){
    synchronized(A.class)
		System.out.println("hello");
	}
}
```

**对于某个类的某个特定对象来说，该类中，所有 synchronized 修饰的非静态方法共享同一个锁，当在对象上调用其任意 synchronized 方法时，此对象都被加锁，此时，其他线程调用该对象上任意的 synchronized 方法只有等到前一个线程方法调用完毕并释放了锁之后才能被调用。 而对于一个类中，所有 synchronized 修饰的静态方法共享同一个锁。**

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SynchronizedDemo1 {
    private Object lock = new Object();

    public static void main(String[] args) {
        SynchronizedDemo1 demo1 = new SynchronizedDemo1();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("子线程：" + Thread.currentThread().getId());
                    demo1.test1();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SynchronizedDemo1.test4();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SynchronizedDemo1.test5();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    demo1.test2();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    demo1.test3();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        try {
            System.out.println("主线程：" + Thread.currentThread().getId());
            demo1.test1();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void test1() throws InterruptedException {
        System.out.println("--- test1 begin - current thread: " + Thread.currentThread().getId());
        Thread.sleep(1000);
        synchronized (lock)
        {
            System.out.println("--- test1 synchronized - current thread: " + Thread.currentThread().getId());
            Thread.sleep(5000);
        }
        System.out.println("--- test1 end - current thread: " + Thread.currentThread().getId());
    }

    public synchronized void test2() throws InterruptedException {
        System.out.println("--- begin 2 - current Thread " + Thread.currentThread().getId());
        Thread.sleep(8000);
        System.out.println("--- end 2 - current Thread " + Thread.currentThread().getId());
    }

    public synchronized void test3() throws InterruptedException {
        System.out.println("--- begin 3 - current Thread " + Thread.currentThread().getId());
        Thread.sleep(8000);
        System.out.println("--- end 3 - current Thread " + Thread.currentThread().getId());
    }

    // static Method
    public synchronized static void test4() throws InterruptedException {
        System.out.println("--- begin 4 - current Thread " + Thread.currentThread().getId());
        Thread.sleep(5000);
        System.out.println("--- end 4 - current Thread " + Thread.currentThread().getId());
    }

    // static Method
    public synchronized static void test5() throws InterruptedException {
        System.out.println("--- begin 5 - current Thread " + Thread.currentThread().getId());
        Thread.sleep(5000);
        System.out.println("--- end 5 - current Thread " + Thread.currentThread().getId());
    }
}
```

#### 内置锁是可重入锁

当某个线程请求一个由其他线程持有的锁时，发出请求的线程就会阻塞，然而，内置锁是可重入的，如果某个线程试图获得一个已经由它自己持有的锁，那么这个请求就会成功。**这也意味着获取锁的操作粒度是“线程”，而不是“调用”。**当线程请求一个未被持有的锁时，jvm 将记下锁的持有者（即哪个线程），并获取该锁的计数值为 1 ，当同一个线程再次获取该锁时， jvm 将计数值递增，当线程退出同步代码块时，计数值将递减，当计数值为 0 时，将释放锁。

```java
public class B {
	public static void main(String[] args) {
		B obj = new B();
		obj.b();
}
  
private synchronized void a(){
    System.out.println("---a");
}

private synchronized void b(){
    System.out.println("---b");
  	// 内部调用同步方法a
    a();
}
  
  //输出：
  	---b
		---a
```

假设没有可重入的锁，对于对象 `obj` 来说，调用 b 方法时，线程将会持有 `obj` 这个锁，在方法 b 中调用方法 a 时，将会一直等待方法 b 释放锁，造成**死锁**的情况。

```java
public class SynchronizedDemo2 extends SynchronizedDemo1 {
    @Override
    public synchronized void test2() throws InterruptedException {
        System.out.println("--- SynchronizedDemo2 begin 2 - current Thread " + Thread.currentThread().getId());
        Thread.sleep(5000);
        System.out.println("--- SynchronizedDemo2 end 2 - current Thread " + Thread.currentThread().getId());
        super.test2();
    }

    public static void main(String[] args) {
        SynchronizedDemo2 synchronizedDemo2 = new SynchronizedDemo2();
        try {
            synchronizedDemo2.test2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### 同步关键字`volatile`

#### 原子性

原子是世界上的最小单位，具有不可分割性。 比如 `a=0` 这个操作不可分割，我们说这是一个原子操作。而     `++i` 就不是一个原子操作。它包含了"读取-修改-写入"的操作。

 同步代码块，可以视作是一个原子操作。Java从JDK 1.5开始提供了java.util.concurrent.atomic包，这个包中的原子操作类提供了一种用法简单、性能高效、线程安全地更新一个变量的方式。比如：`AtomicBoolean` `AtomicInteger` `AtomicLong` 等原子操作类。具体可查阅JDK源码或者参考《ava并发编程的艺术》第7章。

下面说说复合操作与线程安全的问题。我们知道，java 集合框架中的 Vector 类是线程安全的，查看该类的源码发现，很多关键方法都用了`synchronized` 加以修饰。但是实际使用时候，稍有不慎，你会发现，它可能并不是线程安全的。比如在某个类中拓展一下 Vector 的方法，往 vector 中添加一个元素时，先判断该元素是否存在，如果不存在才添加,该方法大概像下面这样：

```java
public class CJUtil{
  public void putElement(Vector vector, E x){
    boolean has = vector.contains(x);
      if(!has){
      vector.add(x);
    }
  }
}
```

上面这个代码肯定是线程不安全的，但是为什么呢？不是说好，Vector 类是线程安全的吗？上网搜了一下，居然发现关于 Vector 类是不是线程安全的存在争议，然后我看到有人说它不是线程安全的，给出的理由比如像上面这种先判断再添加，或者先判断再删除，是一种复合操作，然后认真地打开 JDK 的源码看了，发现 Vector 类中 `contains` 方法并没有用 `synchronized` 修饰，然后得出了结论,Vector不是线程安全的...   

 事实到底是怎样的呢？我们假设 Vector 类的 `contains` 也用 `synchronized` 关键字加锁同步了，此时有两个线程 tA 和 tB 同时访问这个方法，tA 调用到 `contains` 方法的时候，tB 阻塞， tA 执行完 `contains` 方法，返回 false 后，释放了锁，在 tA 执行 `add` 之前，tB 抢到了锁，执行了 `contains` 方法，tA 阻塞。对于同一个元素， tb 判断也不包含，后面， tA 和 tB 都向 Vector 添加了这个元素。经过分析，我们发现，对于上述复合操作线程不安全的原因，并非是其中单个操作没有加锁同步造成的。   

 那如何解决这个问题呢？可能马上会想到，给 putElement 方法加上 `synchronized` 同步。

```java
public class CJUtil{
  public synchronized void putElement(Vector vector, E x){
    boolean has = vector.contains(x);
      if(!has){
      vector.add(x);
    }
  }
}
```

​		 这样整个方法视为一个原子操作，只有当 tA 执行完整个方法后，tB 才能进入，也就不存在上面说的问题了。其实，这只是假象。这种在加锁的方法，并不能保证线程安全。我们可以从两个方面来分析一下：         

1. 从上文我们知道，给方法加锁，**锁对象是调用该方法的对象**。这和我们操作 Vector 方法的锁并不是同一个锁。我们虽然保证了只有一个线程能够进入到 `putElement` 方法去操作 vector，但是我们没法保证其它线程通过其它方法不去操作这个 vector 。 

2. 上一条中，只有一个线程能够进入到 `putElement` 方法，是不准确的，因为这个方法不是静态的，如果在两个线程中，分别用 `CJUtil` 的两个不同的实例对象，是可以同时进入到 `putElement` 方法的。 

正确的做法应该是：

```java
public class CJUtil{
  public void putElement(Vector vector, E x){
    synchronized(vector) {
      boolean has = vector.contains(x);
      if(!has){
      	vector.add(x);
    	}
    }
  }
}
```

#### 重排序

重排序通常是编译器或运行时环境为了优化程序性能而采取的对指令进行重新排序执行的一种手段。重排序分为两类：编译器重排序和运行期重排序，分别对应编译时和运行时环境。

 不要假设指令执行的顺序，因为根本无法预知不同线程之间的指令会以何种顺序执行。

 编译器重排序的典型就是通过调整指令顺序，在不改变程序语义的前提下，尽可能的减少寄存器的读取、存储次数，充分复用寄存器的存储值。

`int a = 5;①`   `int b = 10;②`    `int c = a + 1;③`  假设用的同一个寄存器 这三条语句，如果按照顺序一致性，执行顺序为①②③寄存器要被读写三次；但为了降低重复读写的开销，编译器会交换第二和第三的位置，即执行顺序为①③②

#### 可见性

可见性是一种复杂的属性，因为可见性中的错误总是会违背我们的直觉。通常，我们无法确保执行读操作的线程能适时地看到其他线程写入的值，有时甚至是根本不可能的事情。为了确保多个线程之间对内存写入操作的可见性，必须使用同步机制。

可见性，是指线程之间的可见性，一个线程修改的状态对另一个线程是可见的。也就是一个线程修改的结果。另一个线程马上就能看到。比如：用`volatile`修饰的变量，就会具有可见性。`volatile`修饰的变量**不允许线程内部缓存和重排序，即直接修改内存**。所以对其他线程是可见的。但是这里需要注意一个问题，`volatile`只能让被他修饰内容具有可见性，但不能保证它具有原子性。

```java
private volatile static boolean flag = false;
new Thread() {
            @Override
            public void run() {
                while (!flag) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("work");
                }
            }
        }.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        flag = true;
```

结果表明，没有用  `volatile` 修饰 `flag` 之前，改变了不具有可见性，一个线程将它的值改变后，另一个线程却 “不知道”，所以程序没有退出。当把变量声明为 `volatile` 类型后，编译器与运行时都会注意到这个变量是共享的，因此不会将该变量上的操作与其他内存操作一起重排序。`volatile` 变量不会被缓存在寄存器或者对其他处理器不可见的地方，因此在读取volatile类型的变量时总会返回最新写入的值。 

在访问`volatile`变量时不会执行加锁操作，因此也就不会使执行线程阻塞，因此`volatile`变量是一种比`sychronized`关键字更轻量级的同步机制。

当对非 volatile 变量进行读写的时候，每个线程先从内存拷贝变量到CPU缓存中。如果计算机有多个CPU，每个线程可能在不同的CPU上被处理，这意味着每个线程可以拷贝到不同的 CPU cache 中。 

而声明变量是 `volatile `的，JVM 保证了每次读变量都从内存中读，跳过 CPU cache 这一步。

`volatile` 修饰的遍历具有如下特性：            

1. 保证此变量对所有的线程的可见性，当一个线程修改了这个变量的值，`volatile `保证了新值能立即同步到主内存，以及每次使用前立即从主内存刷新。但普通变量做不到这点，普通变量的值在线程间传递均需要通过主内存（详见：Java内存模型）来完成。 
2. 禁止指令重排序优化。 
3. 不会阻塞线程。

#### synchronized 与可见性

细心的人应该发现了，上面代码中的循环是一个空循环，我试着去掉 `volatile` 关键字，在循环里面加了一条打印信息，如下：

`System.out.println("---");`

结果同步了！

看一下：

```java
public void println(String x) {
    synchronized (this) {
        print(x);
        newLine();
    }
}
```

我们发现，该方法加锁同步了。 那么问题来了，`synchronized` 到底干了什么。。 按理说，`synchronized `只会保证该同步块中的变量的可见性，发生变化后立即同步到主存，但是，flag 变量并不在同步块中，实际上，JVM对于现代的机器做了最大程度的优化，也就是说，最大程度的保障了线程和主存之间的及时的同步，也就是相当于虚拟机尽可能的帮我们加了个`volatile`，但是，当CPU被一直占用的时候，同步就会出现不及时，也就出现了后台线程一直不结束的情况。

## Lock

### synchronized的缺陷

那么如果这个获取锁的线程由于要等待IO或者其他原因（比如调用sleep方法）被阻塞了，但是又没有释放锁，其他线程便只能干巴巴地等待，试想一下，这多么影响程序执行效率。

因此就需要有一种机制可以不让等待的线程一直无期限地等待下去（比如只等待一定的时间或者能够响应中断），通过Lock就可以办到。

再举个例子：当有多个线程读写文件时，读操作和写操作会发生冲突现象，写操作和写操作会发生冲突现象，但是读操作和读操作不会发生冲突现象。

但是采用`synchronized`关键字来实现同步的话，就会导致一个问题：

**如果多个线程都只是进行读操作，所以当一个线程在进行读操作时，其他线程只能等待无法进行读操作。**

因此就需要一种机制来使得多个线程都只是进行读操作时，线程之间不会发生冲突，通过Lock就可以办到。

另外，通过Lock可以知道线程有没有成功获取到锁。这个是synchronized无法办到的。

总结一下，也就是说Lock提供了比synchronized更多的功能。但是要注意以下几点：

　　1）Lock不是Java语言内置的，`synchronized`是Java语言的关键字，因此是内置特性。Lock是一个类，通过这个类可以实现同步访问；

　　2）Lock和synchronized有一点非常大的不同，采用`synchronized`不需要用户去手动释放锁，当`synchronized`方法或者`synchronized`代码块执行完之后，系统会自动让线程释放对锁的占用；**而Lock则必须要用户去手动释放锁，如果没有主动释放锁，就有可能导致出现死锁现象。**

#### 1. Lock

首先要说明的就是Lock，通过查看Lock的源码可知，Lock是一个接口：

```java
public interface Lock {
    void lock();
    void lockInterruptibly() throws InterruptedException;
    boolean tryLock();
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
    void unlock();
    Condition newCondition();
}
```

首先`lock()`方法是平常使用得最多的一个方法，就是用来获取锁。如果锁已被其他线程获取，则进行等待。

由于在前面讲到如果采用Lock，必须主动去释放锁，并且在发生异常时，不会自动释放锁。因此一般来说，使用Lock必须在`try{}catch{}块`中进行，并且将释放锁的操作放在`finally块`中进行，以保证锁一定被被释放，防止死锁的发生。通常使用Lock来进行同步的话，是以下面这种形式去使用的：

```java
Lock lock = ...;
lock.lock();
try{
    //处理任务
}catch(Exception ex){
     
}finally{
    lock.unlock();   //释放锁
}
```

`tryLock()`方法是有返回值的，它表示用来尝试获取锁，如果获取成功，则返回`true`，如果获取失败（即锁已被其他线程获取），则返回`false`，也就说这个方法无论如何都会立即返回。在拿不到锁时不会一直在那等待。

```java
Lock lock = ...;
if(lock.tryLock()) {
     try{
         //处理任务
     }catch(Exception ex){
         
     }finally{
         lock.unlock();   //释放锁
     } 
}else {
    //如果不能获取锁，则直接做其他事情
}
```

`lockInterruptibly()`方法比较特殊，当通过这个方法去获取锁时，如果线程正在等待获取锁，则这个线程能够响应中断，即中断线程的等待状态。也就使说，当两个线程同时通过`lock.lockInterruptibly()`想获取某个锁时，假若此时线程A获取到了锁，而线程B只有在等待，那么对线程B调用`threadB.interrupt()`方法能够中断线程B的等待过程。

由于`lockInterruptibly()`的声明中抛出了异常，所以`lock.lockInterruptibly()`必须放在try块中或者在调用`lockInterruptibly()`的方法外声明抛出`InterruptedException`。

因此`lockInterruptibly()`一般的使用形式如下：

```java
public void method() throws InterruptedException {
    lock.lockInterruptibly();
    try {  
     //.....
    }
    finally {
        lock.unlock();
    }  
}
```

注意，当一个线程获取了锁之后，是不会被`interrupt()`方法中断的。因为本身在前面的文章中讲过单独调用`interrupt()`方法不能中断正在运行过程中的线程，只能中断阻塞过程中的线程。

因此当通过`lockInterruptibly()`方法获取某个锁时，如果不能获取到，只有进行等待的情况下，是可以响应中断的。

而用`synchronized`修饰的话，当一个线程处于等待某个锁的状态，是无法被中断的，只有一直等待下去。

#### 2. ReentrantLock

`ReentrantLock`，意思是“可重入锁”，关于可重入锁的概念在下一节讲述。`ReentrantLock`是唯一实现了`Lock`接口的类，并且`ReentrantLock`提供了更多的方法。下面通过一些实例看具体看一下如何使用`ReentrantLock`。

```java
public class Test {
    private ArrayList<Integer> arrayList = new ArrayList<Integer>();
    public static void main(String[] args)  {
        final Test test = new Test();
         
        new Thread(){
            public void run() {
                test.insert(Thread.currentThread());
            };
        }.start();
         
        new Thread(){
            public void run() {
                test.insert(Thread.currentThread());
            };
        }.start();
    }  
     
    public void insert(Thread thread) {
        Lock lock = new ReentrantLock();    //注意这个地方
        lock.lock();
        try {
            System.out.println(thread.getName()+"得到了锁");
            for(int i=0;i<5;i++) {
                arrayList.add(i);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }finally {
            System.out.println(thread.getName()+"释放了锁");
            lock.unlock();
        }
    }
}
```

输出结果：

```java
Thread-0得到了锁
Thread-1得到了锁
Thread-0释放了锁
Thread-1释放了锁
```

也许有朋友会问，怎么会输出这个结果？第二个线程怎么会在第一个线程释放锁之前得到了锁？原因在于，在insert方法中的lock变量是局部变量，每个线程执行该方法时都会保存一个副本，那么理所当然每个线程执行到`lock.lock()`处获取的是不同的锁，所以就不会发生冲突。

知道了原因改起来就比较容易了，**只需要将lock声明为类的属性即可**。

`lockInterruptibly()`响应中断的使用方法：

```java
public class Test {
    private Lock lock = new ReentrantLock();   
    public static void main(String[] args)  {
        Test test = new Test();
        MyThread thread1 = new MyThread(test);
        MyThread thread2 = new MyThread(test);
        thread1.start();
        thread2.start();
         
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread2.interrupt();
    }  
     
    public void insert(Thread thread) throws InterruptedException{
        lock.lockInterruptibly();   //注意，如果需要正确中断等待锁的线程，必须将获取锁放在外面，然后将InterruptedException抛出
        try {  
            System.out.println(thread.getName()+"得到了锁");
            long startTime = System.currentTimeMillis();
            for(    ;     ;) {
                if(System.currentTimeMillis() - startTime >= Integer.MAX_VALUE)
                    break;
                //插入数据
            }
        }
        finally {
            System.out.println(Thread.currentThread().getName()+"执行finally");
            lock.unlock();
            System.out.println(thread.getName()+"释放了锁");
        }  
    }
}
 
class MyThread extends Thread {
    private Test test = null;
    public MyThread(Test test) {
        this.test = test;
    }
    @Override
    public void run() {
         
        try {
            test.insert(Thread.currentThread());
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+"被中断");
        }
    }
}
```

运行之后，发现thread2能够被正确中断。

#### 3.ReadWriteLock

`ReadWriteLock`也是一个接口，在它里面只定义了两个方法：

```java
public interface ReadWriteLock {
    /**
     * Returns the lock used for reading.
     *
     * @return the lock used for reading.
     */
    Lock readLock();
 
    /**
     * Returns the lock used for writing.
     *
     * @return the lock used for writing.
     */
    Lock writeLock();
}
```

一个用来获取读锁，一个用来获取写锁。也就是说将文件的读写操作分开，分成2个锁来分配给线程，从而使得多个线程可以同时进行读操作。下面的`ReentrantReadWriteLock`实现了`ReadWriteLock`接口。

#### 4.ReentrantReadWriteLock

`ReentrantReadWriteLock`里面提供了很多丰富的方法，不过最主要的有两个方法：`readLock()`和`writeLock()`用来获取读锁和写锁。

下面通过几个例子来看一下`ReentrantReadWriteLock`具体用法。

假如有多个线程要同时进行读操作的话，先看一下synchronized达到的效果：

```java
public class Test {
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
     
    public static void main(String[] args)  {
        final Test test = new Test();
         
        new Thread(){
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();
         
        new Thread(){
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();
         
    }  
     
    public synchronized void get(Thread thread) {
        long start = System.currentTimeMillis();
        while(System.currentTimeMillis() - start <= 1) {
            System.out.println(thread.getName()+"正在进行读操作");
        }
        System.out.println(thread.getName()+"读操作完毕");
    }
}
```

这段程序的输出结果会是，直到thread1执行完读操作之后，才会打印thread2执行读操作的信息。

```java
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0正在进行读操作
Thread-0读操作完毕
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1正在进行读操作
Thread-1读操作完毕
```

而改成用读写锁的话：

```java
public class Test {
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
     
    public static void main(String[] args)  {
        final Test test = new Test();
         
        new Thread(){
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();
         
        new Thread(){
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();
         
    }  
     
    public void get(Thread thread) {
        rwl.readLock().lock();
        try {
            long start = System.currentTimeMillis();
             
            while(System.currentTimeMillis() - start <= 1) {
                System.out.println(thread.getName()+"正在进行读操作");
            }
            System.out.println(thread.getName()+"读操作完毕");
        } finally {
            rwl.readLock().unlock();
        }
    }
}
```

```java
Thread-1正在进行读操作
Thread-0正在进行读操作
Thread-1正在进行读操作
Thread-0正在进行读操作
Thread-1正在进行读操作
Thread-0正在进行读操作
Thread-1正在进行读操作
Thread-0读操作完毕
Thread-1读操作完毕
```

说明thread1和thread2在同时进行读操作。

这样就大大提升了读操作的效率。

不过要注意的是，如果有一个线程已经占用了读锁，则此时其他线程如果要申请写锁，则申请写锁的线程会一直等待释放读锁。

如果有一个线程已经占用了写锁，则此时其他线程如果申请写锁或者读锁，则申请的线程会一直等待释放写锁。

关于ReentrantReadWriteLock类中的其他方法感兴趣的朋友可以自行查阅API文档。

#### 5.Lock和synchronized的选择

总结来说，Lock和synchronized有以下几点不同：

　　1）Lock是一个接口，而synchronized是Java中的关键字，synchronized是内置的语言实现；

　　2）synchronized在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而Lock在发生异常时，如果没有主动通过unLock()去释放锁，则很可能造成死锁现象，因此使用Lock时需要在finally块中释放锁；

　　3）Lock可以让等待锁的线程响应中断，而synchronized却不行，使用synchronized时，等待的线程会一直等待下去，不能够响应中断；

　　4）通过Lock可以知道有没有成功获取锁，而synchronized却无法办到。

　　5）Lock可以提高多个线程进行读操作的效率。

在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，而当竞争资源非常激烈时（即有大量线程同时竞争），此时Lock的性能要远远优于synchronized。所以说，在具体使用时要根据适当情况选择。

### 锁的相关概念

#### 1.可重入锁

如果锁具备可重入性，则称作为可重入锁。像`synchronized`和`ReentrantLock`都是可重入锁，可重入性在我看来实际上表明了锁的分配机制：**基于线程的分配，而不是基于方法调用的分配。**举个简单的例子，当一个线程执行到某个`synchronized`方法时，比如说method1，而在method1中会调用另外一个`synchronized`方法method2，此时线程不必重新去申请锁，而是可以直接执行方法method2。

```java
class MyClass {
    public synchronized void method1() {
        method2();
    }
     
    public synchronized void method2() {
         
    }
}
```

上述代码中的两个方法method1和method2都用synchronized修饰了，假如某一时刻，线程A执行到了method1，此时线程A获取了这个对象的锁，而由于method2也是synchronized方法，假如synchronized不具备可重入性，此时线程A需要重新申请锁。但是这就会造成一个问题，因为线程A已经持有了该对象的锁，而又在申请获取该对象的锁，这样就会线程A一直等待永远不会获取到的锁。

而由于synchronized和Lock都具备可重入性，所以不会发生上述现象。

#### 2.可中断锁

可中断锁：顾名思义，就是可以相应中断的锁。

在Java中，synchronized就不是可中断锁，而Lock是可中断锁。

如果某一线程A正在执行锁中的代码，另一线程B正在等待获取该锁，可能由于等待时间过长，线程B不想等待了，想先处理其他事情，我们可以让它中断自己或者在别的线程中中断它，这种就是可中断锁。

在前面演示lockInterruptibly()的用法时已经体现了Lock的可中断性。

#### 3.公平锁

公平锁即尽量以请求锁的顺序来获取锁。比如同是有多个线程在等待一个锁，当这个锁被释放时，**等待时间最久的线程（最先请求的线程）会获得该所，这种就是公平锁**。

在Java中，**synchronized就是非公平锁**，它无法保证等待的线程获取锁的顺序。

而对于`ReentrantLock`和`ReentrantReadWriteLock`，它**默认情况下是非公平锁**，但是可以设置为公平锁。

在ReentrantLock中定义了2个静态内部类，一个是NotFairSync，一个是FairSync，分别用来实现非公平锁和公平锁。

我们可以在创建ReentrantLock对象时，通过以下方式来设置锁的公平性：
`ReentrantLock lock = new ReentrantLock(true);`
如果参数为true表示为公平锁，为fasle为非公平锁。默认情况下，如果使用无参构造器，则是非公平锁。

另外在ReentrantLock类中定义了很多方法，比如：

- `isFair() `//判断锁是否是公平锁
- `isLocked()` //判断锁是否被任何线程获取了
- `isHeldByCurrentThread()` //判断锁是否被当前线程获取了
- `hasQueuedThreads()` //判断是否有线程在等待该锁
- 在`ReentrantReadWriteLock`中也有类似的方法，同样也可以设置为公平锁和非公平锁。不过要记住，`ReentrantReadWriteLock`**并未实现Lock接口，它实现的是ReadWriteLock接口。**

#### 4.读写锁

读写锁将对一个资源（比如文件）的访问分成了2个锁，一个读锁和一个写锁。

正因为有了读写锁，才使得多个线程之间的读操作不会发生冲突。

ReadWriteLock就是读写锁，它是一个接口，ReentrantReadWriteLock实现了这个接口。

可以通过readLock()获取读锁，通过writeLock()获取写锁。

```java
private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
rwl.readLock().lock();
```

参考：https://www.cnblogs.com/joy99/p/6116855.html