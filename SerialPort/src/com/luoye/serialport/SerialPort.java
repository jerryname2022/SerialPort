/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.luoye.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Build;
import android.util.Log;

/**
 * 串口类
 * 
 * @author LUOYE
 * @data 2015-07-05 11:03:15
 */
public class SerialPort {
	/** Log日志输出标识 */
	private static final String TAG = "SerialPort";

	/** 串口文件描述符，禁止删除或重命名，因为native层关闭串口时需要使用 */
	private FileDescriptor mFd;
	/** 输入流，用于接收串口数据 */
	private FileInputStream mFileInputStream;
	/** 输出流，用于发送串口数据 */
	private FileOutputStream mFileOutputStream;

	/**
	 * 构造函数
	 * 
	 * @param device
	 *            串口名
	 * @param baudrate
	 *            波特率
	 * @param flags
	 *            操作标识
	 * @throws SecurityException
	 *             安全异常，当串口文件不可读写时触发
	 * @throws IOException
	 *             IO异常，开启串口失败时触发
	 */
	public SerialPort(File device, int baudrate, int flags)
			throws SecurityException, IOException {
		/* 检测设备管理权限，即文件的权限属性 */
		if (!device.canRead() || !device.canWrite()) {
			try {
				if (Build.VERSION.SDK_INT >= 23) {
					String file_contexts = "/device/sprd/scx20/common/sepolicy/file_contexts";
					String device_te = "/device/sprd/scx20/common/sepolicy/device.te";
					String untrusted_app_te = "/device/sprd/scx20/common/sepolicy/untrusted_app.te";

					String text1 = "/dev/abc u:object_r:abc_device:s0";
					String text2 = "type abc_device, dev_type, mlstrustedobject;";
					String text3 = "allow untrusted_app adc_device:chr_fileoperate;";

					String text4 = "echo ";
					String text5 = ">>";

					for (int i = 0; i < 3; i++) {
						String content = "";
						String filePath = "";
						if (i == 0) {
							content = text1;
							filePath = file_contexts;
						} else if (i == 1) {
							content = text2;
							filePath = device_te;
						} else if (i == 2) {
							content = text3;
							filePath = untrusted_app_te;
						}
						Process su;
						su = Runtime.getRuntime().exec("/system/bin/su");
						String cmd = text4 + content + text5 + filePath + "\n";
						su.getOutputStream().write(cmd.getBytes());
						if ((su.waitFor() != 0)) {
							throw new SecurityException();
						}
					}
				}

				/* 若没有读/写权限，试着chmod该设备 */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	/**
	 * 获取输入流
	 * 
	 * @return 串口输入流
	 */
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	/**
	 * 获取输出流
	 * 
	 * @return 串口输出流
	 */
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	/**
	 * 原生函数，开启串口虚拟文件
	 * 
	 * @param path
	 *            串口虚拟文件路径
	 * @param baudrate
	 *            波特率
	 * @param flags
	 *            操作标识
	 * @return
	 */
	private native static FileDescriptor open(String path, int baudrate,
			int flags);

	/**
	 * 原生函数，关闭串口虚拟文件
	 */
	public native void close();

	static {
		System.loadLibrary("serial_port");
	}
}
