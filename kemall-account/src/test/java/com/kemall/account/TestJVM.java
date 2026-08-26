package com.kemall.account;

import java.util.ArrayList;
import java.util.List;

public class TestJVM {

    // 创建一个静态 List，作为 GC Root，让对象永远被引用（无法回收）
    private static List<byte[]> leakList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("程序启动，当前堆大小: 1024MB");
        System.out.println("开始疯狂创建对象，直到 OOM...");

        // 每次分配 1MB 的 byte 数组
        int chunkSize = 1024 * 1024; // 1MB

        try {
            while (true) {
                // 创建一个 1MB 的 byte 数组，并放入 List（强引用，永不释放）
                byte[] chunk = new byte[chunkSize];
                leakList.add(chunk);

                // 每 100 次打印一次内存使用情况
                if (leakList.size() % 100 == 0) {
                    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    System.out.printf("已分配 %d 个 1MB 块，当前堆使用: %.2f MB%n",
                            leakList.size(), usedMemory / (1024.0 * 1024.0));
                }

                // 稍微慢一点，方便你观察日志（每秒分配 100MB）
                Thread.sleep(10);
            }
        } catch (OutOfMemoryError e) {
            System.err.println("================== 触发 OOM ==================");
            System.err.println("已成功分配 " + leakList.size() + " 个 1MB 块");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            // 让程序暂停，方便你查看 GC 日志
            Thread.sleep(30000);
        }
    }
}