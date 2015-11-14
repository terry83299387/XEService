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
public class TestMultiCore implements Runnable {
	
	public void run() {
		// waiting for setting processor relations
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("running thread: " + Thread.currentThread().getName());
		while (true) {
			;
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new Thread(new TestMultiCore()).start();
		new Thread(new TestMultiCore()).start();
		new Thread(new TestMultiCore()).start();
		new Thread(new TestMultiCore()).start();
	}
}




