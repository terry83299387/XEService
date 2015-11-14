/**
 * 
 */
package qmk;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.text.*;
import java.net.*;
import java.lang.reflect.*;

/**
 * @author QiaoMingkui
 * 
 */
public class TestSync {
	static class A implements Runnable {
		@Override
		public void run() {
			System.out.println("waiting");
			synchronized (this) {
			try {
				this.wait();
				System.out.println("wake up");
				System.out.println("sleep again");
				Thread.sleep(1000);
				System.out.println("wake up again");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
		}

		public void sayHello() {
			System.out.println("hello");
		}

		public synchronized void sayHelloSync() {
			System.out.println("Synchronized hello");
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		A a = new A();
		Thread t = new Thread(a);
		t.start();
		Thread.sleep(1000);
		a.sayHello();
		a.sayHelloSync();
		Thread.sleep(2000);
		synchronized(a) {
		a.notify();
		System.out.println("after notify in main");
		}
	}
}




